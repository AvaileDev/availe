package io.availe.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import arrow.core.Either
import io.availe.viewmodels.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatSidebar(
    viewModel: ChatViewModel,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val availableSessions by viewModel.availableSessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val isCreatingSession by viewModel.isCreatingSession.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val sidebarWidth = 250.dp
    val collapsedWidth = 48.dp

    val sidebarAlpha by animateFloatAsState(targetValue = if (isExpanded) 1f else 0.8f)

    Row(modifier = modifier.fillMaxHeight()) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) {
            Box(
                modifier = Modifier
                    .width(sidebarWidth)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .alpha(sidebarAlpha)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header with title and new chat button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Chat Sessions",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { viewModel.createSession() },
                            enabled = !isCreatingSession,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            if (isCreatingSession) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("New")
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    // Session list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        items(availableSessions) { sessionId ->
                            SessionItem(
                                sessionId = sessionId,
                                isSelected = sessionId == currentSessionId,
                                onSessionSelected = { viewModel.selectSession(sessionId) },
                                onSessionDeleted = { 
                                    coroutineScope.launch {
                                        viewModel.deleteSession(sessionId)
                                            .fold(
                                                { error -> 
                                                    // Handle error (could show a snackbar or other UI feedback)
                                                    println("Error deleting session: ${error.message}")
                                                },
                                                { /* Session deleted successfully */ }
                                            )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Collapse/expand button
        Box(
            modifier = Modifier
                .width(collapsedWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onToggleExpanded() }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isExpanded) "<" else ">",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SessionItem(
    sessionId: String,
    isSelected: Boolean,
    onSessionSelected: () -> Unit,
    onSessionDeleted: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.small)
            .clickable { onSessionSelected() }
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Session ${sessionId.take(8)}...",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) 
                MaterialTheme.colorScheme.onPrimaryContainer 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        TextButton(
            onClick = { showDeleteConfirmation = true },
            modifier = Modifier.padding(start = 4.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Delete", style = MaterialTheme.typography.bodySmall)
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Session") },
            text = { Text("Are you sure you want to delete this session?") },
            confirmButton = {
                Button(
                    onClick = {
                        onSessionDeleted()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
