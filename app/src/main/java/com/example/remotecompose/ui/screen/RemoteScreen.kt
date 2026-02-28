package com.example.remotecompose.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remotecompose.MainViewModel
import com.example.remotecompose.ui.components.ErrorContent
import com.example.remotecompose.ui.components.RemoteDocumentView
import com.example.remotecompose.ui.components.RemoteScreenTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(
    configUrl: String,
    title: String,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    viewModel: MainViewModel = viewModel(key = configUrl),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(configUrl) {
        viewModel.setConfigUrl(configUrl)
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val subtitle = if (state.lastUpdated > 0) {
        "Last updated: ${timeFormat.format(Date(state.lastUpdated))}"
    } else null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            RemoteScreenTopBar(
                title = title,
                subtitle = subtitle,
                showBack = showBack,
                isLoading = state.isLoading,
                onBack = onBack,
                onRefresh = { viewModel.refresh() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.errorMessage != null && state.documentBytes == null -> {
                    ErrorContent(
                        errorMessage = state.errorMessage!!,
                        onRetry = { viewModel.refresh() }
                    )
                }
                state.documentBytes != null -> {
                    RemoteDocumentView(
                        documentBytes = state.documentBytes!!,
                        modifier = Modifier.fillMaxSize(),
                        onAction = { id, metadata ->
                            viewModel.onAction(id, metadata)
                            if (metadata?.startsWith("navigate:") == true) {
                                onNavigate(metadata.removePrefix("navigate:"))
                            } else {
                                val msg = "Action (ID: $id, Data: $metadata)"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}
