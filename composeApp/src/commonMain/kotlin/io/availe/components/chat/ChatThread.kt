package io.availe.components.chat

import StandardVerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.availe.utils.normaliseUrl
import io.availe.viewmodels.ChatViewModel
import io.ktor.http.*
import kotlinx.coroutines.launch

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

    Column(modifier) {
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