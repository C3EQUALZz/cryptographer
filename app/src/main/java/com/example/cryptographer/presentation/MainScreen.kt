package com.example.cryptographer.presentation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptographer.R
import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
import com.example.cryptographer.presentation.encoding.EncodingScreen
import com.example.cryptographer.presentation.encoding.EncodingViewModel
import com.example.cryptographer.presentation.encryption.EncryptionScreen
import com.example.cryptographer.presentation.encryption.EncryptionViewModel
import com.example.cryptographer.presentation.key.KeyGenerationScreen
import com.example.cryptographer.presentation.key.KeyGenerationViewModel
import com.example.cryptographer.setup.i18n.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Main screen that handles navigation between different app screens.
 * Features a navigation drawer with screen selection and algorithm selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var selectedScreen by remember { mutableStateOf(AppScreen.KeyGeneration) }
    var selectedAlgorithm by remember { mutableStateOf(EncryptionAlgorithm.AES_256) }
    var currentLanguage by remember { mutableStateOf(LocaleHelper.getSavedLanguage(context)) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                selectedScreen = selectedScreen,
                selectedAlgorithm = selectedAlgorithm,
                currentLanguage = currentLanguage,
                drawerState = drawerState,
                scope = scope,
                context = context,
                onScreenSelected = { _ ->
                    scope.launch {
                        drawerState.close()
                    }
                },
                onAlgorithmSelected = { _ ->
                    scope.launch {
                        drawerState.close()
                    }
                },
                onLanguageSelected = { language ->
                    LocaleHelper.setLocale(context, language)
                    // Restart activity to apply locale change
                    (context as? android.app.Activity)?.recreate()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(getScreenTitle(selectedScreen)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.menu)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedScreen) {
                    AppScreen.KeyGeneration -> {
                        val viewModel: KeyGenerationViewModel = viewModel()
                        KeyGenerationScreen(viewModel = viewModel)
                    }
                    AppScreen.Encryption -> {
                        val viewModel: EncryptionViewModel = viewModel()
                        EncryptionScreen(viewModel = viewModel)
                    }
                    AppScreen.Encoding -> {
                        val viewModel: EncodingViewModel = viewModel()
                        EncodingScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

/**
 * Navigation drawer content with screen and algorithm selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationDrawerContent(
    selectedScreen: AppScreen,
    selectedAlgorithm: EncryptionAlgorithm,
    currentLanguage: LocaleHelper.Language,
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,
    onScreenSelected: (AppScreen) -> Unit,
    onAlgorithmSelected: (EncryptionAlgorithm) -> Unit,
    onLanguageSelected: (LocaleHelper.Language) -> Unit
) {
    var expandedLanguageDropdown by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Close button in top-right corner
            IconButton(
                onClick = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close_menu),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // Language selection section
                Text(
                    text = stringResource(R.string.language_selection),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Box {
                    ExposedDropdownMenuBox(
                        expanded = expandedLanguageDropdown,
                        onExpandedChange = { expandedLanguageDropdown = !expandedLanguageDropdown }
                    ) {
                        @Suppress("DEPRECATION")
                        OutlinedTextField(
                            value = currentLanguage.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguageDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedLanguageDropdown,
                            onDismissRequest = { expandedLanguageDropdown = false }
                        ) {
                            LocaleHelper.Language.entries.forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language.displayName) },
                                    onClick = {
                                        onLanguageSelected(language)
                                        expandedLanguageDropdown = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // Screen selection section
                Text(
                    text = stringResource(R.string.screens),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.key_generation)) },
                    selected = selectedScreen == AppScreen.KeyGeneration,
                    onClick = { onScreenSelected(AppScreen.KeyGeneration) },
                    modifier = Modifier.fillMaxWidth()
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    label = { Text(stringResource(R.string.encryption_decryption)) },
                    selected = selectedScreen == AppScreen.Encryption,
                    onClick = { onScreenSelected(AppScreen.Encryption) },
                    modifier = Modifier.fillMaxWidth()
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    label = { Text(stringResource(R.string.encoding)) },
                    selected = selectedScreen == AppScreen.Encoding,
                    onClick = { onScreenSelected(AppScreen.Encoding) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // Algorithm selection section
                Text(
                    text = stringResource(R.string.encryption_algorithms),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                EncryptionAlgorithm.entries.forEach { algorithm ->
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text(algorithm.name) },
                        selected = selectedAlgorithm == algorithm,
                        onClick = { onAlgorithmSelected(algorithm) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Gets the title for the current screen.
 */
@Composable
private fun getScreenTitle(screen: AppScreen): String {
    return when (screen) {
        AppScreen.KeyGeneration -> stringResource(R.string.screen_key_generation)
        AppScreen.Encryption -> stringResource(R.string.screen_encryption)
        AppScreen.Encoding -> stringResource(R.string.screen_encoding)
    }
}

/**
 * App screens enumeration.
 */
private enum class AppScreen {
    KeyGeneration,
    Encryption,
    Encoding
}
