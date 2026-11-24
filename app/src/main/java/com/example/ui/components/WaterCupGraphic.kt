package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalSeniorColors

@Composable
fun WaterCupGraphic(
    cupNumber: Int,
    isFilled: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSeniorColors.current
    val fillLevel by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "water_cup_fill_$cupNumber",
    )

    val description = if (isFilled) {
        stringResource(R.string.cd_water_cup_filled, cupNumber)
    } else {
        stringResource(R.string.cd_water_cup_empty, cupNumber)
    }

    Canvas(
        modifier = modifier
            .size(width = 52.dp, height = 62.dp)
            .semantics { contentDescription = description },
    ) {
        val w = size.width
        val h = size.height
        val strokeWidth = w * 0.08f
        val topInset = w * 0.08f
        val bottomInset = w * 0.20f
        val topY = h * 0.10f
        val bottomY = h * 0.92f

        val cupPath = Path().apply {
            moveTo(topInset, topY)
            lineTo(w - topInset, topY)
            lineTo(w - bottomInset, bottomY)
            lineTo(bottomInset, bottomY)
            close()
        }

        // Cup interior background
        drawPath(
            path = cupPath,
            color = colors.waterCupEmptyBg,
        )

        // Animated water level clipped inside the glass silhouette
        if (fillLevel > 0.01f) {
            clipPath(path = cupPath, clipOp = ClipOp.Intersect) {
                val waterHeight = (bottomY - topY) * 0.80f * fillLevel
                val waterTopY = bottomY - waterHeight

                drawRect(
                    color = colors.waterCupFill,
                    topLeft = Offset(0f, waterTopY),
                    size = Size(w, bottomY - waterTopY + strokeWidth),
                )

                // Crisp top water meniscus highlight
                drawRoundRect(
                    color = colors.waterCupFillTop,
                    topLeft = Offset(0f, waterTopY),
                    size = Size(w, (h * 0.08f).coerceAtLeast(3f)),
                    cornerRadius = CornerRadius(4f, 4f),
                )

                // Subtle glass shine line on left side
                val shineStart = Offset(w * 0.25f, waterTopY + h * 0.08f)
                val shineEnd = Offset(w * 0.29f, bottomY - h * 0.08f)
                if (shineEnd.y > shineStart.y) {
                    drawLine(
                        color = colors.medDoneBadgeBg.copy(alpha = 0.55f),
                        start = shineStart,
                        end = shineEnd,
                        strokeWidth = w * 0.055f,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }

        // Crisp cup outline
        val outlineColor = if (isFilled) colors.waterPrimary else colors.waterCupEmptyOutline
        drawPath(
            path = cupPath,
            color = outlineColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        // Top glass rim highlight
        drawOval(
            color = outlineColor.copy(alpha = 0.25f),
            topLeft = Rect(
                left = topInset,
                top = topY - h * 0.04f,
                right = w - topInset,
                bottom = topY + h * 0.04f,
            ).topLeft,
            size = Size(w - topInset * 2f, h * 0.08f),
        )
    }
}
