package com.example.weatherapp.util

/**
 * Maps OpenWeatherMap condition codes to descriptive emoji icons.
 * OWM codes: https://openweathermap.org/weather-conditions
 */
object WeatherIconMapper {

    fun getEmoji(weatherId: Int): String = when (weatherId) {
        in 200..232 -> "⛈️"   // Thunderstorm
        in 300..321 -> "🌦️"   // Drizzle
        in 500..504 -> "🌧️"   // Rain
        511        -> "🌨️"   // Freezing rain
        in 520..531 -> "🌧️"   // Shower rain
        in 600..622 -> "❄️"   // Snow
        in 700..781 -> "🌫️"   // Atmosphere (fog, mist, haze…)
        800        -> "☀️"   // Clear sky
        801        -> "🌤️"   // Few clouds
        802        -> "⛅"   // Scattered clouds
        in 803..804 -> "☁️"   // Broken / overcast clouds
        else       -> "🌡️"
    }

    fun getLabel(weatherId: Int): String = when (weatherId) {
        in 200..232 -> "Thunderstorm"
        in 300..321 -> "Drizzle"
        in 500..531 -> "Rain"
        in 600..622 -> "Snow"
        in 700..781 -> "Hazy"
        800        -> "Clear"
        in 801..802 -> "Partly Cloudy"
        in 803..804 -> "Cloudy"
        else       -> "Unknown"
    }

    /** Background gradient resource name based on general condition. */
    fun getBackgroundType(weatherId: Int, isDay: Boolean): String = when {
        weatherId == 800 && isDay -> "clear_day"
        weatherId == 800          -> "clear_night"
        weatherId in 801..804     -> "cloudy"
        weatherId in 200..232     -> "stormy"
        weatherId in 300..531     -> "rainy"
        weatherId in 600..622     -> "snowy"
        else                      -> "default"
    }
}
