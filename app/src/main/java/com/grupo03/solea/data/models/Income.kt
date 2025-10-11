package com.grupo03.solea.data.models

data class Income(
    val id: String = "",
    val movementId: String = "",
)

data class IncomeDetails(
    val income: Income = Income(),
    val movement: Movement = Movement(),
)
