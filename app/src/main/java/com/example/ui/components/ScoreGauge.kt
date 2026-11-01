package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ScoreGauge(
    score: Int,
    riskRating: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 900),
        label = "score_anim"
    )

    val (scoreColor, bgTint) = when {
        score >= 80 -> SeveritySecure to SeveritySecureBg
        score >= 60 -> SeverityMedium to SeverityMediumBg
        score >= 40 -> SeverityHigh to SeverityHighBg
        else -> SeverityCritical to SeverityCriticalBg
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(1.dp, BorderStroke, RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Circular Score Dial
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(110.dp)
            ) {
                Canvas(modifier = Modifier.size(96.dp)) {
                    val strokeWidth = 9.dp.toPx()
                    // Background track
                    drawCircle(
                        color = Color(0xFF1E293B),
                        style = Stroke(width = strokeWidth)
                    )
                    // Progress arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(scoreColor.copy(alpha = 0.5f), scoreColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "/100",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Status & Risk Rating
            Column(
                modifier = Modifier.weight(1f).padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "SECURITY HEALTH SCORE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .background(bgTint, RoundedCornerShape(8.dp))
                        .border(1.dp, scoreColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = riskRating,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }

                Text(
                    text = when {
                        score >= 80 -> "Robust posture. Pass with minor hygiene."
                        score >= 60 -> "Warnings detected. Fix before mainnet."
                        score >= 40 -> "High risks found. Fund drainage possible."
                        else -> "Critical vulnerabilities! Do not deploy."
                    },
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
