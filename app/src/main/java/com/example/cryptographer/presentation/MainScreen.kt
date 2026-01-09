package com.example.cryptographer.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptographer.presentation.encryption.EncryptionScreen
import com.example.cryptographer.presentation.encryption.EncryptionViewModel
import com.example.cryptographer.presentation.key.KeyGenerationScreen
import com.example.cryptographer.presentation.key.KeyGenerationViewModel

/**
 * Main screen that handles navigation between different app screens.
 * Features a floating bottom navigation dock.
 */
@Composable
fun MainScreen() {
    var selectedScreen by remember { mutableStateOf<AppScreen>(AppScreen.KeyGeneration) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Content area
        when (selectedScreen) {
            AppScreen.KeyGeneration -> {
                val viewModel: KeyGenerationViewModel = viewModel()
                KeyGenerationScreen(viewModel = viewModel)
            }
            AppScreen.Encryption -> {
                val viewModel: EncryptionViewModel = viewModel()
                EncryptionScreen(viewModel = viewModel)
            }
        }

        // Floating bottom navigation dock
        FloatingNavigationDock(
            selectedScreen = selectedScreen,
            onScreenSelected = { selectedScreen = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

/**
 * Floating bottom navigation dock that hovers above content.
 */
@Composable
private fun FloatingNavigationDock(
    selectedScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationButton(
                screen = AppScreen.KeyGeneration,
                icon = Icons.Default.Settings,
                label = "Ключи",
                isSelected = selectedScreen == AppScreen.KeyGeneration,
                onClick = { onScreenSelected(AppScreen.KeyGeneration) },
                modifier = Modifier.weight(1f)
            )

            NavigationButton(
                screen = AppScreen.Encryption,
                icon = Icons.Default.Lock,
                label = "Шифрование",
                isSelected = selectedScreen == AppScreen.Encryption,
                onClick = { onScreenSelected(AppScreen.Encryption) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual navigation button in the dock.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationButton(
    screen: AppScreen,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

/**
 * App screens enumeration.
 */
private enum class AppScreen {
    KeyGeneration,
    Encryption
}

