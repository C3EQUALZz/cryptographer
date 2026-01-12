package com.example.cryptographer.presentation.aes

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
import com.example.cryptographer.presentation.aes.components.AesClearButton
import com.example.cryptographer.presentation.aes.components.AesDecryptionSection
import com.example.cryptographer.presentation.aes.components.AesEncryptionSection
import com.example.cryptographer.presentation.aes.components.AesErrorCard
import com.example.cryptographer.presentation.aes.components.AesInfoCard
import com.example.cryptographer.presentation.aes.components.AesKeyLengthSelection
import com.example.cryptographer.presentation.aes.components.AesKeySelectionSection
import com.example.cryptographer.presentation.aes.components.AesScreenTitle

/**
 * Screen for AES encryption and decryption.
 * Supports AES-128, AES-192, and AES-256 with key length selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AesScreen(viewModel: AesViewModel) {
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
        AesScreenTitle()
        AesInfoCard()
        AesKeyLengthSelection(
            selectedLength = uiState.selectedKeyLength,
            onLengthSelected = { viewModel.selectKeyLength(it) },
        )
        AesKeySelectionSection(
            availableKeys = availableKeys,
            selectedKeyId = uiState.selectedKeyId,
            onKeySelected = { viewModel.selectKey(it) },
        )
        AesEncryptionSection(
            uiState = uiState,
            onInputChange = { viewModel.updateInputText(it) },
            onEncryptClick = { viewModel.encryptText() },
            clipboard = clipboard,
        )
        AesDecryptionSection(
            uiState = uiState,
            onEncryptedTextChange = { viewModel.updateEncryptedText(it) },
            onIvChange = { viewModel.updateIvText(it) },
            onDecryptClick = { viewModel.decryptText() },
            clipboard = clipboard,
        )
        uiState.error?.let { error ->
            AesErrorCard(error = error)
        }
        AesClearButton(onClearClick = { viewModel.clearAll() })
    }
}
