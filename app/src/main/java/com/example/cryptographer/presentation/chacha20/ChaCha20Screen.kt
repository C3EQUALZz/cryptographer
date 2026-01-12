package com.example.cryptographer.presentation.chacha20

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
import com.example.cryptographer.presentation.chacha20.components.ChaCha20ClearButton
import com.example.cryptographer.presentation.chacha20.components.ChaCha20DecryptionSection
import com.example.cryptographer.presentation.chacha20.components.ChaCha20EncryptionSection
import com.example.cryptographer.presentation.chacha20.components.ChaCha20ErrorCard
import com.example.cryptographer.presentation.chacha20.components.ChaCha20InfoCard
import com.example.cryptographer.presentation.chacha20.components.ChaCha20KeySelectionSection
import com.example.cryptographer.presentation.chacha20.components.ChaCha20ScreenTitle

/**
 * Screen for ChaCha20 encryption and decryption.
 * Supports ChaCha20-256 with nonce.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaCha20Screen(viewModel: ChaCha20ViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val availableKeys by viewModel.availableKeys.collectAsState()
    val clipboard = LocalClipboard.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChaCha20ScreenTitle()
        ChaCha20InfoCard()
        ChaCha20KeySelectionSection(
            availableKeys = availableKeys,
            selectedKeyId = uiState.selectedKeyId,
            onKeySelected = { viewModel.selectKey(it) },
        )
        ChaCha20EncryptionSection(
            uiState = uiState,
            onInputChange = { viewModel.updateInputText(it) },
            onEncryptClick = { viewModel.encryptText() },
            clipboard = clipboard,
        )
        ChaCha20DecryptionSection(
            uiState = uiState,
            onEncryptedTextChange = { viewModel.updateEncryptedText(it) },
            onNonceChange = { viewModel.updateNonceText(it) },
            onDecryptClick = { viewModel.decryptText() },
            clipboard = clipboard,
        )
        uiState.error?.let { error ->
            ChaCha20ErrorCard(error = error)
        }
        ChaCha20ClearButton(onClearClick = { viewModel.clearAll() })
    }
}
