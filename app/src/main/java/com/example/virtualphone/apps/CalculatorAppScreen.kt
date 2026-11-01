package com.example.virtualphone.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.virtualphone.viewmodel.VirtualPhoneViewModel

@Composable
fun CalculatorAppScreen(
    viewModel: VirtualPhoneViewModel,
    modifier: Modifier = Modifier
) {
    val display by viewModel.calcDisplay.collectAsState()

    val buttons = listOf(
        listOf("C", "±", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Result Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF10192A), RoundedCornerShape(16.dp))
                .padding(20.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = display,
                fontSize = if (display.length > 9) 32.sp else 46.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keypad Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { btn ->
                        val isOp = btn in listOf("÷", "×", "-", "+", "=")
                        val isAction = btn in listOf("C", "±", "%")

                        val bg = when {
                            btn == "=" -> SeveritySecure
                            isOp -> ElectricBlue
                            isAction -> Color(0xFF273859)
                            else -> Color(0xFF162238)
                        }

                        val textColor = when {
                            btn == "=" -> BackgroundDark
                            isOp -> Color.White
                            isAction -> CyanNeon
                            else -> TextPrimary
                        }

                        val weight = if (btn == "0") 2f else 1f

                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(64.dp)
                                .background(bg, CircleShape)
                                .clickable { viewModel.calcPress(btn) }
                                .testTag("calc_btn_$btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = btn,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
