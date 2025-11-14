package com.grupo03.solea.ui.navigation

object AppRoutes {
    const val PREFIX = "main"
    const val HOME = "home"
    const val HISTORY = "history"
    const val SAVINGS = "savings"
    const val SHOPPING_LIST = "shopping_list"
    const val SETTINGS = "settings"
    const val STATISTICS = "statistics"

    const val NEW_MOVEMENT = "new_movement"
    const val NEW_CATEGORY = "new_category"

    // Settings
    const val BUDGET_LIMITS = "budget_limits"
    const val EDIT_BUDGET = "edit_budget"

    // Savings (Corrected to follow the existing pattern)
    const val GOAL_MANAGEMENT = "goal_management"
    const val ADD_EDIT_GOAL = "edit_goal" // Single route for create/edit form, like EDIT_BUDGET

    // Scanner
    const val SCAN_RECEIPT = "scan_receipt"
    const val LOADING_SCAN = "loading_scan"
    const val EDIT_SCANNED_RECEIPT = "edit_scanned_receipt"

    // Voice Note
    const val AUDIO_ANALYSIS = "audio_analysis"
    const val EDIT_VOICE_NOTE = "edit_voice_note"
}

object AuthRoutes {
    const val PREFIX = "auth"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
}
