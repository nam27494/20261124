package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DataStoreDailyHealthRepository
import com.example.data.MealTime
import com.example.ui.SeniorCareScreen
import com.example.ui.SeniorCareViewModel
import com.example.ui.theme.SeniorCareTheme
import java.io.File
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SeniorCareRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tempFiles = mutableListOf<File>()

    @After
    fun tearDown() {
        testScope.cancel()
        tempFiles.forEach { it.delete() }
    }

    private fun createTempDataStoreFile(): File {
        val file = File.createTempFile("senior_care_test_", ".preferences_pb")
        file.delete()
        tempFiles.add(file)
        return file
    }

    @Test
    fun `medication and water actions persist across repository recreation`() = runBlocking {
        val file = createTempDataStoreFile()
        val fixedDate = Date(1794912000000L)
        val dataStore1 = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { file },
        )
        val repo1 = DataStoreDailyHealthRepository(dataStore1) { fixedDate }

        repo1.toggleMedication(MealTime.MORNING)
        repo1.toggleMedication(MealTime.DINNER)
        repo1.incrementWater()
        repo1.incrementWater()
        repo1.incrementWater()

        val saved = repo1.observeTodayRecord().first()
        assertTrue(saved.morningTaken)
        assertFalse(saved.lunchTaken)
        assertTrue(saved.dinnerTaken)
        assertEquals(3, saved.waterCups)

        val repo2 = DataStoreDailyHealthRepository(dataStore1) { fixedDate }
        val restored = repo2.observeTodayRecord().first()
        assertTrue(restored.morningTaken)
        assertFalse(restored.lunchTaken)
        assertTrue(restored.dinnerTaken)
        assertEquals(3, restored.waterCups)

        val nextDay = Date(fixedDate.time + 24 * 60 * 60 * 1000L)
        val repoNextDay = DataStoreDailyHealthRepository(dataStore1) { nextDay }
        val freshRecord = repoNextDay.observeTodayRecord().first()
        assertFalse(freshRecord.morningTaken)
        assertFalse(freshRecord.lunchTaken)
        assertFalse(freshRecord.dinnerTaken)
        assertEquals(0, freshRecord.waterCups)
    }

    @Test
    fun `automatic midnight rollover resets open viewmodel and persists clean state`() {
        val file = createTempDataStoreFile()
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { file },
        )
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 17, 23, 59, 55)
            set(Calendar.MILLISECOND, 0)
        }
        var currentFakeTime = cal.time

        val repository = DataStoreDailyHealthRepository(dataStore) { currentFakeTime }
        assertEquals(5050L, repository.millisUntilNextMidnight())

        val midnightTickChannel = Channel<Unit>(Channel.UNLIMITED)
        val viewModel = SeniorCareViewModel(
            repository = repository,
            waitUntilNextCheck = { midnightTickChannel.receive() },
        )

        composeTestRule.setContent {
            val state = viewModel.uiState.collectAsStateWithLifecycle().value
            SeniorCareTheme {
                SeniorCareScreen(
                    uiState = state,
                    onToggleMedication = viewModel::toggleMedication,
                    onIncrementWater = viewModel::incrementWater,
                    onDecrementWater = viewModel::decrementWater,
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("med_morning_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("med_lunch_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("water_add_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("water_add_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3_000L) {
            ShadowLooper.idleMainLooper()
            val s = viewModel.uiState.value.record
            s.morningTaken && s.lunchTaken && s.waterCups == 2
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("water_count_text").performScrollTo().assertTextEquals("2잔")

        // Advance clock past midnight to 2026-11-18 00:00:01 and fire the midnight watcher tick
        currentFakeTime = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 18, 0, 0, 1)
            set(Calendar.MILLISECOND, 0)
        }.time
        midnightTickChannel.trySend(Unit)

        composeTestRule.waitUntil(timeoutMillis = 3_000L) {
            ShadowLooper.idleMainLooper()
            val s = viewModel.uiState.value
            s.record.dateKey == "2026-11-18" &&
                !s.record.morningTaken &&
                !s.record.lunchTaken &&
                s.record.waterCups == 0
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("date_header_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("water_count_text").performScrollTo().assertTextEquals("0잔")
        assertTrue(viewModel.uiState.value.displayDate.contains("11월 18일"))

        // Verify DataStore on disk was also reset to clean state for the new day
        val persistedAfterMidnight = runBlocking(Dispatchers.IO) {
            repository.observeTodayRecord().first()
        }
        assertEquals("2026-11-18", persistedAfterMidnight.dateKey)
        assertFalse(persistedAfterMidnight.morningTaken)
        assertFalse(persistedAfterMidnight.lunchTaken)
        assertEquals(0, persistedAfterMidnight.waterCups)
    }

    @Test
    fun `tapping medication buttons and water button updates senior UI`() {
        val file = createTempDataStoreFile()
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { file },
        )
        val repository = DataStoreDailyHealthRepository(dataStore)
        val viewModel = SeniorCareViewModel(repository)

        composeTestRule.setContent {
            val state = viewModel.uiState.collectAsStateWithLifecycle().value
            SeniorCareTheme {
                SeniorCareScreen(
                    uiState = state,
                    onToggleMedication = viewModel::toggleMedication,
                    onIncrementWater = viewModel::incrementWater,
                    onDecrementWater = viewModel::decrementWater,
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("date_header_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("med_morning_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("med_lunch_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("med_dinner_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("water_count_text").performScrollTo().assertTextEquals("0잔")

        composeTestRule.onNodeWithTag("med_morning_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3_000L) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.record.morningTaken
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("복용 완료").assertIsDisplayed()

        composeTestRule.onNodeWithTag("water_add_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("water_add_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3_000L) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.record.waterCups == 2
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("water_count_text").performScrollTo().assertTextEquals("2잔")

        composeTestRule.onNodeWithTag("water_undo_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 3_000L) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.record.waterCups == 1
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("water_count_text").performScrollTo().assertTextEquals("1잔")
    }
}
