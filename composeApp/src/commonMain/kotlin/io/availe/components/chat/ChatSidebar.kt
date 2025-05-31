package io.availe.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.availe.viewmodels.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatSidebar(
    viewModel: ChatViewModel,
    closeDrawer: () -> Unit
) {
    val availableSessions by viewModel.availableSessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val isCreatingSession by viewModel.isCreatingSession.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .width(250.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Chat Sessions", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = { viewModel.createSession() },
                enabled = !isCreatingSession
            ) {
                if (isCreatingSession) {
                    CircularProgressIndicator(
                        Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("New")
                }
            }
        }
        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
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
                    onSessionSelected = {
                        viewModel.selectSession(sessionId)
                        closeDrawer()
                    },
                    onSessionDeleted = {
                        coroutineScope.launch {
                            viewModel.deleteSession(sessionId).fold({}, {})
                        }
                    },
                    onSessionRenamed = { newTitle: String ->
                        coroutineScope.launch {
                            viewModel.renameSession(sessionId, newTitle).fold({}, {})
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SessionItem(
    sessionId: String,
    isSelected: Boolean,
    onSessionSelected: () -> Unit,
    onSessionDeleted: () -> Unit,
    onSessionRenamed: (String) -> Unit = {}
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }

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
            onClick = {
                newTitle = ""
                showRenameDialog = true
            },
            modifier = Modifier.padding(start = 4.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Rename", style = MaterialTheme.typography.bodySmall)
        }
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

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Session") },
            text = {
                Column {
                    Text("Enter a new title for this session:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = { Text("New title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            onSessionRenamed(newTitle)
                        }
                        showRenameDialog = false
                    },
                    enabled = newTitle.isNotBlank()
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
