package io.availe.components.chatMessageThread

import StandardVerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.availe.util.forwardScrollTo
import io.availe.viewmodels.ChatViewModel

@Composable
fun ChatThreadContainerWithScrollbar(
    lazyListState: LazyListState,
    responsiveWidth: Float,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    Box(
        modifier
            .fillMaxWidth()
            .forwardScrollTo(lazyListState)
    ) {
        ChatThread(
            state = lazyListState,
            messages = messages,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(responsiveWidth)
                .fillMaxHeight()
        )
        StandardVerticalScrollbar(
            listState = lazyListState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
