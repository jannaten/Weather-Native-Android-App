package com.example.weatherapp

import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.domain.usecase.GetCurrentWeatherUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetCurrentWeatherUseCaseTest {

    private lateinit var repository: WeatherRepository
    private lateinit var useCase: GetCurrentWeatherUseCase

    private val fakeWeather = Weather(
        cityId = 1, cityName = "Helsinki", country = "FI",
        latitude = 60.17, longitude = 24.93,
        temperature = 5.0, feelsLike = 2.0,
        tempMin = 3.0, tempMax = 7.0,
        humidity = 80, pressure = 1012, windSpeed = 4.0, windDegree = 270,
        visibility = 10000, cloudiness = 40,
        weatherId = 800, weatherMain = "Clear", weatherDescription = "clear sky",
        weatherIcon = "01d", sunrise = 0L, sunset = 0L, timezone = 7200, timestamp = 0L
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetCurrentWeatherUseCase(repository)
    }

    @Test
    fun `invoke with coordinates calls repository and returns success`() = runTest {
        coEvery { repository.getWeatherByCoordinates(60.17, 24.93) } returns Result.success(fakeWeather)

        val result = useCase(60.17, 24.93)

        assertTrue(result.isSuccess)
        assertEquals(fakeWeather, result.getOrNull())
        coVerify(exactly = 1) { repository.getWeatherByCoordinates(60.17, 24.93) }
    }

    @Test
    fun `invoke with city name calls repository`() = runTest {
        coEvery { repository.getWeatherByCity("Helsinki", "FI") } returns Result.success(fakeWeather)

        val result = useCase("Helsinki", "FI")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.getWeatherByCity("Helsinki", "FI") }
    }

    @Test
    fun `invoke with city name propagates failure`() = runTest {
        val ex = RuntimeException("City not found")
        coEvery { repository.getWeatherByCity("XYZ", "") } returns Result.failure(ex)

        val result = useCase("XYZ", "")

        assertTrue(result.isFailure)
        assertEquals(ex, result.exceptionOrNull())
    }

    @Test
    fun `invoke with city ID calls repository`() = runTest {
        coEvery { repository.getWeatherByCityId(2643743) } returns Result.success(fakeWeather)

        val result = useCase(2643743)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.getWeatherByCityId(2643743) }
    }

    @Test
    fun `byZip delegates to repository`() = runTest {
        coEvery { repository.getWeatherByZip("00100", "FI") } returns Result.success(fakeWeather)

        val result = useCase.byZip("00100", "FI")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.getWeatherByZip("00100", "FI") }
    }
}
