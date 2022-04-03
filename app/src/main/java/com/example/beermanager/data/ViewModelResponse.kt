package com.example.beermanager.data;

/**
 * Represents state of fetching data in [androidx.lifecycle.ViewModel].
 */
sealed class ViewModelResponse<out T, out E> {
    object Idle: ViewModelResponse<Nothing, Nothing>()
    object Loading: ViewModelResponse<Nothing, Nothing>()
    data class Error<out E>(val error: E): ViewModelResponse<Nothing, E>()
    data class Success<out T>(val content: T): ViewModelResponse<T, Nothing>()
}