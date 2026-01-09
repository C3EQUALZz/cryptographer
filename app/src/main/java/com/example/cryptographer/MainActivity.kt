package com.example.cryptographer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptographer.presentation.key.KeyGenerationScreen
import com.example.cryptographer.presentation.key.KeyGenerationViewModel
import com.example.cryptographer.ui.theme.CryptographerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptographerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Hilt automatically provides ViewModel
                    val viewModel: KeyGenerationViewModel = viewModel()
                    KeyGenerationScreen(viewModel = viewModel)
                }
            }
        }
    }
}