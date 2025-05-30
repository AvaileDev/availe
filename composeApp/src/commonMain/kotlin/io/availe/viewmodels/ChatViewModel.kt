package io.availe.viewmodels

import io.availe.models.InternalMessage
import io.availe.repositories.KtorChatRepository
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: KtorChatRepository
) {
    private val _messages = MutableStateFlow<List<InternalMessage>>(emptyList())
    val messages: StateFlow<List<InternalMessage>> = _messages.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        scope.launch {
            repository.createSession()
            _messages.value = repository.getHistory()
        }
    }

    fun send(text: String, targetUrl: Url) {
        scope.launch {
            repository.sendMessage(text, targetUrl)
            _messages.value = repository.getHistory()
        }
    }
}
