package io.availe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.availe.components.chat.ChatInputFieldContainer
import io.availe.components.chat.ChatThread
import io.availe.config.ClientProvider
import io.availe.network.KtorChatRepository
import io.availe.util.getScreenWidthDp
import io.availe.viewmodels.ChatViewModel
import io.ktor.http.*
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
    var textContent: String by remember { mutableStateOf("") }
    val chatRepository = remember { KtorChatRepository(httpClient) }
    val chatViewModel = remember { ChatViewModel(chatRepository) }
    var targetUrl: String by remember { mutableStateOf("http://localhost:8080/nlip") }

    val snackbarHostState = remember { SnackbarHostState() }

    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.zIndex(1f).fillMaxWidth()

                )
            }
        ) { innerPadding ->
            Surface(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
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
                        targetUrl = targetUrl,
                        onTargetUrlChange = { text, _ -> targetUrl = text },
                        snackbarHostState = snackbarHostState,
                        onSend = { message, url ->
                            chatViewModel.send(
                                message,
                                targetUrl = Url(targetUrl)
                            )
                            textContent = ""
                        }
                    )

                }
            }
        }
    }
}
