package com.grupo03.solea.presentation.states

import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType

object CoreState {
    data class State(
        val movements: List<Movement> = emptyList(),
        val movementTypes: List<MovementType> = emptyList(),
        val isLoading: Boolean = false,
        val homeScreenState: HomeScreenState = HomeScreenState(),
    )

    data class HomeScreenState(
        val balance: Double = 0.0,
        val income: Double = 0.0,
        val outcome: Double = 0.0,
        val activeSheet: Boolean = false,
        val movementSet: List<Pair<Movement, MovementType>> = emptyList(),
    )
}