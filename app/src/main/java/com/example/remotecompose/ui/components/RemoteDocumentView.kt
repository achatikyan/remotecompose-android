@file:SuppressLint("RestrictedApiAndroidX")

package com.example.remotecompose.ui.components

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.remote.player.view.RemoteComposePlayer

@Composable
fun RemoteDocumentView(
    documentBytes: ByteArray,
    modifier: Modifier = Modifier,
    onAction: (id: Int, metadata: String?) -> Unit = { _, _ -> },
) {
    key(documentBytes) {
        AndroidView(
            factory = { ctx ->
                RemoteComposePlayer(ctx).apply {
                    setDocument(documentBytes)
                    addIdActionListener { id, metadata ->
                        onAction(id, metadata)
                    }
                }
            },
            modifier = modifier
        )
    }
}
