package com.example.cryptographer.presentation.main

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptographer.R
import com.example.cryptographer.domain.common.valueobjects.Language
import com.example.cryptographer.domain.common.valueobjects.ThemeMode
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
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
fun MainScreen(viewModel: MainViewModel = viewModel()) {
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

    val drawerActions = remember(viewModel, scope, drawerState, context) {
        DrawerActions(
            onScreenSelected = { screen ->
                viewModel.selectScreen(screen)
                scope.launch { drawerState.close() }
            },
            onAlgorithmSelected = { algorithm ->
                viewModel.selectAlgorithm(algorithm)
                scope.launch { drawerState.close() }
            },
            onLanguageSelected = { language ->
                viewModel.updateLanguage(language.code)
                scope.launch {
                    drawerState.close()
                    (context as? Activity)?.recreate()
                }
            },
            onThemeModeChanged = { themeMode ->
                viewModel.updateThemeMode(themeMode.value)
                scope.launch {
                    drawerState.close()
                    (context as? Activity)?.recreate()
                }
            },
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                drawerState = drawerState,
                scope = scope,
                context = context,
                selectedScreen = uiState.selectedScreen,
                selectedAlgorithm = uiState.selectedAlgorithm,
                currentLanguage = currentLanguage,
                currentThemeMode = currentThemeMode,
                actions = drawerActions,
            )
        },
    ) {
        MainScreenContent(
            selectedScreen = uiState.selectedScreen,
            drawerState = drawerState,
            scope = scope,
        )
    }
}

@Composable
private fun MainScreenContent(
    selectedScreen: AppScreen,
    drawerState: DrawerState,
    scope: CoroutineScope,
) {
    Scaffold(
        topBar = {
            MainScreenTopBar(
                screen = selectedScreen,
                onMenuClick = {
                    scope.launch { drawerState.open() }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ScreenContent(selectedScreen = selectedScreen)
        }
    }
}

@Composable
private fun MainScreenTopBar(
    screen: AppScreen,
    onMenuClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(getScreenTitle(screen)) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.menu),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

@Composable
private fun ScreenContent(selectedScreen: AppScreen) {
    when (selectedScreen) {
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

private data class DrawerActions(
    val onScreenSelected: (AppScreen) -> Unit,
    val onAlgorithmSelected: (EncryptionAlgorithm) -> Unit,
    val onLanguageSelected: (Language) -> Unit,
    val onThemeModeChanged: (ThemeMode) -> Unit,
)

/**
 * Navigation drawer content with screen and algorithm selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationDrawerContent(
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,
    selectedScreen: AppScreen,
    selectedAlgorithm: EncryptionAlgorithm,
    currentLanguage: Language,
    currentThemeMode: ThemeMode,
    actions: DrawerActions,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            DrawerCloseButton(
                onClose = {
                    scope.launch { drawerState.close() }
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 48.dp),
            ) {
                DrawerTitle()
                LanguageSelectionSection(
                    currentLanguage = currentLanguage,
                    onLanguageSelected = actions.onLanguageSelected,
                )
                DividerSpacer()
                ThemeSelectionSection(
                    currentThemeMode = currentThemeMode,
                    context = context,
                    onThemeModeChanged = actions.onThemeModeChanged,
                )
                DividerSpacer()
                ScreenSelectionSection(
                    selectedScreen = selectedScreen,
                    onScreenSelected = actions.onScreenSelected,
                )
                DividerSpacer()
                AlgorithmSelectionSection(
                    selectedAlgorithm = selectedAlgorithm,
                    onAlgorithmSelected = actions.onAlgorithmSelected,
                )
            }
        }
    }
}

@Composable
private fun DrawerCloseButton(onClose: () -> Unit) {
    IconButton(
        onClick = onClose,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.close_menu),
        )
    }
}

@Composable
private fun DrawerTitle() {
    Text(
        text = stringResource(R.string.menu),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 24.dp),
        color = MaterialTheme.colorScheme.primary,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelectionSection(
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
) {
    var expandedLanguageDropdown by remember { mutableStateOf(false) }

    Text(
        text = stringResource(R.string.language_selection),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
    )

    ExposedDropdownMenuBox(
        expanded = expandedLanguageDropdown,
        onExpandedChange = { expandedLanguageDropdown = !expandedLanguageDropdown },
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
            shape = RoundedCornerShape(8.dp),
        )
        ExposedDropdownMenu(
            expanded = expandedLanguageDropdown,
            onDismissRequest = { expandedLanguageDropdown = false },
        ) {
            Language.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.displayName) },
                    onClick = {
                        onLanguageSelected(language)
                        expandedLanguageDropdown = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectionSection(
    currentThemeMode: ThemeMode,
    context: Context,
    onThemeModeChanged: (ThemeMode) -> Unit,
) {
    Text(
        text = stringResource(R.string.theme_selection),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.dark_theme),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = when (currentThemeMode) {
                ThemeMode.SYSTEM -> {
                    (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES
                }
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            },
            onCheckedChange = { isDark ->
                val newTheme = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
                onThemeModeChanged(newTheme)
            },
        )
    }
}

@Composable
private fun ScreenSelectionSection(
    selectedScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
) {
    Text(
        text = stringResource(R.string.screens),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
    )

    NavigationDrawerItem(
        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
        label = { Text(stringResource(R.string.key_generation)) },
        selected = selectedScreen == AppScreen.KeyGeneration,
        onClick = { onScreenSelected(AppScreen.KeyGeneration) },
        modifier = Modifier.fillMaxWidth(),
    )

    NavigationDrawerItem(
        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
        label = { Text(stringResource(R.string.encryption_decryption)) },
        selected = selectedScreen == AppScreen.Encryption,
        onClick = { onScreenSelected(AppScreen.Encryption) },
        modifier = Modifier.fillMaxWidth(),
    )

    NavigationDrawerItem(
        icon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
        label = { Text(stringResource(R.string.encoding)) },
        selected = selectedScreen == AppScreen.Encoding,
        onClick = { onScreenSelected(AppScreen.Encoding) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AlgorithmSelectionSection(
    selectedAlgorithm: EncryptionAlgorithm,
    onAlgorithmSelected: (EncryptionAlgorithm) -> Unit,
) {
    Text(
        text = stringResource(R.string.encryption_algorithms),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
    )

    EncryptionAlgorithm.entries.forEach { algorithm ->
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text(algorithm.name) },
            selected = selectedAlgorithm == algorithm,
            onClick = { onAlgorithmSelected(algorithm) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DividerSpacer() {
    Spacer(modifier = Modifier.height(24.dp))
    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
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
