package com.apex.karting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.apex.karting.data.ApexRepository
import com.apex.karting.ui.navigation.ApexNavHost
import com.apex.karting.ui.theme.ApexTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApexTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var checked by remember { mutableStateOf(false) }
                val repository = remember { ApexRepository.getInstance(this) }

                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        isLoggedIn = repository.checkSession()
                        checked = true
                    }
                }

                if (!checked) {
                    // показывать спиннер? можно просто пустой экран
                } else {
                    ApexNavHost(startDestination = if (isLoggedIn) "main" else "auth")
                }
            }
        }
    }
}