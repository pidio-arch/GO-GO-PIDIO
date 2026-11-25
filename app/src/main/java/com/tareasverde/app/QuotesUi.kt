@file:OptIn(ExperimentalLayoutApi::class)

package com.tareasverde.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuotesScreen() {
    var filter by remember { mutableStateOf<QuoteType?>(null) }
    var current by remember { mutableStateOf(Quotes.random()) }
    val list = Quotes.all.filter { filter == null || it.type == filter }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Brand(18.sp)
                Spacer(Modifier.height(10.dp))
                Text("Frases", color = TextMain, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardDark)
                        .border(1.dp, GreenAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(22.dp)
                ) {
                    Icon(
                        Icons.Outlined.FormatQuote,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(current.text, color = TextMain, fontSize = 22.sp, lineHeight = 30.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("— ${current.author}", color = GreenAccent, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { current = Quotes.random(filter) }) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Otra frase", color = GreenAccent)
                    }
                }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GreenChip("Todas", filter == null) { filter = null }
                GreenChip("Motivacionales", filter == QuoteType.MOTIVACIONAL) {
                    filter = QuoteType.MOTIVACIONAL
                }
                GreenChip("Reflexivas", filter == QuoteType.REFLEXIVA) {
                    filter = QuoteType.REFLEXIVA
                }
                GreenChip("Religiosas", filter == QuoteType.RELIGIOSA) {
                    filter = QuoteType.RELIGIOSA
                }
            }
        }
        items(list) { q ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardDark)
                    .padding(16.dp)
            ) {
                Text("“${q.text}”", color = TextMain, fontSize = 16.sp)
                Spacer(Modifier.height(6.dp))
                Text(q.author, color = TextDim, fontSize = 13.sp)
            }
        }
    }
}
