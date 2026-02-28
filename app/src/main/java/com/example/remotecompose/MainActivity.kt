package com.example.remotecompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.remotecompose.navigation.AppNavigation
import com.example.remotecompose.ui.theme.RemoteComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemoteComposeTheme {
                AppNavigation()
            }
        }
    }
}
