package io.availe.components.chatMessageThread

import StandardVerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.availe.viewmodels.ChatViewModel

@Composable
fun ChatThread(
    state: LazyListState,
    responsiveWidth: Float,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages.size) { idx ->
                Box(modifier = Modifier.fillMaxWidth(responsiveWidth)) {
                    ChatThreadMessageRow(messages[idx])
                }
            }
        }

        StandardVerticalScrollbar(
            listState = state,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
