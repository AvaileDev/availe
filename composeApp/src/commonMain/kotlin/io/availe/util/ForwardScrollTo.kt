package io.availe.util

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

@Composable
fun Modifier.forwardScrollTo(listState: LazyListState) = composed {
    val proxyState = rememberScrollableState { delta ->
        listState.dispatchRawDelta(delta)
        delta
    }
    scrollable(
        orientation = Orientation.Vertical,
        state = proxyState,
        reverseDirection = true
    )
}
