package io.availe.components.chatMessageThread

import StandardVerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ChatThreadContainerWithScrollbar(
    lazyListState: LazyListState,
    responsiveWidth: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        ChatThread(
            state = lazyListState,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(responsiveWidth)
                .fillMaxHeight()
        )

        StandardVerticalScrollbar(lazyListState, Modifier.align(Alignment.CenterEnd))
    }
}
