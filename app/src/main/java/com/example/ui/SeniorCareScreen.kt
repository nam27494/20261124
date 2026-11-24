package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DailyHealthRecord
import com.example.data.MealTime
import com.example.ui.components.MedicationButton
import com.example.ui.components.WaterCupGraphic
import com.example.ui.theme.LocalSeniorColors

private const val DAILY_WATER_CUP_DISPLAY_GOAL = 8

@Composable
fun SeniorCareScreen(
    uiState: SeniorCareUiState,
    onToggleMedication: (MealTime) -> Unit,
    onIncrementWater: () -> Unit,
    onDecrementWater: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSeniorColors.current
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.ivoryBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            val isWideLayout = maxWidth >= 840.dp

            if (isWideLayout) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 1120.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    DateHeaderCard(displayDate = uiState.displayDate)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        MedicationSection(
                            record = uiState.record,
                            onToggleMedication = onToggleMedication,
                            modifier = Modifier.weight(1f),
                        )

                        WaterIntakeSection(
                            waterCups = uiState.record.waterCups,
                            onIncrementWater = onIncrementWater,
                            onDecrementWater = onDecrementWater,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(26.dp),
                ) {
                    DateHeaderCard(displayDate = uiState.displayDate)

                    MedicationSection(
                        record = uiState.record,
                        onToggleMedication = onToggleMedication,
                    )

                    WaterIntakeSection(
                        waterCups = uiState.record.waterCups,
                        onIncrementWater = onIncrementWater,
                        onDecrementWater = onDecrementWater,
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DateHeaderCard(
    displayDate: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSeniorColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("date_header_card"),
        shape = RoundedCornerShape(24.dp),
        color = colors.ivoryHeader,
        border = BorderStroke(width = 1.5.dp, color = colors.warmBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = colors.inkSecondary,
                    modifier = Modifier.size(26.dp),
                )
                Text(
                    text = stringResource(R.string.today_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.inkSecondary,
                )
            }

            Text(
                text = displayDate,
                style = MaterialTheme.typography.headlineLarge,
                color = colors.inkPrimary,
                modifier = Modifier.testTag("date_header_text"),
            )
        }
    }
}

@Composable
private fun MedicationSection(
    record: DailyHealthRecord,
    onToggleMedication: (MealTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSeniorColors.current
    val allTaken = record.takenCount == 3

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.medication_section_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.inkPrimary,
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = if (allTaken) colors.medDoneBg else colors.ivoryHeader,
            ) {
                Text(
                    text = if (allTaken) {
                        stringResource(R.string.medication_all_done)
                    } else {
                        stringResource(R.string.medication_progress_summary, record.takenCount)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (allTaken) Color.White else colors.inkSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        MedicationButton(
            label = stringResource(R.string.medication_morning),
            icon = Icons.Rounded.WbSunny,
            iconContentDescription = stringResource(R.string.cd_morning_icon),
            iconTint = colors.morningAccent,
            isTaken = record.morningTaken,
            testTag = "med_morning_button",
            onClick = { onToggleMedication(MealTime.MORNING) },
        )

        MedicationButton(
            label = stringResource(R.string.medication_lunch),
            icon = Icons.Rounded.LightMode,
            iconContentDescription = stringResource(R.string.cd_lunch_icon),
            iconTint = colors.lunchAccent,
            isTaken = record.lunchTaken,
            testTag = "med_lunch_button",
            onClick = { onToggleMedication(MealTime.LUNCH) },
        )

        MedicationButton(
            label = stringResource(R.string.medication_dinner),
            icon = Icons.Rounded.Bedtime,
            iconContentDescription = stringResource(R.string.cd_dinner_icon),
            iconTint = colors.dinnerAccent,
            isTaken = record.dinnerTaken,
            testTag = "med_dinner_button",
            onClick = { onToggleMedication(MealTime.DINNER) },
        )
    }
}

@Composable
private fun WaterIntakeSection(
    waterCups: Int,
    onIncrementWater: () -> Unit,
    onDecrementWater: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSeniorColors.current
    val haptic = LocalHapticFeedback.current
    val goalReached = waterCups >= DAILY_WATER_CUP_DISPLAY_GOAL

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.ivoryCard,
        border = BorderStroke(width = 2.dp, color = colors.waterPrimary.copy(alpha = 0.28f)),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.water_section_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.inkPrimary,
                    )
                    Text(
                        text = if (goalReached) {
                            stringResource(R.string.water_goal_achieved)
                        } else {
                            stringResource(R.string.water_goal_hint)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (goalReached) colors.medDoneBg else colors.inkSecondary,
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = colors.waterLight,
                            shape = RoundedCornerShape(20.dp),
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.water_count_format, waterCups),
                        style = MaterialTheme.typography.displayLarge,
                        color = colors.waterDark,
                        modifier = Modifier.testTag("water_count_text"),
                    )
                }
            }

            // 8 visual cups in 2 spacious rows of 4
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colors.ivoryBackground,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (cupIndex in 1..4) {
                        WaterCupGraphic(
                            cupNumber = cupIndex,
                            isFilled = waterCups >= cupIndex,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (cupIndex in 5..8) {
                        WaterCupGraphic(
                            cupNumber = cupIndex,
                            isFilled = waterCups >= cupIndex,
                        )
                    }
                }
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onIncrementWater()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 84.dp)
                    .testTag("water_add_button"),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.waterPrimary,
                    contentColor = Color.White,
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.WaterDrop,
                    contentDescription = stringResource(R.string.cd_water_drop),
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.water_add_button),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
            }

            if (waterCups > 0) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDecrementWater()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 58.dp)
                        .testTag("water_undo_button"),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, colors.warmBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.inkSecondary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RemoveCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.water_undo_button),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
