package io.availe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.availe.components.ChatInputField
import io.availe.components.chatMessageThread.ChatThread
import io.availe.network.KtorChatRepository
import io.availe.network.ChatTarget
import io.availe.util.getScreenWidthDp
import io.availe.viewmodels.ChatViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val listState = rememberLazyListState()
    val screenWidth: Dp = getScreenWidthDp()
    val responsiveWidth: Float = when {
        screenWidth < 600.dp -> .9f
        screenWidth < 840.dp -> .7f
        else -> .63f
    }
    var useInternal: Boolean by remember { mutableStateOf(false) }
    var textState: String by remember { mutableStateOf("") }
    val chatRepository = remember { KtorChatRepository(HttpClient(CIO)) }
    val chatViewModel = remember { ChatViewModel(chatRepository) }

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
                ChatInputField(
                    modifier = Modifier.fillMaxWidth(responsiveWidth),
                    text = textState,
                    onTextChange = { textState = it },
                    useInternal = useInternal,
                    onToggleUseInternal = { useInternal = !useInternal },
                    onSend = { message ->
                        chatViewModel.send(message, if (useInternal) ChatTarget.Internal else ChatTarget.External)
                        textState = ""
                    }
                )
            }
        }
    }
}
