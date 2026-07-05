package com.apex.karting.ui.screens.rating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apex.karting.data.ApiException
import com.apex.karting.data.ApexRepository
import com.apex.karting.ui.components.ApexPrimaryButton
import com.apex.karting.ui.screens.sheets.RateSuccessSheet
import com.apex.karting.ui.theme.ApexError
import com.apex.karting.ui.theme.ApexRed
import com.apex.karting.util.Formatters
import java.time.Instant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateMarshalScreen(
    slotId: String,
    slotStartsAt: Instant,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ApexRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var rating by remember { mutableIntStateOf(0) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    if (showSuccess) {
        RateSuccessSheet(onDismiss = {
            showSuccess = false
            onDismiss()
        })
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Оцените маршала", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(Formatters.formatDateTime(slotStartsAt), color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                    IconButton(onClick = { rating = star }) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "$star звёзд",
                            tint = if (star <= rating) ApexRed else MaterialTheme.colorScheme.onSurface.copy(0.3f)
                        )
                    }
                }
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = ApexError)
            }
            Spacer(Modifier.height(24.dp))
            ApexPrimaryButton(
                text = "Отправить оценку",
                enabled = rating in 1..5,
                loading = submitting,
                onClick = {
                    scope.launch {
                        submitting = true
                        error = null
                        try {
                            repository.rateMarshal(slotId, rating)
                            showSuccess = true
                        } catch (e: ApiException) {
                            error = e.message
                            if (e.code.name.contains("ALREADY") || e.code.name.contains("NOT_ALLOWED")) {
                                kotlinx.coroutines.delay(1500)
                                onDismiss()
                            }
                        } finally {
                            submitting = false
                        }
                    }
                }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}