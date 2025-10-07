package com.grupo03.solea.presentation.states.screens

data class HomeScreenState(
    val fabExpanded: Boolean = false,
    val selectedMovement: HistoryMovementItem? = null
)
