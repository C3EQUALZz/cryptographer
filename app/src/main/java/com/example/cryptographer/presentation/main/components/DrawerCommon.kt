package com.example.cryptographer.presentation.main.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cryptographer.R

@Composable
fun BoxScope.DrawerCloseButton(onClose: () -> Unit) {
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
fun DrawerTitle() {
    Text(
        text = stringResource(R.string.menu),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 24.dp),
        color = MaterialTheme.colorScheme.primary,
    )
}
