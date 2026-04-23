# WeatherWise

A production-ready Android weather app built with modern Kotlin and Clean Architecture. Fetches real-time weather data from the OpenWeatherMap API, caches results offline with Room, and delivers a polished Material Design 3 experience across light and dark themes.

---

## Features

- **Current conditions** — temperature, feels-like, humidity, wind, pressure, visibility, cloudiness, sunrise/sunset
- **Hourly forecast** — next 24 hours in a horizontal scrollable strip
- **5-day daily forecast** — grouped daily highs/lows with precipitation probability
- **Location-aware home screen** — auto-fetches weather for your GPS position
- **City / coordinates / ZIP / City ID search** — four search modes via a chip selector
- **Saved locations** — bookmark any city; swipe to delete
- **Unit preferences** — Celsius / Fahrenheit; m/s / mph / km/h; persisted with DataStore
- **Dark mode** — follows system theme with full Material You colour tokens
- **Offline cache** — Room database with a 30-minute TTL so the last result is always available

---

## Architecture

```
app/
├── data/               # Remote DTOs, Room entities/DAOs, WeatherRepositoryImpl
├── domain/             # Pure Kotlin models, WeatherRepository interface, use cases
├── presentation/       # Single Activity + 4 Fragments + 3 ListAdapters
├── di/                 # Hilt modules (Network, Database, App)
└── util/               # UiState, Extensions, LocationHelper, WeatherIconMapper, Constants
```

The project follows **MVVM + Clean Architecture**:

- **Domain layer** knows nothing about Android or any framework.
- **Data layer** communicates with the network and database; returns `Result<T>` to callers.
- **Presentation layer** holds `StateFlow<UiState<T>>` in every ViewModel; Fragments observe and render.
- A single `MainActivity` hosts a `NavHostFragment`; screens are Fragments managed by the **Navigation Component** with Safe Args typed navigation.

---

## Tech Stack

| Area | Library |
|---|---|
| Language | Kotlin 1.9 |
| DI | Hilt 2.51 |
| Networking | Retrofit 2.11 + OkHttp 4.12 + Gson |
| Database | Room 2.6 |
| Async | Coroutines + StateFlow |
| Navigation | Navigation Component 2.7 + Safe Args |
| Location | Fused Location Provider (Play Services 21.3) |
| Preferences | DataStore Preferences 1.1 |
| UI | Material Design 3 (DayNight) + ViewBinding |
| Logging | Timber 5.0 |
| Testing | JUnit 4, MockK, Turbine |
| Build | Gradle KTS + Version Catalog (`libs.versions.toml`) |

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17 (Android Studio bundles one; set it under **Settings → Build Tools → Gradle → Gradle JDK**)
- An [OpenWeatherMap](https://openweathermap.org/api) free-tier API key

### 1. Clone the repository

```bash
git clone https://github.com/jannaten/Weather-Native-Android-App.git
cd Weather-Native-Android-App
```

### 2. Get an API key

1. Create a free account at [openweathermap.org](https://openweathermap.org/)
2. Go to **My API Keys** in your account dashboard
3. Copy the default key (or generate a new one — it activates within ~2 hours)

### 3. Add the key to local.properties

Create (or open) `local.properties` in the project root and add:

```properties
WEATHER_API_KEY=your_api_key_here
```

> `local.properties` is gitignored and never committed — each developer adds their own key locally.

### 4. Sync and run

1. Open the project in Android Studio
2. Click **File → Sync Project with Gradle Files**
3. Select a device or emulator (API 26+) and click **Run**

---

## Project Structure

```
app/src/main/java/com/example/weatherapp/
├── WeatherApp.kt
├── data/
│   ├── local/
│   │   ├── dao/          SavedLocationDao, WeatherDao
│   │   ├── db/           WeatherDatabase
│   │   └── entity/       WeatherEntity, SavedLocationEntity
│   ├── remote/
│   │   ├── api/          WeatherApiService
│   │   └── dto/          WeatherResponseDto, ForecastResponseDto
│   └── repository/       WeatherRepositoryImpl
├── di/
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   └── NetworkModule.kt
├── domain/
│   ├── model/            Weather, WeatherForecast, SavedLocation
│   ├── repository/       WeatherRepository
│   └── usecase/          GetCurrentWeatherUseCase, GetForecastUseCase, ManageSavedLocationsUseCase
├── presentation/
│   ├── MainActivity.kt
│   ├── adapter/          HourlyForecastAdapter, ForecastDayAdapter, SavedLocationsAdapter
│   ├── home/             HomeFragment, HomeViewModel
│   ├── search/           SearchFragment, SearchViewModel
│   ├── savedlocations/   SavedLocationsFragment, SavedLocationsViewModel
│   ├── settings/         SettingsFragment, SettingsViewModel
│   └── weather/          WeatherDetailFragment, WeatherDetailViewModel
└── util/
    ├── Constants.kt
    ├── Extensions.kt
    ├── LocationHelper.kt
    ├── UiState.kt
    └── WeatherIconMapper.kt
```

---

## Running Tests

```bash
./gradlew test
```

Unit tests cover the `GetCurrentWeatherUseCase` (all four search modes) and `SearchViewModel` (state transitions, input validation, mode switching) using MockK and Turbine.

---

## License

This project is open source and available under the [MIT License](LICENSE).
