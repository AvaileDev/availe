package io.availe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.availe.components.ChatInputFieldContainer
import io.availe.components.chatMessageThread.ChatThread
import io.availe.config.ClientProvider
import io.availe.network.KtorChatRepository
import io.availe.util.getScreenWidthDp
import io.availe.viewmodels.ChatViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val httpClient = ClientProvider.client

    val listState = rememberLazyListState()
    val screenWidth: Dp = getScreenWidthDp()
    val responsiveWidth: Float = when {
        screenWidth < 600.dp -> .9f
        screenWidth < 840.dp -> .7f
        else -> .63f
    }
    var useInternal: Boolean by remember { mutableStateOf(false) }
    var textContent: String by remember { mutableStateOf("") }
    val chatRepository = remember { KtorChatRepository(httpClient) }
    val chatViewModel = remember { ChatViewModel(chatRepository) }
    var serverPort: String by remember { mutableStateOf(SERVER_PORT.toString()) }

    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChatThread(
                    state = listState,
                    responsiveWidth = responsiveWidth,
                    viewModel = chatViewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Spacer(Modifier.height(8.dp))
                ChatInputFieldContainer(
                    modifier = Modifier.fillMaxWidth(responsiveWidth),
                    textContent = textContent,
                    onTextChange = { textContent = it },
                    useInternal = useInternal,
                    onToggleUseInternal = { useInternal = !useInternal },
                    serverPort = serverPort,
                    onServerPortChange = { serverPort = it },
                    onSend = { message ->
                        chatViewModel.send(
                            message,
                            targetPort = serverPort.toInt()
                        )
                        textContent = ""
                    }
                )
            }
        }
    }
}
