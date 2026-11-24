package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.dailyHealthDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "daily_health_prefs"
)

interface DailyHealthRepository {
    fun observeTodayRecord(): Flow<DailyHealthRecord>
    suspend fun toggleMedication(mealTime: MealTime)
    suspend fun incrementWater()
    suspend fun decrementWater()
    suspend fun syncDateAndResetIfNeeded()
    fun millisUntilNextMidnight(): Long
    fun currentDateKey(): String
    fun currentDisplayDate(): String
}

class DataStoreDailyHealthRepository(
    private val dataStore: DataStore<Preferences>,
    private val nowProvider: () -> Date = { Date() },
) : DailyHealthRepository {

    override fun currentDateKey(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(nowProvider())
    }

    override fun currentDisplayDate(): String {
        val formatter = SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN)
        return formatter.format(nowProvider())
    }

    override fun millisUntilNextMidnight(): Long {
        val now = nowProvider()
        val nextMidnight = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 50)
        }
        return (nextMidnight.timeInMillis - now.time).coerceAtLeast(250L)
    }

    override fun observeTodayRecord(): Flow<DailyHealthRecord> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs ->
                val today = currentDateKey()
                val savedDate = prefs[KEY_DATE]
                if (savedDate == today) {
                    DailyHealthRecord(
                        dateKey = today,
                        morningTaken = prefs[KEY_MORNING] ?: false,
                        lunchTaken = prefs[KEY_LUNCH] ?: false,
                        dinnerTaken = prefs[KEY_DINNER] ?: false,
                        waterCups = (prefs[KEY_WATER] ?: 0).coerceAtLeast(0),
                    )
                } else {
                    DailyHealthRecord(dateKey = today)
                }
            }

    override suspend fun syncDateAndResetIfNeeded() {
        val today = currentDateKey()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
        }
    }

    override suspend fun toggleMedication(mealTime: MealTime) {
        val today = currentDateKey()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
            val key = when (mealTime) {
                MealTime.MORNING -> KEY_MORNING
                MealTime.LUNCH -> KEY_LUNCH
                MealTime.DINNER -> KEY_DINNER
            }
            val current = prefs[key] ?: false
            prefs[key] = !current
        }
    }

    override suspend fun incrementWater() {
        val today = currentDateKey()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
            val current = (prefs[KEY_WATER] ?: 0).coerceAtLeast(0)
            prefs[KEY_WATER] = current + 1
        }
    }

    override suspend fun decrementWater() {
        val today = currentDateKey()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
            val current = (prefs[KEY_WATER] ?: 0).coerceAtLeast(0)
            prefs[KEY_WATER] = (current - 1).coerceAtLeast(0)
        }
    }

    private fun resetIfNewDay(
        prefs: MutablePreferences,
        today: String,
    ) {
        if (prefs[KEY_DATE] != today) {
            prefs[KEY_DATE] = today
            prefs[KEY_MORNING] = false
            prefs[KEY_LUNCH] = false
            prefs[KEY_DINNER] = false
            prefs[KEY_WATER] = 0
        }
    }

    companion object {
        private val KEY_DATE = stringPreferencesKey("record_date")
        private val KEY_MORNING = booleanPreferencesKey("morning_taken")
        private val KEY_LUNCH = booleanPreferencesKey("lunch_taken")
        private val KEY_DINNER = booleanPreferencesKey("dinner_taken")
        private val KEY_WATER = intPreferencesKey("water_cups")
    }
}
