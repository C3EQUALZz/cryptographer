package com.example.cryptographer.presentation.main.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cryptographer.R
import com.example.cryptographer.domain.common.valueobjects.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionSection(currentLanguage: Language, onLanguageSelected: (Language) -> Unit) {
    var expandedLanguageDropdown by remember { mutableStateOf(false) }

    Text(
        text = stringResource(R.string.language_selection),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
    )

    ExposedDropdownMenuBox(
        expanded = expandedLanguageDropdown,
        onExpandedChange = { expandedLanguageDropdown = it },
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
