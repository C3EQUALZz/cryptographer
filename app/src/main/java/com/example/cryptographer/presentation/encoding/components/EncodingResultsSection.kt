package com.example.cryptographer.presentation.encoding.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.res.stringResource
import com.example.cryptographer.R
import com.example.cryptographer.presentation.encoding.EncodingUiState

@Composable
fun EncodingResultsSection(uiState: EncodingUiState, clipboard: Clipboard) {
    if (uiState.utf8Result.isNotEmpty() || uiState.inputText.isNotBlank()) {
        EncodingResultCard(
            title = stringResource(R.string.utf8),
            content = uiState.utf8Result.ifEmpty { uiState.inputText },
            clipboard = clipboard,
        )
    }

    if (uiState.asciiResult.isNotEmpty() || uiState.inputText.isNotBlank()) {
        EncodingResultCard(
            title = stringResource(R.string.ascii),
            content = uiState.asciiResult.ifEmpty { "..." },
            clipboard = clipboard,
        )
    }

    if (uiState.base64Result.isNotEmpty() || uiState.inputText.isNotBlank()) {
        EncodingResultCard(
            title = stringResource(R.string.base64),
            content = uiState.base64Result.ifEmpty { "..." },
            clipboard = clipboard,
        )
    }
}
