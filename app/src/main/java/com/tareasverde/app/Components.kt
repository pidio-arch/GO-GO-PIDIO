@file:OptIn(ExperimentalMaterial3Api::class)

package com.tareasverde.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Logo en texto: GO GO en blanco y PIDIO en verde. */
@Composable
fun Brand(fontSize: TextUnit = 16.sp) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White)) { append("GO GO ") }
            withStyle(SpanStyle(color = GreenAccent)) { append("PIDIO") }
        },
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
    )
}

@Composable
fun GreenChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = TextDim,
            selectedContainerColor = GreenAccent,
            selectedLabelColor = BgBlack,
        ),
    )
}

@Composable
fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(18.dp),
        content = content,
    )
}
