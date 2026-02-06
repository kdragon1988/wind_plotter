package com.example.windplotter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object TacticalUi {
    val bgTop = Color(0xFF050B10)
    val bgMid = Color(0xFF04090D)
    val bgBottom = Color(0xFF060C12)
    val panel = Color(0xCC0D1720)
    val panelTint = Color(0xCC101E2A)
    val border = Color(0x8836D9F8)
    val accent = Color(0xFF33E6FF)
    val text = Color(0xFFE9F7FF)
    val muted = Color(0xFF8FA7B8)
    val good = Color(0xFF57E9B2)
    val warn = Color(0xFFFFB347)
    val danger = Color(0xFFFF6079)
}

fun Modifier.tacticalBackground(): Modifier {
    return this.background(
        Brush.verticalGradient(
            listOf(TacticalUi.bgTop, TacticalUi.bgMid, TacticalUi.bgBottom)
        )
    )
}

@Composable
fun TacticalPanel(
    title: String,
    modifier: Modifier = Modifier,
    bodyPadding: Dp = 10.dp,
    titleBottomSpacing: Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TacticalUi.panel),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, TacticalUi.border, RoundedCornerShape(12.dp))
                .background(TacticalUi.panelTint.copy(alpha = 0.35f))
                .padding(bodyPadding)
        ) {
            Text(
                text = title,
                color = TacticalUi.accent,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(titleBottomSpacing))
            content()
        }
    }
}

@Composable
fun TacticalActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = TacticalUi.accent,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = TacticalUi.text
        )
    ) {
        Text(
            text = label,
            color = TacticalUi.text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
