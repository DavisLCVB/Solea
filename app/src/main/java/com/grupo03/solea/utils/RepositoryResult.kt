package com.grupo03.solea.utils

/**
 * Generic result wrapper for repository operations
 * The error type is constrained to AppError to ensure type safety
 */
sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val error: AppError) : RepositoryResult<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun errorOrNull(): AppError? = when (this) {
        is Success -> null
        is Error -> error
    }

    inline fun onSuccess(action: (T) -> Unit): RepositoryResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (AppError) -> Unit): RepositoryResult<T> {
        if (this is Error) action(error)
        return this
    }

    /**
     * Type-safe error extraction for specific error types
     */
    inline fun <reified E : AppError> errorAs(): E? = when (this) {
        is Success -> null
        is Error -> error as? E
    }
}