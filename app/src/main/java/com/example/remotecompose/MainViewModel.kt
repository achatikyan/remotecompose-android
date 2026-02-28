@file:SuppressLint("RestrictedApiAndroidX")

package com.example.remotecompose

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.remotecompose.data.remote.RemoteConfigFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val documentBytes: ByteArray? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val lastActionMessage: String = "No interaction yet",
    val lastUpdated: Long = 0L,
) {
    companion object {
        private const val BASE = "https://api.github.com/repos/achatikyan/remotecompose/contents"

        fun configUrlForScreen(screenId: String): String {
            return if (screenId == "home") "$BASE/config.rc"
            else "$BASE/config_$screenId.rc"
        }
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var configUrl: String = MainUiState.configUrlForScreen("home")

    init {
        loadDocument()
    }

    fun setConfigUrl(url: String) {
        if (url == configUrl && _uiState.value.documentBytes != null) return
        configUrl = url
        loadDocument()
    }

    fun refresh() {
        loadDocument()
    }

    fun onAction(id: Int, metadata: String?) {
        val message = "Action (ID: $id, Data: $metadata)"
        _uiState.update { it.copy(lastActionMessage = message) }
    }

    private fun loadDocument() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = RemoteConfigFetcher.fetchDocument(configUrl)
                result.fold(
                    onSuccess = { bytes ->
                        val changed = !bytes.contentEquals(_uiState.value.documentBytes)
                        val updatedTime = if (changed) System.currentTimeMillis() else _uiState.value.lastUpdated
                        _uiState.update { it.copy(isLoading = false, documentBytes = bytes, lastUpdated = updatedTime) }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message ?: "Unknown error",
                                documentBytes = null,
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load layout",
                        documentBytes = null,
                    )
                }
            }
        }
    }
}
