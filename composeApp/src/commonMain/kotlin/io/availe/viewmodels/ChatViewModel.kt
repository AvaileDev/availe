package io.availe.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.availe.network.ChatRepository
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class UiMessage(
    val id: String,
    val text: String,
    val fromAi: Boolean,
    val timeStamp: Long,
    val isPending: Boolean = false,
    val isError: Boolean = false
)

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    private fun now() = TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds

    @OptIn(ExperimentalUuidApi::class)
    fun send(text: String, targetUrl: Url) {
        if (text.isBlank()) return
        val userMsgId = Uuid.random().toString()
        _messages.update { list ->
            list + UiMessage(
                id = userMsgId,
                text = text,
                fromAi = false,
                timeStamp = now(),
                isPending = true
            )
        }
        viewModelScope.launch {
            val reply = try {
                chatRepository.sendMessage(text, conversationId = null, targetUrl = targetUrl)
            } catch (e: Exception) {
                _messages.update { list ->
                    list.map { if (it.id == userMsgId) it.copy(isPending = false, isError = true) else it }
                }
                return@launch
            }
            _messages.update { list ->
                list.map { if (it.id == userMsgId) it.copy(isPending = false) else it } +
                        UiMessage(
                            id = Uuid.random().toString(),
                            text = reply,
                            fromAi = true,
                            timeStamp = now()
                        )
            }
        }
    }
}
