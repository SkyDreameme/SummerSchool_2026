package com.apex.karting.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apex.karting.data.ApexRepository
import com.apex.karting.data.Client
import com.apex.karting.data.MockData
import com.apex.karting.ui.components.ApexDestructiveButton
import com.apex.karting.ui.components.ErrorState
import com.apex.karting.ui.components.InfoCard
import com.apex.karting.ui.components.LabelValueRow
import com.apex.karting.ui.components.SkeletonList
import com.apex.karting.ui.theme.ApexRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { ApexRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var client by remember { mutableStateOf<Client?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    fun load(isRefresh: Boolean = false) {
        scope.launch {
            if (isRefresh) refreshing = true else loading = true
            error = false
            try {
                client = repository.getClient()
            } catch (_: Exception) {
                error = true
            } finally {
                loading = false
                refreshing = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    IconButton(onClick = { load(isRefresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            repository.logout()
                            onLogout()
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Выйти")
                    }
                }
            )
        }
    ) { padding ->
        when {
            loading -> SkeletonList(Modifier.padding(padding))
            error -> ErrorState(
                title = "Нет соединения",
                subtitle = "Проверьте интернет",
                onRetry = { load() },
                modifier = Modifier.padding(padding)
            )
            client != null -> {
                Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                    InfoCard {
                        LabelValueRow("Телефон", client!!.phone)
                    }
                    Spacer(Modifier.height(16.dp))
                    InfoCard {
                        Text(client!!.loyaltyStatus.label, color = ApexRed, style = MaterialTheme.typography.titleMedium)
                        Text("Скидка ${client!!.discountPercent}%")
                    }
                    Spacer(Modifier.height(16.dp))
                    InfoCard {
                        Text("Контакты", style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${MockData.SUPPORT_PHONE}")))
                        }) { Text("Позвонить в поддержку") }
                        TextButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MockData.SUPPORT_TELEGRAM)))
                        }) { Text("Написать в Telegram") }
                    }
                    Spacer(Modifier.weight(1f))
                    ApexDestructiveButton(
                        text = "Выйти",
                        onClick = {
                            scope.launch {
                                repository.logout()
                                onLogout()
                            }
                        }
                    )
                }
            }
        }
    }
}