package com.grupo03.solea.presentation.states.screens

/**
 * UI state for the home screen.
 *
 * Manages the state of UI elements specific to the home screen, such as
 * the floating action button state and selected movement details.
 *
 * @property fabExpanded Whether the floating action button is expanded showing options
 * @property selectedMovement The movement item selected for detailed view, null if none selected
 */
data class HomeScreenState(
    val fabExpanded: Boolean = false,
    val selectedMovement: HistoryMovementItem? = null
)
