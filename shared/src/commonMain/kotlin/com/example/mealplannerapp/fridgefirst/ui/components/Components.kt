package com.example.mealplannerapp.fridgefirst.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mealplannerapp.fridgefirst.ui.UserMessage

/** Standard padding for screen content lists. */
val ScreenContentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)

/** Like [ScreenContentPadding] with room at the bottom so a floating action button never covers the last row. */
val ScreenContentPaddingWithFab = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)

/**
 * Per-screen scaffold: title bar + snackbar host. Window insets are left to the app shell
 * (which owns the bottom navigation bar), so they aren't applied twice.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeFirstScaffold(
    title: String,
    snackbarHostState: SnackbarHostState? = null,
    subtitle: String? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title)
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = navigationIcon,
                actions = { actions() }
            )
        },
        snackbarHost = { if (snackbarHostState != null) SnackbarHost(snackbarHostState) },
        floatingActionButton = floatingActionButton,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        content = content
    )
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, supporting: String? = null) {
    Column(modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (supporting != null) {
            Text(
                supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Shows [message] once as a snackbar, then reports it consumed. Keyed on the message id so the
 * same text can be shown twice in a row, and a newer message replaces one still on screen.
 */
@Composable
fun MessageSnackbar(message: UserMessage?, snackbarHostState: SnackbarHostState, onShown: (Long) -> Unit) {
    LaunchedEffect(message?.id) {
        if (message != null) {
            snackbarHostState.showSnackbar(message.text)
            onShown(message.id)
        }
    }
}

/** `supportingText` slot for a text field: shows [message] when there is one, otherwise nothing. */
fun errorSupportingText(message: String?): (@Composable () -> Unit)? =
    if (message == null) null else { { Text(message) } }
