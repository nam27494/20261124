package com.example.data

enum class MealTime {
    MORNING,
    LUNCH,
    DINNER,
}

data class DailyHealthRecord(
    val dateKey: String,
    val morningTaken: Boolean = false,
    val lunchTaken: Boolean = false,
    val dinnerTaken: Boolean = false,
    val waterCups: Int = 0,
) {
    val takenCount: Int
        get() = listOf(morningTaken, lunchTaken, dinnerTaken).count { it }
}
