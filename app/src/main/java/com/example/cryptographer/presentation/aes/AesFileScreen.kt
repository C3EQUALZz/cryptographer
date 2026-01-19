package com.example.cryptographer.presentation.aes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cryptographer.presentation.aes.components.AesClearButton
import com.example.cryptographer.presentation.aes.components.AesErrorCard
import com.example.cryptographer.presentation.aes.components.AesFileDecryptionSection
import com.example.cryptographer.presentation.aes.components.AesFileEncryptionSection
import com.example.cryptographer.presentation.aes.components.AesFileInfoCard
import com.example.cryptographer.presentation.aes.components.AesFileScreenTitle
import com.example.cryptographer.presentation.aes.components.AesKeyLengthSelection
import com.example.cryptographer.presentation.aes.components.AesKeySelectionSection
import com.example.cryptographer.presentation.common.defaultOutputFileName
import com.example.cryptographer.presentation.common.persistReadPermission
import com.example.cryptographer.presentation.common.persistWritePermission

/**
 * Screen for AES file encryption and decryption.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AesFileScreen(viewModel: AesFileViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val availableKeys by viewModel.availableKeys.collectAsState()
    val clipboard = LocalClipboard.current
    val context = LocalContext.current

    val encryptInputPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            persistReadPermission(context, it)
            viewModel.updateEncryptInputPath(it.toString())
        }
    }

    val encryptOutputPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        uri?.let {
            persistWritePermission(context, it)
            viewModel.updateEncryptOutputPath(it.toString())
        }
    }

    val decryptInputPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            persistReadPermission(context, it)
            viewModel.updateDecryptInputPath(it.toString())
        }
    }

    val decryptOutputPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        uri?.let {
            persistWritePermission(context, it)
            viewModel.updateDecryptOutputPath(it.toString())
        }
    }

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
        AesFileScreenTitle()
        AesFileInfoCard()
        AesKeyLengthSelection(
            selectedLength = uiState.selectedKeyLength,
            onLengthSelected = { viewModel.selectKeyLength(it) },
        )
        AesKeySelectionSection(
            availableKeys = availableKeys,
            selectedKeyId = uiState.selectedKeyId,
            onKeySelected = { viewModel.selectKey(it) },
        )
        AesFileEncryptionSection(
            uiState = uiState,
            onInputChange = { viewModel.updateEncryptInputPath(it) },
            onOutputChange = { viewModel.updateEncryptOutputPath(it) },
            onPickInput = { encryptInputPicker.launch(arrayOf("*/*")) },
            onPickOutput = {
                val fileName = defaultOutputFileName(
                    inputPath = uiState.encryptInputPath,
                    suffix = ".enc",
                    fallback = "encrypted.bin",
                )
                encryptOutputPicker.launch(fileName)
            },
            onEncryptClick = { viewModel.encryptFile() },
            clipboard = clipboard,
        )
        AesFileDecryptionSection(
            uiState = uiState,
            onInputChange = { viewModel.updateDecryptInputPath(it) },
            onOutputChange = { viewModel.updateDecryptOutputPath(it) },
            onPickInput = { decryptInputPicker.launch(arrayOf("*/*")) },
            onPickOutput = {
                val fileName = defaultOutputFileName(
                    inputPath = uiState.decryptInputPath,
                    suffix = ".dec",
                    fallback = "decrypted.bin",
                )
                decryptOutputPicker.launch(fileName)
            },
            onDecryptClick = { viewModel.decryptFile() },
            clipboard = clipboard,
        )
        uiState.error?.let { error ->
            AesErrorCard(error = error)
        }
        AesClearButton(onClearClick = { viewModel.clearAll() })
    }
}
