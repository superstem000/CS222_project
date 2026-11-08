package com.example.mealplannerapp.fridgefirst.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** One-time snackbar message. */
data class UserMessage(val id: Long, val text: String)

class MessageHolder {
    private var nextId = 1L
    private val _current = MutableStateFlow<UserMessage?>(null)
    val current: StateFlow<UserMessage?> = _current.asStateFlow()

    fun show(text: String) {
        _current.value = UserMessage(nextId++, text)
    }

    fun shown(id: Long) {
        _current.update { if (it?.id == id) null else it }
    }
}
