package com.grupo03.solea.presentation.states

import com.grupo03.solea.data.models.Movement
import com.grupo03.solea.data.models.MovementType
import com.grupo03.solea.utils.ErrorCode

object CoreState {

    enum class HomeContent {
        HOME,
        NEW_MOVEMENT_FORM,
        NEW_MOVEMENT_TYPE_FORM
    }

    data class State(
        val movements: List<Movement> = emptyList(),
        val movementTypes: List<MovementType> = emptyList(),
        val isLoading: Boolean = false,
        val homeScreenState: HomeScreenState = HomeScreenState(),
        val newMovementFormState: NewMovementFormState = NewMovementFormState(),
        val newMovementTypeFormState: NewMovementTypeFormState = NewMovementTypeFormState(),
        val currentContent: HomeContent = HomeContent.HOME,
    )

    data class HomeScreenState(
        val balance: Double = 0.0,
        val income: Double = 0.0,
        val outcome: Double = 0.0,
        val activeSheet: Boolean = false,
        val movementSet: List<Pair<Movement, MovementType>> = emptyList(),
    )

    data class NewMovementFormState(
        val movementAmount: String = "",
        val typeList: List<String> = emptyList(),
        val typeSelected: String = "",
        val note: String = "",
        val isAmountValid: Boolean = true,
        val isTypeValid: Boolean = true,
        val isNoteValid: Boolean = true,
        val errorCode: ErrorCode.Movement? = null,
    )

    data class NewMovementTypeFormState(
        val typeName: String = "",
        val typeDescription: String = "",
        val isNameValid: Boolean = true,
        val isDescriptionValid: Boolean = true,
        val errorCode: ErrorCode.Movement? = null,
    )
}