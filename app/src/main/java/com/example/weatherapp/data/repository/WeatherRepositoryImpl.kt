package com.example.weatherapp.data.repository

import com.example.weatherapp.data.local.dao.SavedLocationDao
import com.example.weatherapp.data.local.dao.WeatherDao
import com.example.weatherapp.data.local.entity.SavedLocationEntity
import com.example.weatherapp.data.local.entity.WeatherEntity
import com.example.weatherapp.data.remote.api.WeatherApiService
import com.example.weatherapp.data.remote.dto.ForecastItemDto
import com.example.weatherapp.data.remote.dto.WeatherResponseDto
import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.domain.model.SavedLocation
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.domain.model.WeatherForecast
import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao,
    private val savedLocationDao: SavedLocationDao
) : WeatherRepository {

    // region Current weather

    override suspend fun getWeatherByCoordinates(lat: Double, lon: Double): Result<Weather> =
        safeApiCall { apiService.getWeatherByCoordinates(lat, lon) }

    override suspend fun getWeatherByCity(city: String, countryCode: String): Result<Weather> {
        val query = if (countryCode.isBlank()) city else "$city,$countryCode"
        return safeApiCall { apiService.getWeatherByCity(query) }
    }

    override suspend fun getWeatherByCityId(cityId: Int): Result<Weather> =
        safeApiCall { apiService.getWeatherByCityId(cityId) }

    override suspend fun getWeatherByZip(zipCode: String, countryCode: String): Result<Weather> =
        safeApiCall { apiService.getWeatherByZip("$zipCode,$countryCode") }

    // endregion

    // region Forecast

    override suspend fun getForecastByCoordinates(lat: Double, lon: Double): Result<WeatherForecast> =
        runCatching {
            val dto = apiService.getForecastByCoordinates(lat, lon)
            dto.toWeatherForecast()
        }.onFailure { Timber.e(it, "getForecastByCoordinates failed") }

    override suspend fun getForecastByCity(city: String, countryCode: String): Result<WeatherForecast> =
        runCatching {
            val query = if (countryCode.isBlank()) city else "$city,$countryCode"
            val dto = apiService.getForecastByCity(query)
            dto.toWeatherForecast()
        }.onFailure { Timber.e(it, "getForecastByCity failed") }

    // endregion

    // region Saved locations

    override fun getSavedLocations(): Flow<List<SavedLocation>> =
        savedLocationDao.getAllLocations().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun saveLocation(location: SavedLocation) {
        savedLocationDao.insertLocation(location.toEntity())
    }

    override suspend fun deleteLocation(locationId: Int) {
        savedLocationDao.deleteLocation(locationId)
    }

    override suspend fun isLocationSaved(cityId: Int): Boolean =
        savedLocationDao.isLocationSaved(cityId) > 0

    // endregion

    // region Offline cache

    override suspend fun getCachedWeather(cityId: Int): Weather? =
        weatherDao.getWeatherByCityId(cityId)?.toDomain()

    // endregion

    // region Helpers

    private suspend fun safeApiCall(call: suspend () -> WeatherResponseDto): Result<Weather> =
        runCatching {
            val dto = call()
            val domain = dto.toDomain()
            // Cache successful response
            weatherDao.insertWeather(domain.toEntity())
            domain
        }.onFailure { Timber.e(it, "API call failed") }

    // endregion

    // region DTO → Domain mappers

    private fun WeatherResponseDto.toDomain(): Weather {
        val condition = weather.firstOrNull()
        return Weather(
            cityId = id,
            cityName = name,
            country = sys.country.orEmpty(),
            latitude = coord.lat,
            longitude = coord.lon,
            temperature = main.temp,
            feelsLike = main.feelsLike,
            tempMin = main.tempMin,
            tempMax = main.tempMax,
            humidity = main.humidity,
            pressure = main.pressure,
            windSpeed = wind.speed,
            windDegree = wind.deg ?: 0,
            visibility = visibility ?: 10000,
            cloudiness = clouds?.all ?: 0,
            weatherId = condition?.id ?: 800,
            weatherMain = condition?.main.orEmpty(),
            weatherDescription = condition?.description.orEmpty(),
            weatherIcon = condition?.icon.orEmpty(),
            sunrise = sys.sunrise ?: 0L,
            sunset = sys.sunset ?: 0L,
            timezone = timezone,
            timestamp = dt
        )
    }

    private fun com.example.weatherapp.data.remote.dto.ForecastResponseDto.toWeatherForecast(): WeatherForecast {
        val hourly = list.take(8).map { it.toHourlyForecast() }    // next 24 h (3-h steps)
        val daily = list.groupByDay().map { (_, items) ->
            val temps = items.map { it.main.temp }
            val condition = items.maxByOrNull { it.pop ?: 0.0 }
                ?: items.first()
            val weatherCond = condition.weather.firstOrNull()
            DailyForecast(
                date = items.first().dt,
                tempMin = temps.min(),
                tempMax = temps.max(),
                humidity = items.map { it.main.humidity }.average().toInt(),
                windSpeed = items.map { it.wind.speed }.average(),
                weatherId = weatherCond?.id ?: 800,
                weatherMain = weatherCond?.main.orEmpty(),
                weatherDescription = weatherCond?.description.orEmpty(),
                weatherIcon = weatherCond?.icon.orEmpty(),
                precipitationProbability = items.mapNotNull { it.pop }.maxOrNull() ?: 0.0
            )
        }
        return WeatherForecast(
            cityName = city.name,
            country = city.country,
            hourlyForecasts = hourly,
            dailyForecasts = daily
        )
    }

    private fun ForecastItemDto.toHourlyForecast(): HourlyForecast {
        val cond = weather.firstOrNull()
        return HourlyForecast(
            timestamp = dt,
            temperature = main.temp,
            weatherId = cond?.id ?: 800,
            weatherMain = cond?.main.orEmpty(),
            weatherIcon = cond?.icon.orEmpty(),
            precipitationProbability = pop ?: 0.0
        )
    }

    /** Groups 3-hourly items into calendar days (UTC). */
    private fun List<ForecastItemDto>.groupByDay(): Map<Int, List<ForecastItemDto>> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        return groupBy { item ->
            cal.timeInMillis = item.dt * 1000L
            cal.get(Calendar.DAY_OF_YEAR)
        }
    }

    // endregion

    // region Domain ↔ Entity mappers

    private fun Weather.toEntity() = WeatherEntity(
        cityId = cityId,
        cityName = cityName,
        country = country,
        latitude = latitude,
        longitude = longitude,
        temperature = temperature,
        feelsLike = feelsLike,
        tempMin = tempMin,
        tempMax = tempMax,
        humidity = humidity,
        pressure = pressure,
        windSpeed = windSpeed,
        windDegree = windDegree,
        visibility = visibility,
        cloudiness = cloudiness,
        weatherId = weatherId,
        weatherMain = weatherMain,
        weatherDescription = weatherDescription,
        weatherIcon = weatherIcon,
        sunrise = sunrise,
        sunset = sunset,
        timezone = timezone
    )

    private fun WeatherEntity.toDomain() = Weather(
        cityId = cityId,
        cityName = cityName,
        country = country,
        latitude = latitude,
        longitude = longitude,
        temperature = temperature,
        feelsLike = feelsLike,
        tempMin = tempMin,
        tempMax = tempMax,
        humidity = humidity,
        pressure = pressure,
        windSpeed = windSpeed,
        windDegree = windDegree,
        visibility = visibility,
        cloudiness = cloudiness,
        weatherId = weatherId,
        weatherMain = weatherMain,
        weatherDescription = weatherDescription,
        weatherIcon = weatherIcon,
        sunrise = sunrise,
        sunset = sunset,
        timezone = timezone,
        timestamp = cachedAt / 1000L
    )

    private fun SavedLocation.toEntity() = SavedLocationEntity(
        id = id,
        cityId = cityId,
        name = name,
        country = country,
        latitude = latitude,
        longitude = longitude,
        isCurrentLocation = isCurrentLocation
    )

    private fun SavedLocationEntity.toDomain() = SavedLocation(
        id = id,
        cityId = cityId,
        name = name,
        country = country,
        latitude = latitude,
        longitude = longitude,
        isCurrentLocation = isCurrentLocation
    )

    // endregion
}
