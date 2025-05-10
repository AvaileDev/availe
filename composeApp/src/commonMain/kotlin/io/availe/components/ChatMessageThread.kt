package io.availe.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ChatMessageThread(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(1000) { index ->
            Card {
                Text("Hello #$index")
            }
        }
    }
}