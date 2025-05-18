package io.availe.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.availe.viewmodels.UiMessage

// Decides whether to show a user or AI message
@Composable
fun ChatThreadMessageRow(message: UiMessage) {
    val isAi = message.fromAi

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
    ) {
        if (isAi) {
            ChatThreadAiText(message.text)
        } else {
            ChatThreadUserBubble(message.text)
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
        modifier = Modifier
            .fillMaxWidth(0.65f)
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
