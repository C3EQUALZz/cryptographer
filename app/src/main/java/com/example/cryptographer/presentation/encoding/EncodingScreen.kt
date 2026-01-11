package com.example.cryptographer.presentation.encoding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import com.example.cryptographer.presentation.encoding.components.EncodingClearButton
import com.example.cryptographer.presentation.encoding.components.EncodingErrorCard
import com.example.cryptographer.presentation.encoding.components.EncodingInfoCard
import com.example.cryptographer.presentation.encoding.components.EncodingInputCard
import com.example.cryptographer.presentation.encoding.components.EncodingResultsSection
import com.example.cryptographer.presentation.encoding.components.EncodingScreenTitle

/**
 * Screen for converting text between different encodings (UTF-8, ASCII, BASE64).
 * Displays results for all encodings simultaneously for easy comparison.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncodingScreen(viewModel: EncodingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboard.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EncodingScreenTitle()
        EncodingInfoCard()
        EncodingInputCard(
            inputText = uiState.inputText,
            isLoading = uiState.isLoading,
            onInputChange = { viewModel.updateInputText(it) },
        )
                EncodingResultsSection(
                    uiState = uiState,
                    clipboard = clipboard,
                )
        uiState.error?.let { error ->
            EncodingErrorCard(error = error)
        }
        EncodingClearButton(onClearClick = { viewModel.clearAll() })
    }
}
