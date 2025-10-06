package com.grupo03.solea.utils

import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Generic function to map Firestore exceptions to domain-specific errors
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

// Convenience functions for each domain

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
