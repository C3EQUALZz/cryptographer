package com.example.cryptographer.presentation.encoding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptographer.domain.text.valueobjects.TextEncoding
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for text encoding conversion screen.
 *
 * Responsibilities:
 * - Manages UI state (input text, selected encoding, converted results)
 * - Delegates business logic to Presenter
 * - Handles coroutine scoping for async operations
 */
@HiltViewModel
class EncodingViewModel @Inject constructor(
    private val presenter: EncodingPresenter
) : ViewModel() {
    private val _uiState = MutableStateFlow(EncodingUiState())
    val uiState: StateFlow<EncodingUiState> = _uiState.asStateFlow()

    /**
     * Updates the input text.
     */
    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(
            inputText = text,
            error = null
        )
        // Auto-convert when text changes
        if (text.isNotBlank()) {
            convertText()
        } else {
            _uiState.value = _uiState.value.copy(
                utf8Result = "",
                asciiResult = "",
                base64Result = ""
            )
        }
    }

    /**
     * Updates the selected encoding (for manual conversion trigger).
     */
    fun selectEncoding(encoding: TextEncoding) {
        _uiState.value = _uiState.value.copy(
            selectedEncoding = encoding,
            error = null
        )
        convertText()
    }

    /**
     * Converts text to all encodings.
     */
    fun convertText() {
        val inputText = _uiState.value.inputText
        if (inputText.isBlank()) {
            _uiState.value = _uiState.value.copy(
                utf8Result = "",
                asciiResult = "",
                base64Result = ""
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Convert to all encodings in parallel
            val utf8Result = presenter.convertText(inputText, TextEncoding.UTF8)
            val asciiResult = presenter.convertText(inputText, TextEncoding.ASCII)
            val base64Result = presenter.convertText(inputText, TextEncoding.BASE64)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                utf8Result = utf8Result.getOrNull() ?: "",
                asciiResult = asciiResult.getOrNull() ?: "",
                base64Result = base64Result.getOrNull() ?: "",
                error = utf8Result.exceptionOrNull()?.message
                    ?: asciiResult.exceptionOrNull()?.message
                    ?: base64Result.exceptionOrNull()?.message
            )
        }
    }

    /**
     * Clears all input and results.
     */
    fun clearAll() {
        _uiState.value = EncodingUiState()
    }
}

/**
 * UI state for encoding conversion screen.
 */
data class EncodingUiState(
    val inputText: String = "",
    val selectedEncoding: TextEncoding = TextEncoding.UTF8,
    val utf8Result: String = "",
    val asciiResult: String = "",
    val base64Result: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

