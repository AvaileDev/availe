package io.availe.components.chatMessageThread

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatThread(
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = state,
        modifier = modifier
    ) {
        items(1000) { index ->
            Card(modifier = Modifier.padding(8.dp)) {
                Text("Hello #$index", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
