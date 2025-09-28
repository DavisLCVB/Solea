package com.grupo03.solea.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.grupo03.solea.data.repositories.MovementsRepository

class MovementsViewModel(
    private val movementsRepository: MovementsRepository
) : ViewModel() {
}