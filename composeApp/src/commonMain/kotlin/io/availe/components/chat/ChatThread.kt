package io.availe.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.availe.components.HeightTracker
import io.availe.components.StandardVerticalScrollbar
import io.availe.models.InternalMessage
import io.availe.viewmodels.ChatViewModel

@Composable
fun ChatThread(
    state: LazyListState,
    responsiveWidth: Float,
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val heightTracker = remember { HeightTracker() }
    LaunchedEffect(messages.size) { heightTracker.resize(messages.size) }
    Box(modifier = modifier.fillMaxSize()) {
        SelectionContainer {
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                itemsIndexed(items = messages, key = { _, m -> m.id }) { index, m ->
                    Box(
                        Modifier
                            .fillMaxWidth(responsiveWidth)
                            .onGloballyPositioned { coords ->
                                heightTracker.updateHeight(index, coords.size.height)
                            }
                    ) {
                        ChatThreadMessageRow(m)
                    }
                }
            }
        }
        StandardVerticalScrollbar(
            listState = state,
            heights = heightTracker,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(8.dp)
        )
    }
}

@Composable
fun ChatThreadMessageRow(message: InternalMessage) {
    val isAi = message.senderRole != InternalMessage.Role.USER

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
    ) {
        if (isAi) {
            ChatThreadAiText(message.nlipMessage.content)
        } else {
            ChatThreadUserBubble(message.nlipMessage.content)
        }
    }
}

@Composable
fun ChatThreadAiText(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .background(Color.Transparent)
            .padding(8.dp),
        textAlign = TextAlign.Start
    )
}

@Composable
fun ChatThreadUserBubble(message: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(0.65f)
    ) {
        Text(
            text = message,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
