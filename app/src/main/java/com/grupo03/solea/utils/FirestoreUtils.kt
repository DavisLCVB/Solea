package com.grupo03.solea.utils

import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Generic function to map Firestore exceptions to domain-specific errors.
 *
 * This function translates Firebase Firestore exception codes into application-specific
 * error types, providing better type safety and error handling throughout the application.
 *
 * @param T The type of AppError to return
 * @param e The FirebaseFirestoreException to map
 * @param defaultError The error to return for unmapped exceptions
 * @param notFound The error to return when the document/collection is not found
 * @param permissionDenied The error to return when access is denied
 * @param networkError The error to return when the network is unavailable
 * @return The appropriate domain-specific error
 */
inline fun <reified T : AppError> mapFirestoreException(
    e: FirebaseFirestoreException,
    defaultError: T,
    notFound: T,
    permissionDenied: T,
    networkError: T
): T {
    return when (e.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> permissionDenied
        FirebaseFirestoreException.Code.UNAVAILABLE -> networkError
        FirebaseFirestoreException.Code.NOT_FOUND -> notFound
        else -> defaultError
    }
}

/**
 * Maps FirebaseFirestoreException to BudgetError.
 *
 * @param e The exception to map
 * @param defaultError The default error to use for unmapped exceptions
 * @return The appropriate BudgetError
 */
fun mapFirestoreException(
    e: FirebaseFirestoreException,
    defaultError: BudgetError
): BudgetError {
    return mapFirestoreException(
        e,
        defaultError,
        notFound = BudgetError.NOT_FOUND,
        permissionDenied = BudgetError.PERMISSION_DENIED,
        networkError = BudgetError.NETWORK_ERROR
    )
}

/**
 * Maps FirebaseFirestoreException to MovementError.
 *
 * @param e The exception to map
 * @param defaultError The default error to use for unmapped exceptions
 * @return The appropriate MovementError
 */
fun mapFirestoreException(
    e: FirebaseFirestoreException,
    defaultError: MovementError
): MovementError {
    return mapFirestoreException(
        e,
        defaultError,
        notFound = MovementError.NOT_FOUND,
        permissionDenied = MovementError.PERMISSION_DENIED,
        networkError = MovementError.NETWORK_ERROR
    )
}

/**
 * Maps FirebaseFirestoreException to CategoryError.
 *
 * @param e The exception to map
 * @param defaultError The default error to use for unmapped exceptions
 * @return The appropriate CategoryError
 */
fun mapFirestoreException(
    e: FirebaseFirestoreException,
    defaultError: CategoryError
): CategoryError {
    return mapFirestoreException(
        e,
        defaultError,
        notFound = CategoryError.NOT_FOUND,
        permissionDenied = CategoryError.PERMISSION_DENIED,
        networkError = CategoryError.NETWORK_ERROR
    )
}

/**
 * Maps FirebaseFirestoreException to ItemError.
 *
 * @param e The exception to map
 * @param defaultError The default error to use for unmapped exceptions
 * @return The appropriate ItemError
 */
fun mapFirestoreException(
    e: FirebaseFirestoreException,
    defaultError: ItemError
): ItemError {
    return mapFirestoreException(
        e,
        defaultError,
        notFound = ItemError.NOT_FOUND,
        permissionDenied = ItemError.PERMISSION_DENIED,
        networkError = ItemError.NETWORK_ERROR
    )
}

/**
 * Maps FirebaseFirestoreException to ReceiptError.
 *
 * @param e The exception to map
 * @param defaultError The default error to use for unmapped exceptions
 * @return The appropriate ReceiptError
 */
fun mapFirestoreException(
    e: FirebaseFirestoreException,
    defaultError: ReceiptError
): ReceiptError {
    return mapFirestoreException(
        e,
        defaultError,
        notFound = ReceiptError.NOT_FOUND,
        permissionDenied = ReceiptError.PERMISSION_DENIED,
        networkError = ReceiptError.NETWORK_ERROR
    )
}

/**
 * Maps FirebaseFirestoreException to SourceError.
 *
 * @param e The exception to map
 * @param defaultError The default error to use for unmapped exceptions
 * @return The appropriate SourceError
 */
@Suppress("unused")
fun mapFirestoreException(
    e: FirebaseFirestoreException,
    defaultError: SourceError
): SourceError {
    return mapFirestoreException(
        e,
        defaultError,
        notFound = SourceError.NOT_FOUND,
        permissionDenied = SourceError.PERMISSION_DENIED,
        networkError = SourceError.NETWORK_ERROR
    )
}

/**
 * Maps FirebaseFirestoreException to UserError.
 *
 * @param e The exception to map
 * @param defaultError The default error to use for unmapped exceptions
 * @return The appropriate UserError
 */
@Suppress("unused")
fun mapFirestoreException(
    e: FirebaseFirestoreException,
    defaultError: UserError
): UserError {
    return mapFirestoreException(
        e,
        defaultError,
        notFound = UserError.NOT_FOUND,
        permissionDenied = UserError.PERMISSION_DENIED,
        networkError = UserError.NETWORK_ERROR
    )
}

/**
 * Maps FirebaseFirestoreException to SavingsGoalError.
 *
 * @param e The exception to map
 * @param defaultError The default error to use for unmapped exceptions
 * @return The appropriate SavingsGoalError
 */
fun mapFirestoreException(
    e: FirebaseFirestoreException,
    defaultError: SavingsGoalError
): SavingsGoalError {
    return mapFirestoreException(
        e,
        defaultError,
        notFound = SavingsGoalError.NOT_FOUND,
        permissionDenied = SavingsGoalError.PERMISSION_DENIED,
        networkError = SavingsGoalError.NETWORK_ERROR
    )
}
