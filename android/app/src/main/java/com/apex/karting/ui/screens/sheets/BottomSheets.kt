package com.apex.karting.ui.screens.sheets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
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
import com.apex.karting.data.Booking
import com.apex.karting.data.TrackConfiguration
import com.apex.karting.ui.components.ApexDestructiveButton
import com.apex.karting.ui.components.ApexPrimaryButton
import com.apex.karting.ui.components.ApexSecondaryButton
import com.apex.karting.ui.components.LabelValueRow
import com.apex.karting.ui.theme.ApexError
import com.apex.karting.ui.theme.ApexSuccess
import com.apex.karting.util.Formatters
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingSuccessSheet(
    booking: Booking,
    onDismiss: () -> Unit,
    onGoToBookings: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { /* blocked per spec */ },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ApexSuccess, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Бронь создана", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            booking.slot?.let { slot ->
                LabelValueRow("Дата", Formatters.formatDate(slot.startsAt))
                LabelValueRow("Время", Formatters.formatTime(slot.startsAt))
                LabelValueRow("Конфигурация", slot.trackConfiguration.label)
            }
            LabelValueRow("Экипировка", booking.gearOption.label)
            LabelValueRow("Итого", Formatters.formatPrice(booking.finalPrice))
            Spacer(Modifier.height(16.dp))
            Text("Переведите сумму по ссылке, чтобы подтвердить бронь.")
            Text("Бронь автоматически перейдёт в статус Paid после поступления оплаты.")
            Spacer(Modifier.height(8.dp))
            Text("Если оплата прошла, но статус не изменился — свяжитесь с администратором.")
            Spacer(Modifier.height(16.dp))
            ApexPrimaryButton(
                text = "Оплатить депозит",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(booking.depositLink)))
                }
            )
            Spacer(Modifier.height(8.dp))
            ApexSecondaryButton(
                text = "Скопировать ссылку",
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("deposit", booking.depositLink))
                    Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(Modifier.height(8.dp))
            ApexSecondaryButton(
                text = "К моим записям",
                onClick = {
                    onDismiss()
                    onGoToBookings()
                }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmCancelSheet(
    booking: Booking,
    onDismiss: () -> Unit,
    onCancelled: () -> Unit
) {
    val context = LocalContext.current
    val repository = com.apex.karting.data.ApexRepository.getInstance(context)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Отменить бронь?", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("Депозит не возвращается автоматически. Отменить бронь нельзя будет восстановить.")
            booking.slot?.let {
                Spacer(Modifier.height(16.dp))
                Text(Formatters.formatDateTime(it.startsAt))
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = ApexError)
            }
            Spacer(Modifier.height(24.dp))
            ApexDestructiveButton(
                text = "Отменить бронь",
                loading = loading,
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            repository.cancelBooking(booking.id)
                            onCancelled()
                        } catch (e: com.apex.karting.data.ApiException) {
                            error = e.message
                        } finally {
                            loading = false
                        }
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            ApexSecondaryButton(text = "Оставить бронь", onClick = onDismiss)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelSuccessSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ApexSuccess, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Бронь отменена", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Возврат депозита осуществляется по правилам центра.")
            Spacer(Modifier.height(24.dp))
            ApexPrimaryButton(text = "Понятно", onClick = onDismiss)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateSuccessSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ApexSuccess, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Спасибо за оценку!", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            ApexPrimaryButton(text = "Закрыть", onClick = onDismiss)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackConfigSheet(
    config: TrackConfiguration,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Конфигурация ${config.label}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Text(config.description)
            Spacer(Modifier.height(24.dp))
            ApexPrimaryButton(text = "Понятно", onClick = onDismiss)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterSheet(
    initialRange: com.apex.karting.data.DateRange,
    onDismiss: () -> Unit,
    onApply: (com.apex.karting.data.DateRange) -> Unit,
    onReset: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var fromDays by remember { mutableIntStateOf(0) }
    var toDays by remember { mutableIntStateOf(13) }
    val today = java.time.LocalDate.now()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Выберите диапазон дат", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Text("От: через $fromDays дн. (${today.plusDays(fromDays.toLong())})")
            Slider(
                value = fromDays.toFloat(),
                onValueChange = { fromDays = it.toInt().coerceIn(0, toDays) },
                valueRange = 0f..30f,
                steps = 29
            )
            Text("До: через $toDays дн. (${today.plusDays(toDays.toLong())})")
            Slider(
                value = toDays.toFloat(),
                onValueChange = { toDays = it.toInt().coerceIn(fromDays, 30) },
                valueRange = 0f..30f,
                steps = 29
            )
            Spacer(Modifier.height(16.dp))
            ApexPrimaryButton(
                text = "Применить",
                onClick = {
                    onApply(
                        com.apex.karting.data.DateRange(
                            today.plusDays(fromDays.toLong()),
                            today.plusDays(toDays.toLong())
                        )
                    )
                }
            )
            Spacer(Modifier.height(8.dp))
            ApexSecondaryButton(text = "Сбросить", onClick = onReset)
            Spacer(Modifier.height(32.dp))
        }
    }
}