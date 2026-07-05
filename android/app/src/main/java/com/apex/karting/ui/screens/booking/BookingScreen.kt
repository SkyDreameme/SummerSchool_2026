package com.apex.karting.ui.screens.booking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
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
import com.apex.karting.data.ApiException
import com.apex.karting.data.ApiErrorCode
import com.apex.karting.data.ApexRepository
import com.apex.karting.data.Booking
import com.apex.karting.data.Client
import com.apex.karting.data.GearOption
import com.apex.karting.data.Slot
import com.apex.karting.data.finalPrice
import com.apex.karting.ui.components.ApexPrimaryButton
import com.apex.karting.ui.components.ApexSecondaryButton
import com.apex.karting.ui.components.ErrorState
import com.apex.karting.ui.components.InfoCard
import com.apex.karting.ui.components.LabelValueRow
import com.apex.karting.ui.components.LoadingBox
import com.apex.karting.ui.screens.sheets.BookingSuccessSheet
import com.apex.karting.ui.theme.ApexError
import com.apex.karting.util.Formatters
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    slotId: String,
    onBack: () -> Unit,
    onGoToSchedule: () -> Unit,
    onGoToBookings: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ApexRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var slot by remember { mutableStateOf<Slot?>(null) }
    var client by remember { mutableStateOf<Client?>(null) }
    var gearOption by remember { mutableStateOf(GearOption.Rental) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var submitErrorAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var createdBooking by remember { mutableStateOf<Booking?>(null) }

    // Загрузка данных
    LaunchedEffect(slotId) {
        loading = true
        loadError = null
        try {
            slot = repository.getSlot(slotId)
            client = repository.getClient()
        } catch (e: ApiException) {
            loadError = e.message ?: "Не удалось загрузить данные"
        } catch (e: Exception) {
            loadError = "Произошла ошибка при загрузке"
        } finally {
            loading = false
        }
    }

    // Шторка успеха
    createdBooking?.let { booking ->
        BookingSuccessSheet(
            booking = booking,
            onDismiss = { createdBooking = null },
            onGoToBookings = onGoToBookings
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Бронирование") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        when {
            loading -> LoadingBox(Modifier.padding(padding))
            loadError != null -> ErrorState(
                title = "Ошибка загрузки",
                subtitle = loadError!!,
                onRetry = { /* можно перезагрузить, но проще вернуться назад */ },
                modifier = Modifier.padding(padding)
            )
            slot == null || client == null -> ErrorState(
                title = "Данные не найдены",
                subtitle = "Попробуйте выбрать другой слот",
                onRetry = onBack,
                modifier = Modifier.padding(padding)
            )
            else -> {
                val s = slot!!
                val c = client!!
                val finalPrice = s.finalPrice(gearOption, c.discountPercent)

                Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                    // Информация о слоте
                    InfoCard {
                        Text(Formatters.formatDateTime(s.startsAt), style = MaterialTheme.typography.titleMedium)
                        Text("Конфигурация: ${s.trackConfiguration.label}")
                        Text("Маршал: ${s.marshal.name}")
                        Text("Свободно мест: ${s.safeAvailableSeats}/${s.totalSeats}")
                    }
                    Spacer(Modifier.height(16.dp))

                    // Выбор экипировки
                    Text("Экипировка", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        GearOption.entries.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = gearOption == option,
                                onClick = { gearOption = option },
                                shape = SegmentedButtonDefaults.itemShape(index, GearOption.entries.size),
                                label = { Text(if (option == GearOption.Rental) "Прокат" else "Своя") }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Итоговая цена
                    InfoCard {
                        Text("Итого: ${Formatters.formatPrice(finalPrice)}", style = MaterialTheme.typography.headlineSmall)
                        Text("Скидка ${c.discountPercent}% · ${gearOption.label}")
                    }

                    // Ошибка отправки
                    submitError?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = ApexError)
                        submitErrorAction?.let { action ->
                            Spacer(Modifier.height(8.dp))
                            ApexSecondaryButton(
                                text = if (it.contains("запис")) "К моим записям" else "Выбрать другое время",
                                onClick = action
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Кнопка "Забронировать место"
                    ApexPrimaryButton(
                        text = "Забронировать место",
                        loading = submitting,
                        enabled = !submitting,
                        onClick = {
                            scope.launch {
                                submitting = true
                                submitError = null
                                submitErrorAction = null
                                try {
                                    createdBooking = repository.createBooking(slotId, gearOption)
                                } catch (e: ApiException) {
                                    submitError = e.message
                                    submitErrorAction = when (e.code) {
                                        ApiErrorCode.BOOKING_ALREADY_EXISTS -> onGoToBookings
                                        ApiErrorCode.NO_SEATS, ApiErrorCode.SLOT_CANCELLED -> onGoToSchedule
                                        else -> null
                                    }
                                } catch (e: Exception) {
                                    submitError = "Произошла непредвиденная ошибка"
                                } finally {
                                    submitting = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}