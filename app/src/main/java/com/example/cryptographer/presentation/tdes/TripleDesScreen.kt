package com.example.cryptographer.presentation.tdes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import com.example.cryptographer.presentation.tdes.components.TripleDesClearButton
import com.example.cryptographer.presentation.tdes.components.TripleDesDecryptionSection
import com.example.cryptographer.presentation.tdes.components.TripleDesEncryptionSection
import com.example.cryptographer.presentation.tdes.components.TripleDesErrorCard
import com.example.cryptographer.presentation.tdes.components.TripleDesInfoCard
import com.example.cryptographer.presentation.tdes.components.TripleDesKeySelectionSection
import com.example.cryptographer.presentation.tdes.components.TripleDesScreenTitle

/**
 * Screen for Triple DES encryption and decryption.
 * Supports 3DES-112 and 3DES-168 with IV.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripleDesScreen(viewModel: TripleDesViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val availableKeys by viewModel.availableKeys.collectAsState()
    val clipboard = LocalClipboard.current

    // Reload keys when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadAvailableKeys()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TripleDesScreenTitle()
        TripleDesInfoCard()
        TripleDesKeySelectionSection(
            availableKeys = availableKeys,
            selectedKeyId = uiState.selectedKeyId,
            onKeySelected = { viewModel.selectKey(it) },
        )
        TripleDesEncryptionSection(
            uiState = uiState,
            onInputChange = { viewModel.updateInputText(it) },
            onEncryptClick = { viewModel.encryptText() },
            clipboard = clipboard,
        )
        TripleDesDecryptionSection(
            uiState = uiState,
            onEncryptedTextChange = { viewModel.updateEncryptedText(it) },
            onIvChange = { viewModel.updateIvText(it) },
            onDecryptClick = { viewModel.decryptText() },
            clipboard = clipboard,
        )
        uiState.error?.let { error ->
            TripleDesErrorCard(error = error)
        }
        TripleDesClearButton(onClearClick = { viewModel.clearAll() })
    }
}
