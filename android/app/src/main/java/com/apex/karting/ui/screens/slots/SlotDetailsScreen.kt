package com.apex.karting.ui.screens.slots

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import com.apex.karting.data.Client
import com.apex.karting.data.Slot
import com.apex.karting.data.SlotStatus
import com.apex.karting.data.finalPrice
import com.apex.karting.ui.components.ApexPrimaryButton
import com.apex.karting.ui.components.ErrorState
import com.apex.karting.ui.components.InfoCard
import com.apex.karting.ui.components.LabelValueRow
import com.apex.karting.ui.components.LoadingBox
import com.apex.karting.ui.components.SkeletonList
import com.apex.karting.ui.screens.sheets.TrackConfigSheet
import com.apex.karting.ui.theme.ApexError
import com.apex.karting.ui.theme.ApexRed
import com.apex.karting.util.Formatters
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotDetailsScreen(
    slotId: String,
    onBack: () -> Unit,
    onBook: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ApexRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var slot by remember { mutableStateOf<Slot?>(null) }
    var client by remember { mutableStateOf<Client?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var showTrackConfig by remember { mutableStateOf(false) }

    LaunchedEffect(slotId) {
        loading = true
        error = false
        try {
            slot = repository.getSlot(slotId)
            client = repository.getClient()
        } catch (_: Exception) {
            error = true
        } finally {
            loading = false
        }
    }

    if (showTrackConfig && slot != null) {
        TrackConfigSheet(config = slot!!.trackConfiguration, onDismiss = { showTrackConfig = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали слота") },
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
            error || slot == null -> ErrorState(
                title = "Слот не найден",
                subtitle = "Вернитесь к расписанию",
                onRetry = onBack,
                modifier = Modifier.padding(padding)
            )
            else -> {
                val s = slot!!
                val c = client!!
                val rentalFinal = s.finalPrice(com.apex.karting.data.GearOption.Rental, c.discountPercent)
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                ) {
                    Text(Formatters.formatDateTime(s.startsAt), style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    IconButton(onClick = { showTrackConfig = true }) {
                        Icon(Icons.Default.Info, contentDescription = null)
                    }
                    Text("${s.trackConfiguration.label} · Маршал: ${s.marshal.name}")
                    Spacer(Modifier.height(16.dp))
                    InfoCard {
                        LabelValueRow("С прокатной экипировкой", Formatters.formatPrice(s.priceWithRental))
                        LabelValueRow("Со своей экипировкой", Formatters.formatPrice(s.priceOwnGear))
                    }
                    Spacer(Modifier.height(16.dp))
                    InfoCard {
                        Text("${c.loyaltyStatus.label} · Скидка ${c.discountPercent}%", color = ApexRed)
                        Spacer(Modifier.height(8.dp))
                        Text("Итого: ${Formatters.formatPrice(rentalFinal)}", style = MaterialTheme.typography.headlineMedium)
                        Text("Скидка ${c.discountPercent}% применена")
                    }
                    Spacer(Modifier.weight(1f))
                    val buttonText = when {
                        s.status == SlotStatus.CancelledByCenter -> "Заезд отменён центром"
                        s.safeAvailableSeats <= 0 -> "Мест нет"
                        else -> "Забронировать"
                    }
                    if (s.status == SlotStatus.CancelledByCenter && s.cancellationReason != null) {
                        Text("Причина: ${s.cancellationReason}", color = ApexError, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    ApexPrimaryButton(
                        text = buttonText,
                        enabled = s.isBookable,
                        onClick = { onBook(s.id) }
                    )
                }
            }
        }
    }
}