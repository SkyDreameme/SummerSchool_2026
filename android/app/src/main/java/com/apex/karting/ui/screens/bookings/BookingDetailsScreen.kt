package com.apex.karting.ui.screens.bookings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.apex.karting.data.Booking
import com.apex.karting.data.DisplayBookingStatus
import com.apex.karting.ui.components.ApexDestructiveButton
import com.apex.karting.ui.components.ApexPrimaryButton
import com.apex.karting.ui.components.ApexSecondaryButton
import com.apex.karting.ui.components.InfoCard
import com.apex.karting.ui.components.LabelValueRow
import com.apex.karting.ui.components.LoadingBox
import com.apex.karting.ui.components.SkeletonList
import com.apex.karting.ui.components.StatusBadge
import com.apex.karting.ui.screens.rating.RateMarshalScreen
import com.apex.karting.ui.screens.sheets.CancelSuccessSheet
import com.apex.karting.ui.screens.sheets.ConfirmCancelSheet
import com.apex.karting.ui.theme.ApexError
import com.apex.karting.ui.theme.ApexMuted
import com.apex.karting.util.Formatters
import java.time.Instant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    bookingId: String,
    onBack: () -> Unit,
    onCancelled: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ApexRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var booking by remember { mutableStateOf<Booking?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showCancelSheet by remember { mutableStateOf(false) }
    var showCancelSuccess by remember { mutableStateOf(false) }
    var showRateScreen by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            loading = true
            try {
                booking = repository.getBooking(bookingId)
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(bookingId) { reload() }

    if (showCancelSheet && booking != null) {
        ConfirmCancelSheet(
            booking = booking!!,
            onDismiss = { showCancelSheet = false },
            onCancelled = {
                showCancelSheet = false
                showCancelSuccess = true
                reload()
            }
        )
    }

    if (showCancelSuccess) {
        CancelSuccessSheet(
            onDismiss = {
                showCancelSuccess = false
                onCancelled()
            }
        )
    }

    if (showRateScreen && booking?.slot != null) {
        RateMarshalScreen(
            slotId = booking!!.slotId,
            slotStartsAt = booking!!.slot!!.startsAt,
            onDismiss = {
                showRateScreen = false
                reload()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали брони") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        when {
            loading -> SkeletonList(Modifier.padding(padding))
            booking == null -> Text(
                "Бронь не найдена",
                modifier = Modifier.padding(padding).padding(16.dp)
            )
            else -> {
                val b = booking!!
                val slot = b.slot
                Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                    InfoCard {
                        if (slot != null) {
                            LabelValueRow("Дата", Formatters.formatDate(slot.startsAt))
                            LabelValueRow("Время", Formatters.formatTime(slot.startsAt))
                            LabelValueRow("Конфигурация", slot.trackConfiguration.label)
                        }
                        LabelValueRow("Экипировка", b.gearOption.label)
                        LabelValueRow("Сумма", Formatters.formatPrice(b.finalPrice))
                    }
                    Spacer(Modifier.height(16.dp))
                    InfoCard {
                        StatusBadge(b.displayStatus)
                        b.cancellationReason?.let {
                            Spacer(Modifier.height(8.dp))
                            Text("Причина: $it", color = ApexError)
                        }
                    }
                    if (b.displayStatus == DisplayBookingStatus.Pending) {
                        Spacer(Modifier.height(16.dp))
                        InfoCard {
                            Text("Оплата депозита", style = MaterialTheme.typography.titleSmall)
                            Text(b.depositLink, color = ApexMuted)
                            Spacer(Modifier.height(8.dp))
                            Text("Если оплата прошла, но статус не изменился — свяжитесь с администратором.")
                            Spacer(Modifier.height(8.dp))
                            ApexPrimaryButton(
                                text = "Оплатить депозит",
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(b.depositLink)))
                                }
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    if (b.canCancel) {
                        ApexDestructiveButton(
                            text = "Отменить",
                            onClick = { showCancelSheet = true }
                        )
                    } else if (b.displayStatus in listOf(DisplayBookingStatus.Pending, DisplayBookingStatus.Paid)) {
                        Text(
                            "Отмена доступна не позднее чем за 2 часа до заезда",
                            color = ApexMuted,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    val canRate = b.displayStatus == DisplayBookingStatus.Completed &&
                            !b.rated &&
                            slot != null &&
                            !Instant.now().isBefore(slot.endsAt)
                    if (canRate) {
                        Spacer(Modifier.height(8.dp))
                        ApexSecondaryButton(
                            text = "Оценить маршала",
                            onClick = { showRateScreen = true }
                        )
                    }
                }
            }
        }
    }
}