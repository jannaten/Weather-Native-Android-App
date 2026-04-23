package com.example.weatherapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.domain.usecase.GetCurrentWeatherUseCase
import com.example.weatherapp.presentation.search.SearchMode
import com.example.weatherapp.presentation.search.SearchViewModel
import com.example.weatherapp.util.UiState
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getWeatherUseCase: GetCurrentWeatherUseCase
    private lateinit var viewModel: SearchViewModel

    private val fakeWeather = Weather(
        cityId = 1, cityName = "London", country = "GB",
        latitude = 51.5, longitude = -0.12,
        temperature = 12.0, feelsLike = 10.0, tempMin = 9.0, tempMax = 14.0,
        humidity = 70, pressure = 1015, windSpeed = 5.0, windDegree = 180,
        visibility = 9000, cloudiness = 50,
        weatherId = 803, weatherMain = "Clouds", weatherDescription = "broken clouds",
        weatherIcon = "04d", sunrise = 0L, sunset = 0L, timezone = 0, timestamp = 0L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getWeatherUseCase = mockk()
        viewModel = SearchViewModel(getWeatherUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Empty`() = runTest {
        viewModel.searchState.test {
            assertEquals(UiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchByCity emits Loading then Success`() = runTest {
        coEvery { getWeatherUseCase("London", "GB") } returns Result.success(fakeWeather)

        viewModel.searchState.test {
            assertEquals(UiState.Empty, awaitItem())
            viewModel.searchByCity("London", "GB")
            assertEquals(UiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val success = awaitItem()
            assertTrue(success is UiState.Success)
            assertEquals(fakeWeather, (success as UiState.Success).data)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchByCity with blank input emits Error without network call`() = runTest {
        viewModel.searchState.test {
            assertEquals(UiState.Empty, awaitItem())
            viewModel.searchByCity("  ", "")
            val error = awaitItem()
            assertTrue(error is UiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchByCoordinates with invalid lat emits Error`() = runTest {
        viewModel.searchState.test {
            assertEquals(UiState.Empty, awaitItem())
            viewModel.searchByCoordinates("200.0", "0.0")
            val error = awaitItem()
            assertTrue(error is UiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSearchMode updates mode and resets state to Empty`() = runTest {
        viewModel.setSearchMode(SearchMode.COORDINATES)
        assertEquals(SearchMode.COORDINATES, viewModel.searchMode.value)
        assertEquals(UiState.Empty, viewModel.searchState.value)
    }
}
