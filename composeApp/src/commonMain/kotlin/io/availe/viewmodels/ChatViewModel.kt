package io.availe.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.availe.network.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiMessage(val text: String, val fromAi: Boolean)

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    fun send(text: String, targetPort: Int) {
        if (text.isBlank()) return
        _messages.update { it + UiMessage(text, fromAi = false) }
        viewModelScope.launch {
            val reply = try {
                chatRepository.sendMessage(text, targetPort = targetPort)
            } catch (e: Exception) {
                "Error: ${e.message ?: "Unknown"}"
            }
            _messages.update { it + UiMessage(reply, fromAi = true) }
        }
    }
}
