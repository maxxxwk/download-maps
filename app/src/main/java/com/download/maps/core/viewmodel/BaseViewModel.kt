package com.download.maps.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

abstract class BaseViewModel<INTENT : Any, MUTATION : Any, STATE : Any>(
    initialState: STATE
) : ViewModel() {
    private val intents = Channel<INTENT>(Channel.BUFFERED)

    @Suppress("MagicNumber")
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<STATE> = intents
        .receiveAsFlow()
        .flatMapMerge(transform = ::executeIntent)
        .scan(initialState, ::reduceState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = initialState
        )

    protected fun onIntent(intent: INTENT) {
        intents.trySend(intent)
    }

    protected abstract fun executeIntent(intent: INTENT): Flow<MUTATION>

    protected abstract fun reduceState(currentSTATE: STATE, mutation: MUTATION): STATE
}
