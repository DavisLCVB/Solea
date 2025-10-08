package com.grupo03.solea.utils

/**
 * Generic result wrapper for repository operations.
 *
 * This sealed class encapsulates the result of repository operations,
 * providing type-safe handling of success and error cases. The error type
 * is constrained to AppError to ensure type safety.
 *
 * Use pattern matching to handle results:
 * ```kotlin
 * when (result) {
 *     is RepositoryResult.Success -> { /* use result.data */ }
 *     is RepositoryResult.Error -> { /* handle result.error */ }
 * }
 * ```
 *
 * Or use the functional extensions:
 * ```kotlin
 * result
 *     .onSuccess { data -> /* handle success */ }
 *     .onError { error -> /* handle error */ }
 * ```
 *
 * @param T The type of data contained in a successful result
 */
sealed class RepositoryResult<out T> {
    /**
     * Represents a successful repository operation.
     *
     * @param T The type of data returned
     * @property data The data returned by the operation
     */
    data class Success<T>(val data: T) : RepositoryResult<T>()

    /**
     * Represents a failed repository operation.
     *
     * @property error The specific domain error that occurred
     */
    data class Error(val error: AppError) : RepositoryResult<Nothing>()

    /**
     * Checks if this result represents a success.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Checks if this result represents an error.
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Returns the data if this is a Success, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the error if this is an Error, null otherwise.
     */
    fun errorOrNull(): AppError? = when (this) {
        is Success -> null
        is Error -> error
    }

    /**
     * Executes the given action if this is a Success.
     *
     * @param action The action to execute with the data
     * @return This RepositoryResult for chaining
     */
    inline fun onSuccess(action: (T) -> Unit): RepositoryResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Executes the given action if this is an Error.
     *
     * @param action The action to execute with the error
     * @return This RepositoryResult for chaining
     */
    @Suppress("unused")
    inline fun onError(action: (AppError) -> Unit): RepositoryResult<T> {
        if (this is Error) action(error)
        return this
    }

    /**
     * Type-safe error extraction for specific error types.
     *
     * Attempts to cast the error to a specific AppError subtype.
     *
     * @param E The specific AppError subtype to extract
     * @return The error cast to type E if this is an Error and the cast succeeds, null otherwise
     */
    inline fun <reified E : AppError> errorAs(): E? = when (this) {
        is Success -> null
        is Error -> error as? E
    }
}
