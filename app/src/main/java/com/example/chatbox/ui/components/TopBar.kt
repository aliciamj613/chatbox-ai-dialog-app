package com.example.chatbox.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onClear: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        actions = {
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.Delete, contentDescription = "Clear")
            }
        }
    )
}
