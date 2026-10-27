package com.example.carpetneed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.carpetneed.ui.navigation.CarpetNavHost
import com.example.carpetneed.ui.theme.CarpetNeedTheme
import com.example.carpetneed.ui.viewmodel.CarpetNeedViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CarpetNeedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val settings = uiState.appSettings

            CarpetNeedTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CarpetNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
