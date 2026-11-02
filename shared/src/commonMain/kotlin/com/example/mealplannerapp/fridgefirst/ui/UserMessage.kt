package com.example.mealplannerapp.fridgefirst.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** A one-time message for a snackbar. The id makes repeated identical texts distinct. */
data class UserMessage(val id: Long, val text: String)

/** Holds the latest [UserMessage] for a ViewModel. Plain Kotlin so ViewModels stay Compose-free. */
class MessageHolder {
    private var nextId = 1L
    private val _current = MutableStateFlow<UserMessage?>(null)
    val current: StateFlow<UserMessage?> = _current.asStateFlow()

    fun show(text: String) {
        _current.value = UserMessage(nextId++, text)
    }

    /** Clears the message only if it is still the one that was shown. */
    fun shown(id: Long) {
        _current.update { if (it?.id == id) null else it }
    }
}
