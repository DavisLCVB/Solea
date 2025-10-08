package com.grupo03.solea.presentation.viewmodels.screens

import androidx.lifecycle.ViewModel
import com.grupo03.solea.presentation.states.screens.HistoryMovementItem
import com.grupo03.solea.presentation.states.screens.HomeScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the home screen UI state management.
 *
 * Manages the state of UI elements on the home screen, including the floating action
 * button (FAB) expansion state and movement selection for detail viewing.
 */
class HomeViewModel : ViewModel() {
    /** Home screen UI state including FAB expansion and selected movement */
    private val _homeState = MutableStateFlow(HomeScreenState())
    val homeState: StateFlow<HomeScreenState> = _homeState.asStateFlow()

    /**
     * Toggles the floating action button expansion state.
     *
     * Expands the FAB if collapsed, collapses it if expanded.
     */
    fun onToggleFab() {
        _homeState.value = _homeState.value.copy(fabExpanded = !_homeState.value.fabExpanded)
    }

    /**
     * Collapses the floating action button.
     */
    fun onCollapseFab() {
        _homeState.value = _homeState.value.copy(fabExpanded = false)
    }

    /**
     * Sets the selected movement for detail viewing.
     *
     * @param movement The movement to select, or null to clear selection
     */
    fun onMovementSelected(movement: HistoryMovementItem?) {
        _homeState.value = _homeState.value.copy(selectedMovement = movement)
    }
}
