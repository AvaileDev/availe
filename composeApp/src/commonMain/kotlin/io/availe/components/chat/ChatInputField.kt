package io.availe.components.chat

import StandardVerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.ktor.http.*

private val MIN_HEIGHT = 100.dp
private val MAX_HEIGHT = 200.dp

@Composable
fun ChatInputField(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
) {
    val scrollState = rememberScrollState()
    var textFieldHeight by remember { mutableStateOf(MIN_HEIGHT) }
    val density = LocalDensity.current
    val maxHeightPx = with(density) { MAX_HEIGHT.toPx() }
    val minHeightPx = with(density) { MIN_HEIGHT.toPx() }

    Box(
        modifier
            .height(textFieldHeight)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .fillMaxSize(),

                /* Sets the height of the parent [Box container] explicitly.
                We set the height of the parent from the child because otherwise,
                the scrollbar fully expands the parents even when there's zero text. */
                onTextLayout = { textLayoutResult ->
                    val rawPxHeight = textLayoutResult.size.height.toFloat()

                    /* Skip px-to-Dp conversion when possible.
                    This can be expensive should a user paste a gigantic amount of text. */
                    val newHeight = when {
                        rawPxHeight >= maxHeightPx -> MAX_HEIGHT
                        rawPxHeight <= minHeightPx -> MIN_HEIGHT
                        else -> (rawPxHeight / density.density).dp
                    }
                    textFieldHeight = newHeight
                }
            )
        }

        StandardVerticalScrollbar(scrollState, Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
fun UrlInputBox(
    targetUrl: String,
    onTargetUrlChange: (String, Url?) -> Unit,
    error: String? = null
) {
    TextField(
        value = targetUrl,
        onValueChange = { input ->
            val cleaned = input.trim()
            val parsedUrl = try {
                Url(cleaned)
            } catch (e: Exception) {
                null
            }
            onTargetUrlChange(input, parsedUrl)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Enter URL") },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Uri
        ),
        isError = error != null
    )
    if (error != null) {
        Text(text = error, color = Color.Red)
    }
}