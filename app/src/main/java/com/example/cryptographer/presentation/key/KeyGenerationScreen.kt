package com.example.cryptographer.presentation.key

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.presentation.key.components.AlgorithmSelectionCard
import com.example.cryptographer.presentation.key.components.DeleteAllKeysDialog
import com.example.cryptographer.presentation.key.components.GenerateKeyButton
import com.example.cryptographer.presentation.key.components.GeneratedKeyCard
import com.example.cryptographer.presentation.key.components.KeyGenerationErrorCard
import com.example.cryptographer.presentation.key.components.KeyGenerationInfoCard
import com.example.cryptographer.presentation.key.components.KeyGenerationScreenTitle
import com.example.cryptographer.presentation.key.components.SavedKeysSection

/**
 * Screen for generating and viewing encryption keys.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyGenerationScreen(viewModel: KeyGenerationViewModel) {
    LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val savedKeys by viewModel.savedKeys.collectAsState()
    val clipboard = LocalClipboard.current
    var selectedAlgorithm by remember { mutableStateOf(EncryptionAlgorithm.AES_256) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        KeyGenerationScreenTitle()
        KeyGenerationInfoCard()
        AlgorithmSelectionCard(
            selectedAlgorithm = selectedAlgorithm,
            onAlgorithmSelected = { selectedAlgorithm = it },
        )
        GenerateKeyButton(
            isLoading = uiState.isLoading,
            enabled = !uiState.isLoading,
            onClick = { viewModel.generateKey(selectedAlgorithm) },
        )
        uiState.generatedKey?.let { key ->
            GeneratedKeyCard(
                key = key,
                uiState = uiState,
                clipboard = clipboard,
            )
        }
        uiState.error?.let { error ->
            KeyGenerationErrorCard(error = error)
        }
        SavedKeysSection(
            savedKeys = savedKeys,
            onKeyClick = { viewModel.loadKey(it) },
            onDeleteKey = { viewModel.deleteKey(it) },
            onDeleteAllClick = { showDeleteAllDialog = true },
        )
        if (showDeleteAllDialog) {
            DeleteAllKeysDialog(
                keysCount = savedKeys.size,
                onConfirm = {
                    viewModel.deleteAllKeys()
                    showDeleteAllDialog = false
                },
                onDismiss = { showDeleteAllDialog = false },
            )
        }
    }
}
