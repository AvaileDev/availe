package io.availe.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.oikvpqya.compose.fastscroller.VerticalScrollbar
import io.github.oikvpqya.compose.fastscroller.material3.defaultMaterialScrollbarStyle
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter

private val min_height = 100.dp
private val max_height = 400.dp

/**
 * A composable function that renders a responsive chat input field with a scrollbar
 * and adaptive height adjustment based on the amount of text input.
 *
 * @param modifier Optional modifier that applies layout to the Box container.
 */
@Composable
fun ChatInputField(modifier: Modifier = Modifier) {
    var textState by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var textFieldHeight by remember { mutableStateOf(min_height) }
    val density = LocalDensity.current
    val maxHeightPx = with(density) { max_height.toPx() }

    Box(
        modifier
            .heightIn(min = min_height, max = max_height)
            .height(textFieldHeight)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
    ) {
        BasicTextField(
            value = textState,
            onValueChange = { textState = it },
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),

            /* Sets the height of the parent [Box container] explicitly.
            We set the height of the parent from the child because otherwise,
            the scrollbar fully expands the parents even when there's zero text. */
            onTextLayout = { textLayoutResult ->
                val rawPxHeight = textLayoutResult.size.height.toFloat()

                /* Skip px-to-Dp conversion and use MAX_HEIGHT directly if text height exceeds maxHeightPx.
                This avoids a px-to-Dp calculation,
                which can be expensive should a user paste a gigantic amount of text. */
                val newHeight: Dp = if (rawPxHeight > maxHeightPx) {
                    max_height
                } else {
                    (rawPxHeight / density.density).dp
                }
                textFieldHeight = newHeight
            }
        )

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            style = defaultMaterialScrollbarStyle()
        )
    }
}