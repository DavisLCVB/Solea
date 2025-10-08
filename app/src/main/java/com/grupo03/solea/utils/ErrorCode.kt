package com.grupo03.solea.utils

import androidx.annotation.StringRes
import com.grupo03.solea.R

/**
 * Base interface for all application errors
 */
sealed interface AppError {
    val code: String

    @get:StringRes
    val messageRes: Int

    /**
     * Optional additional context for the error
     */
    val context: Map<String, Any>
        get() = emptyMap()

}

/**
 * Authentication related errors
 */
enum class AuthError(
    override val code: String,
    @StringRes override val messageRes: Int
) : AppError {
    USER_NOT_FOUND("AUTH_001", R.string.error_auth_user_not_found),
    USER_COLLISION("AUTH_002", R.string.error_auth_user_collision),
    USERNAME_INVALID("AUTH_003", R.string.error_auth_invalid_username),
    INVALID_CREDENTIALS("AUTH_004", R.string.error_auth_invalid_credentials),
    EMAIL_ERROR("AUTH_005", R.string.error_auth_email),
    EMAIL_EMPTY("AUTH_006", R.string.error_auth_email_empty),
    EMAIL_INVALID("AUTH_007", R.string.error_auth_invalid_email),
    EMAIL_TOO_LONG("AUTH_008", R.string.error_auth_email_too_long),
    WEAK_PASSWORD("AUTH_009", R.string.error_auth_weak_password),
    PASSWORD_EMPTY("AUTH_010", R.string.error_auth_password_empty),
    PASSWORDS_DO_NOT_MATCH("AUTH_011", R.string.error_auth_passwords_do_not_match),
    GOOGLE_SIGN_IN_FAILED("AUTH_012", R.string.error_auth_google_sign_in_failed),
    UNKNOWN_ERROR("AUTH_999", R.string.error_auth_unknown);
}

/**
 * Movement related errors
 */
enum class MovementError(
    override val code: String,
    @StringRes override val messageRes: Int
) : AppError {
    NOT_FOUND("MOV_001", R.string.error_movement_not_found),
    CREATION_FAILED("MOV_002", R.string.error_movement_creation_failed),
    UPDATE_FAILED("MOV_003", R.string.error_movement_update_failed),
    DELETE_FAILED("MOV_004", R.string.error_movement_delete_failed),
    FETCH_FAILED("MOV_005", R.string.error_movement_fetch_failed),
    INVALID_AMOUNT("MOV_006", R.string.error_movement_invalid_amount),
    INVALID_TYPE("MOV_007", R.string.error_movement_invalid_type),
    PERMISSION_DENIED("MOV_012", R.string.error_movement_permission_denied),
    NETWORK_ERROR("MOV_013", R.string.error_movement_network_error),
    UNKNOWN_ERROR("MOV_999", R.string.error_movement_unknown);
}

/**
 * Budget related errors
 */
enum class BudgetError(
    override val code: String,
    @StringRes override val messageRes: Int
) : AppError {
    NOT_FOUND("BDG_001", R.string.error_budget_not_found),
    CREATION_FAILED("BDG_002", R.string.error_budget_creation_failed),
    UPDATE_FAILED("BDG_003", R.string.error_budget_update_failed),
    DELETE_FAILED("BDG_004", R.string.error_budget_delete_failed),
    FETCH_FAILED("BDG_005", R.string.error_budget_fetch_failed),
    INVALID_AMOUNT("BDG_006", R.string.error_budget_invalid_amount),
    INVALID_CATEGORY("BDG_007", R.string.error_budget_invalid_category),
    PERMISSION_DENIED("BDG_011", R.string.error_budget_permission_denied),
    NETWORK_ERROR("BDG_012", R.string.error_budget_network_error),
    UNKNOWN_ERROR("BDG_999", R.string.error_budget_unknown);
}

/**
 * Receipt related errors
 */
enum class ReceiptError(
    override val code: String,
    @StringRes override val messageRes: Int
) : AppError {
    NOT_FOUND("RCP_001", R.string.error_receipt_not_found),
    CREATION_FAILED("RCP_002", R.string.error_receipt_creation_failed),
    UPDATE_FAILED("RCP_003", R.string.error_receipt_update_failed),
    DELETE_FAILED("RCP_004", R.string.error_receipt_delete_failed),
    FETCH_FAILED("RCP_005", R.string.error_receipt_fetch_failed),
    PERMISSION_DENIED("RCP_007", R.string.error_receipt_permission_denied),
    NETWORK_ERROR("RCP_008", R.string.error_receipt_network_error),
    UNKNOWN_ERROR("RCP_999", R.string.error_receipt_unknown);
}

