package com.example.cryptographer.presentation.main

import android.app.Activity
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptographer.domain.common.valueobjects.Language
import com.example.cryptographer.domain.common.valueobjects.ThemeMode
import com.example.cryptographer.presentation.main.components.DrawerActions
import com.example.cryptographer.presentation.main.components.MainScreenContent
import com.example.cryptographer.presentation.main.components.NavigationDrawerContent
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
