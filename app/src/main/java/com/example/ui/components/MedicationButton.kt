package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalSeniorColors

@Composable
fun MedicationButton(
    label: String,
    icon: ImageVector,
    iconContentDescription: String,
    iconTint: Color,
    isTaken: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSeniorColors.current
    val haptic = LocalHapticFeedback.current

    val containerColor by animateColorAsState(
        targetValue = if (isTaken) colors.medDoneBg else colors.ivoryCard,
        animationSpec = tween(durationMillis = 220),
        label = "med_bg_$testTag",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isTaken) colors.medDoneBorder else colors.warmBorder,
        animationSpec = tween(durationMillis = 220),
        label = "med_border_$testTag",
    )
    val titleColor by animateColorAsState(
        targetValue = if (isTaken) Color.White else colors.inkPrimary,
        animationSpec = tween(durationMillis = 220),
        label = "med_title_$testTag",
    )

    val takenLabel = stringResource(R.string.medication_taken)
    val pendingLabel = stringResource(R.string.medication_not_taken)

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 96.dp)
            .testTag(testTag)
            .semantics {
                role = Role.Button
                stateDescription = if (isTaken) takenLabel else pendingLabel
            },
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = BorderStroke(width = 2.dp, color = borderColor),
        shadowElevation = if (isTaken) 4.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            color = if (isTaken) {
                                Color.White.copy(alpha = 0.22f)
                            } else {
                                iconTint.copy(alpha = 0.14f)
                            },
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconContentDescription,
                        tint = if (isTaken) Color.White else iconTint,
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineMedium,
                    color = titleColor,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            AnimatedContent(
                targetState = isTaken,
                label = "med_badge_$testTag",
            ) { checked ->
                if (checked) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colors.medDoneBadgeBg,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = stringResource(R.string.cd_check_done),
                                tint = colors.medDoneBadgeText,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = takenLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.medDoneBadgeText,
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colors.medPendingBadgeBg,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = colors.inkSecondary,
                                modifier = Modifier.size(26.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pendingLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.inkSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}
