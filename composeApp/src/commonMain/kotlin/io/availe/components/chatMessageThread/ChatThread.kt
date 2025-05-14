package io.availe.components.chatMessageThread

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.availe.viewmodels.UiMessage

@Composable
fun ChatThread(
    state: LazyListState,
    modifier: Modifier = Modifier,
    messages: List<UiMessage> = emptyList(),
) {
    LazyColumn(
        state = state,
        modifier = modifier
    ) {
        items(messages.size) { index ->
            ChatThreadMessageRow(messages[index])
        }
    }
}
