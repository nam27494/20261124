package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyHealthRecord
import com.example.data.DailyHealthRepository
import com.example.data.MealTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SeniorCareUiState(
    val displayDate: String = "",
    val record: DailyHealthRecord = DailyHealthRecord(dateKey = ""),
    val isLoaded: Boolean = false,
)

class SeniorCareViewModel(
    private val repository: DailyHealthRepository,
    private val waitUntilNextCheck: suspend (Long) -> Unit = { millis -> delay(millis) },
) : ViewModel() {

    private val dateTick = MutableStateFlow(0L)

    val uiState: StateFlow<SeniorCareUiState> = combine(
        repository.observeTodayRecord(),
        dateTick,
    ) { record, _ ->
        val currentKey = repository.currentDateKey()
        val effectiveRecord = if (record.dateKey == currentKey) {
            record
        } else {
            DailyHealthRecord(dateKey = currentKey)
        }
        SeniorCareUiState(
            displayDate = repository.currentDisplayDate(),
            record = effectiveRecord,
            isLoaded = true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SeniorCareUiState(
            displayDate = repository.currentDisplayDate(),
            record = DailyHealthRecord(dateKey = repository.currentDateKey()),
            isLoaded = false,
        ),
    )

    init {
        refreshDate()
        startMidnightWatcher()
    }

    fun toggleMedication(mealTime: MealTime) {
        viewModelScope.launch {
            repository.toggleMedication(mealTime)
        }
    }

    fun incrementWater() {
        viewModelScope.launch {
            repository.incrementWater()
        }
    }

    fun decrementWater() {
        viewModelScope.launch {
            repository.decrementWater()
        }
    }

    fun refreshDate() {
        viewModelScope.launch {
            repository.syncDateAndResetIfNeeded()
            dateTick.value += 1L
        }
    }

    private fun startMidnightWatcher() {
        viewModelScope.launch {
            var lastKnownDateKey = repository.currentDateKey()
            while (isActive) {
                val millisLeft = repository.millisUntilNextMidnight()
                waitUntilNextCheck(minOf(millisLeft, MAX_MIDNIGHT_POLL_INTERVAL_MS))
                val currentKey = repository.currentDateKey()
                if (currentKey != lastKnownDateKey) {
                    lastKnownDateKey = currentKey
                    repository.syncDateAndResetIfNeeded()
                    dateTick.value += 1L
                }
            }
        }
    }

    companion object {
        private const val MAX_MIDNIGHT_POLL_INTERVAL_MS = 60_000L
    }

    class Factory(
        private val repository: DailyHealthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SeniorCareViewModel::class.java)) {
                return SeniorCareViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
