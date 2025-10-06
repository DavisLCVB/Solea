package com.grupo03.solea.presentation.viewmodels.screens

import androidx.lifecycle.ViewModel
import com.grupo03.solea.presentation.states.screens.HomeScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _homeState = MutableStateFlow(HomeScreenState())
    val homeState: StateFlow<HomeScreenState> = _homeState.asStateFlow()

    fun onToggleFab() {
        _homeState.value = _homeState.value.copy(fabExpanded = !_homeState.value.fabExpanded)
    }

    fun onCollapseFab() {
        _homeState.value = _homeState.value.copy(fabExpanded = false)
    }
}