/**
 * Item related errors
 */
enum class ItemError(
    override val code: String,
    @StringRes override val messageRes: Int
) : AppError {
    NOT_FOUND("ITM_001", R.string.error_item_not_found),
    CREATION_FAILED("ITM_002", R.string.error_item_creation_failed),
    UPDATE_FAILED("ITM_003", R.string.error_item_update_failed),
    DELETE_FAILED("ITM_004", R.string.error_item_delete_failed),
    FETCH_FAILED("ITM_005", R.string.error_item_fetch_failed),
    PERMISSION_DENIED("ITM_007", R.string.error_item_permission_denied),
    NETWORK_ERROR("ITM_008", R.string.error_item_network_error),
    UNKNOWN_ERROR("ITM_999", R.string.error_item_unknown);
}

/**
 * Generic Repository/Infrastructure errors
 * Used only for cross-cutting concerns not specific to any domain
 */
enum class RepositoryError(
    override val code: String,
    @StringRes override val messageRes: Int
) : AppError {
    @Suppress("unused")
    DATABASE_CONNECTION_FAILED("REP_001", R.string.error_repository_database_connection_failed),

    @Suppress("unused")
    TRANSACTION_FAILED("REP_002", R.string.error_repository_transaction_failed),

    @Suppress("unused")
    SERIALIZATION_ERROR("REP_003", R.string.error_repository_serialization_error),
    UNKNOWN_ERROR("REP_999", R.string.error_repository_unknown);
}

/**
 * Category related errors
 */
enum class CategoryError(
    override val code: String,
    @StringRes override val messageRes: Int
) : AppError {
    NOT_FOUND("CAT_001", R.string.error_category_not_found),
    CREATION_FAILED("CAT_002", R.string.error_category_creation_failed),
    UPDATE_FAILED("CAT_003", R.string.error_category_update_failed),
    DELETE_FAILED("CAT_004", R.string.error_category_delete_failed),
    FETCH_FAILED("CAT_005", R.string.error_category_fetch_failed),
    INVALID_NAME("CAT_006", R.string.error_category_invalid_name),
    ALREADY_EXISTS("CAT_008", R.string.error_category_already_exists),
    PERMISSION_DENIED("CAT_009", R.string.error_category_permission_denied),
    NETWORK_ERROR("CAT_010", R.string.error_category_network_error),
    UNKNOWN_ERROR("CAT_999", R.string.error_category_unknown);
}

/**
 * Source related errors (for income sources)
 */
enum class SourceError(
    override val code: String,
    @StringRes override val messageRes: Int
) : AppError {
    NOT_FOUND("SRC_001", R.string.error_source_not_found),

    @Suppress("unused")
    CREATION_FAILED("SRC_002", R.string.error_source_creation_failed),

    @Suppress("unused")
    UPDATE_FAILED("SRC_003", R.string.error_source_update_failed),

    @Suppress("unused")
    DELETE_FAILED("SRC_004", R.string.error_source_delete_failed),

    @Suppress("unused")
    FETCH_FAILED("SRC_005", R.string.error_source_fetch_failed),

    @Suppress("unused")
    INVALID_DATA("SRC_006", R.string.error_source_invalid_data),
    PERMISSION_DENIED("SRC_007", R.string.error_source_permission_denied),
    NETWORK_ERROR("SRC_008", R.string.error_source_network_error),
    UNKNOWN_ERROR("SRC_999", R.string.error_source_unknown);
}

/**
 * User related errors
 */
enum class UserError(
    override val code: String,
    @StringRes override val messageRes: Int
) : AppError {
    NOT_FOUND("USR_001", R.string.error_user_not_found),

    @Suppress("unused")
    CREATION_FAILED("USR_002", R.string.error_user_creation_failed),

    @Suppress("unused")
    UPDATE_FAILED("USR_003", R.string.error_user_update_failed),

    @Suppress("unused")
    DELETE_FAILED("USR_004", R.string.error_user_delete_failed),

    @Suppress("unused")
    FETCH_FAILED("USR_005", R.string.error_user_fetch_failed),

    @Suppress("unused")
    INVALID_DATA("USR_006", R.string.error_user_invalid_data),
    PERMISSION_DENIED("USR_007", R.string.error_user_permission_denied),
    NETWORK_ERROR("USR_008", R.string.error_user_network_error),
    UNKNOWN_ERROR("USR_999", R.string.error_user_unknown);
}

/**
 * Extension function to get string resource from any AppError
 */
@StringRes
fun AppError.getStringRes(): Int = messageRes

/**
 * Extension function to get error code from any AppError
 */
@Suppress("unused")
fun AppError.getCode(): String = code
