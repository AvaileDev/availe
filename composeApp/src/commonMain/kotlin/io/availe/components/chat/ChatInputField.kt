package io.availe.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.availe.utils.normaliseUrl
import io.ktor.http.*
import kotlinx.coroutines.launch

@Composable
fun ChatInputField(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
) {
    val maxLines = 5

    TextFieldWithScrollbar(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier.fillMaxWidth(),
        minLines = 1,
        maxLines = maxLines,
        placeholder = { Text("Type a message...") }
    )
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

@Composable
fun ChatInputFieldContainer(
    modifier: Modifier = Modifier,
    textContent: String,
    onTextChange: (String) -> Unit,
    targetUrl: String,
    onTargetUrlChange: (String, Url?) -> Unit,
    snackbarHostState: SnackbarHostState,
    onSend: (String, Url) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var urlError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.imePadding()) {
        ChatInputField(text = textContent, onTextChange = onTextChange)

        Spacer(Modifier.padding(10.dp))

        UrlInputBox(targetUrl, onTargetUrlChange, error = urlError)

        Spacer(Modifier.padding(10.dp))

        Button(
            onClick = {
                val parsed = try {
                    normaliseUrl(targetUrl, ensureTrailingSlash = true)
                } catch (e: Exception) {
                    null
                }
                if (parsed != null) {
                    urlError = null
                    onSend(textContent, parsed)
                } else {
                    urlError = "Invalid URL"

                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Invalid URL!")
                    }
                }
            },
            enabled = textContent.isNotBlank()
        ) {
            Text("Send")
        }
    }
}