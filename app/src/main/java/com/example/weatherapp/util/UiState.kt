package com.example.weatherapp.util

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>()
    data object Empty : UiState<Nothing>()
}

/** Convenience: run a block only when the state holds data. */
inline fun <T> UiState<T>.onSuccess(block: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) block(data)
    return this
}

inline fun <T> UiState<T>.onError(block: (String, Throwable?) -> Unit): UiState<T> {
    if (this is UiState.Error) block(message, cause)
    return this
}
