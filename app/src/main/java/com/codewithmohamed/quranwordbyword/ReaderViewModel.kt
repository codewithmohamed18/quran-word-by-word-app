package com.codewithmohamed.quranwordbyword

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReaderState(
    val ready: Boolean=false, val page: Int=1, val displayed: Boolean=false,
    val preferences: ReadingPreferences=ReadingPreferences(), val config: NavigationConfig?=null,
    val error: String?=null, val fitRequest: Int=0
)
class ReaderViewModel(application: Application) : AndroidViewModel(application) {
    val engine=PdfEngine(application)
    private val store=ReaderStore(application)
    private val _state=MutableStateFlow(ReaderState())
    val state: StateFlow<ReaderState> = _state.asStateFlow()
    init {
        viewModelScope.launch {
            try {
                store.migrateLegacy(application)
                val initial=store.preferences.first()
                val config=NavigationConfig.load(application)
                engine.open()
                _state.value=ReaderState(ready=true,page=initial.lastPage,preferences=initial,config=config)
                store.preferences.collect { prefs -> _state.update { it.copy(preferences=prefs) } }
            } catch(e: Exception) {
                _state.update { it.copy(error="The included Qur’an could not be opened. Check free storage and restart the app.") }
            }
        }
    }
    fun goTo(page: Int) {
        if(!_state.value.ready) return
        val target=page.coerceIn(1,960)
        _state.update { if(it.page==target) it else it.copy(page=target,displayed=false,error=null) }
    }
    fun displayed(page: Int) {
        if(page!=_state.value.page) return
        _state.update { it.copy(displayed=true,error=null) }
        viewModelScope.launch { store.setPage(page) }
    }
    fun failure(message: String) { _state.update { it.copy(error=message) } }
    fun bookmark() { viewModelScope.launch { store.toggleBookmark(_state.value.page) } }
    fun removeBookmark(page: Int) { if(page in _state.value.preferences.bookmarks) viewModelScope.launch { store.toggleBookmark(page) } }
    fun theme(mode: String) { viewModelScope.launch { store.setTheme(mode) } }
    fun keepAwake(value: Boolean) { viewModelScope.launch { store.setKeepAwake(value) } }
    fun fit() { _state.update { it.copy(fitRequest=it.fitRequest+1) } }
    override fun onCleared() { engine.close(); super.onCleared() }
}
