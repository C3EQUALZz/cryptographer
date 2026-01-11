package com.example.cryptographer.presentation.encryption

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
import com.example.cryptographer.presentation.encryption.components.DecryptionSection
import com.example.cryptographer.presentation.encryption.components.EncryptionClearButton
import com.example.cryptographer.presentation.encryption.components.EncryptionErrorCard
import com.example.cryptographer.presentation.encryption.components.EncryptionInfoCard
import com.example.cryptographer.presentation.encryption.components.EncryptionScreenTitle
import com.example.cryptographer.presentation.encryption.components.EncryptionSection
import com.example.cryptographer.presentation.encryption.components.KeySelectionSection

/**
 * Screen for encrypting and decrypting text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncryptionScreen(viewModel: EncryptionViewModel) {
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
        EncryptionScreenTitle()
        EncryptionInfoCard()
        KeySelectionSection(
            availableKeys = availableKeys,
            selectedKeyId = uiState.selectedKeyId,
            onKeySelected = { viewModel.selectKey(it) },
        )
        EncryptionSection(
            uiState = uiState,
            onInputChange = { viewModel.updateInputText(it) },
            onEncryptClick = { viewModel.encryptText() },
            clipboard = clipboard,
        )
        DecryptionSection(
            uiState = uiState,
            onEncryptedTextChange = { viewModel.updateEncryptedText(it) },
            onIvChange = { viewModel.updateIvText(it) },
            onDecryptClick = { viewModel.decryptText() },
            clipboard = clipboard,
        )
        uiState.error?.let { error ->
            EncryptionErrorCard(error = error)
        }
        EncryptionClearButton(onClearClick = { viewModel.clearAll() })
    }
}
