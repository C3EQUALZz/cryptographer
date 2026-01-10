package com.example.cryptographer.presentation.main

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptographer.R
import com.example.cryptographer.domain.common.value_objects.Language
import com.example.cryptographer.domain.common.value_objects.ThemeMode
import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
import com.example.cryptographer.presentation.encoding.EncodingScreen
import com.example.cryptographer.presentation.encoding.EncodingViewModel
import com.example.cryptographer.presentation.encryption.EncryptionScreen
import com.example.cryptographer.presentation.encryption.EncryptionViewModel
import com.example.cryptographer.presentation.key.KeyGenerationScreen
import com.example.cryptographer.presentation.key.KeyGenerationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Main screen that handles navigation between different app screens.
 * Features a navigation drawer with screen selection and algorithm selection.
 * Uses ViewModel for state management and Presenter for business logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Convert string codes to domain enums
    val currentLanguage = remember(uiState.languageCode) {
        Language.fromCode(uiState.languageCode)
    }
    val currentThemeMode = remember(uiState.themeMode) {
        ThemeMode.fromValue(uiState.themeMode)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                selectedScreen = uiState.selectedScreen,
                selectedAlgorithm = uiState.selectedAlgorithm,
                currentLanguage = currentLanguage,
                currentThemeMode = currentThemeMode,
                drawerState = drawerState,
                scope = scope,
                context = context,
                onScreenSelected = { screen ->
                    viewModel.selectScreen(screen)
                    scope.launch {
                        drawerState.close()
                    }
                },
                onAlgorithmSelected = { algorithm ->
                    viewModel.selectAlgorithm(algorithm)
                    scope.launch {
                        drawerState.close()
                    }
                },
                onLanguageSelected = { language ->
                    viewModel.updateLanguage(language.code)
                    // Restart activity to apply locale change
                    scope.launch {
                        drawerState.close()
                        (context as? Activity)?.recreate()
                    }
                },
                onThemeModeChanged = { themeMode ->
                    viewModel.updateThemeMode(themeMode.value)
                    // Restart activity to apply theme change
                    scope.launch {
                        drawerState.close()
                        (context as? Activity)?.recreate()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(getScreenTitle(uiState.selectedScreen)) },
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
                when (uiState.selectedScreen) {
                    AppScreen.KeyGeneration -> {
                        val keyViewModel: KeyGenerationViewModel = viewModel()
                        KeyGenerationScreen(viewModel = keyViewModel)
                    }
                    AppScreen.Encryption -> {
                        val encryptionViewModel: EncryptionViewModel = viewModel()
                        EncryptionScreen(viewModel = encryptionViewModel)
                    }
                    AppScreen.Encoding -> {
                        val encodingViewModel: EncodingViewModel = viewModel()
                        EncodingScreen(viewModel = encodingViewModel)
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
    currentLanguage: Language,
    currentThemeMode: ThemeMode,
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,
    onScreenSelected: (AppScreen) -> Unit,
    onAlgorithmSelected: (EncryptionAlgorithm) -> Unit,
    onLanguageSelected: (Language) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit
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
                    contentDescription = stringResource(R.string.close_menu)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.menu),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                // Language selection section
                Text(
                    text = stringResource(R.string.language_selection),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                ExposedDropdownMenuBox(
                    expanded = expandedLanguageDropdown,
                    onExpandedChange = { expandedLanguageDropdown = !expandedLanguageDropdown }
                ) {
                    OutlinedTextField(
                        value = currentLanguage.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguageDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedLanguageDropdown,
                        onDismissRequest = { expandedLanguageDropdown = false }
                    ) {
                        Language.entries.forEach { language ->
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

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // Theme selection section
                Text(
                    text = stringResource(R.string.theme_selection),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dark_theme),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = when (currentThemeMode) {
                            ThemeMode.SYSTEM -> {
                                // If system mode, check actual system theme
                                (context.resources.configuration.uiMode and
                                        Configuration.UI_MODE_NIGHT_MASK) ==
                                        Configuration.UI_MODE_NIGHT_YES
                            }
                            ThemeMode.DARK -> true
                            ThemeMode.LIGHT -> false
                        },
                        onCheckedChange = { isDark ->
                            val newTheme = if (isDark) {
                                ThemeMode.DARK
                            } else {
                                ThemeMode.LIGHT
                            }
                            onThemeModeChanged(newTheme)
                        }
                    )
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
