package com.apex.karting.ui.screens.slots

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apex.karting.data.ApexRepository
import com.apex.karting.data.DateRange
import com.apex.karting.data.Slot
import com.apex.karting.data.SlotStatus
import com.apex.karting.data.TrackConfiguration
import com.apex.karting.ui.components.ApexSecondaryButton
import com.apex.karting.ui.components.EmptyState
import com.apex.karting.ui.components.ErrorState
import com.apex.karting.ui.components.SkeletonList
import com.apex.karting.ui.screens.sheets.DateFilterSheet
import com.apex.karting.ui.screens.sheets.TrackConfigSheet
import com.apex.karting.ui.theme.ApexError
import com.apex.karting.ui.theme.ApexMuted
import com.apex.karting.util.Formatters
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotListScreen(
    onSlotClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { ApexRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    val today = remember { LocalDate.now() }
    var dateRange by remember { mutableStateOf(DateRange(today, today.plusDays(6))) }
    var selectedDate by remember { mutableStateOf(today) }
    var slots by remember { mutableStateOf<List<Slot>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var showDateFilter by remember { mutableStateOf(false) }
    var showTrackConfig by remember { mutableStateOf<TrackConfiguration?>(null) }
    var noSlotsWarning by remember { mutableStateOf(false) }

    fun loadSlots(isRefresh: Boolean = false) {
        scope.launch {
            if (isRefresh) refreshing = true else loading = true
            error = false
            try {
                slots = repository.getSlots(dateRange.from, dateRange.to)
            } catch (_: Exception) {
                error = true
            } finally {
                loading = false
                refreshing = false
            }
        }
    }

    LaunchedEffect(dateRange) { loadSlots() }

    val dates = remember(dateRange) {
        generateSequence(dateRange.from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(dateRange.to) }
            .toList()
    }

    val filteredSlots = slots?.filter { slot ->
        val slotDate = slot.startsAt.atZone(ZoneId.systemDefault()).toLocalDate()
        slotDate == selectedDate
    }.orEmpty()

    if (showDateFilter) {
        DateFilterSheet(
            initialRange = dateRange,
            onDismiss = { showDateFilter = false },
            onApply = { range ->
                dateRange = range
                selectedDate = range.from
                showDateFilter = false
                noSlotsWarning = false
            },
            onReset = {
                dateRange = DateRange(today, today.plusDays(6))
                selectedDate = today
                showDateFilter = false
            }
        )
    }

    showTrackConfig?.let { config ->
        TrackConfigSheet(config = config, onDismiss = { showTrackConfig = null })
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Расписание") },
                actions = {
                    IconButton(onClick = { loadSlots(isRefresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        when {
            loading -> SkeletonList(Modifier.padding(padding))
            error -> ErrorState(
                title = "Не удалось загрузить расписание",
                subtitle = "Повторите через минуту",
                onRetry = { loadSlots() },
                modifier = Modifier.padding(padding)
            )
            else -> {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(end = 8.dp)
                        ) {
                            items(dates) { date ->
                                FilterChip(
                                    selected = date == selectedDate,
                                    onClick = { selectedDate = date },
                                    label = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(Formatters.formatDayShort(date), style = MaterialTheme.typography.labelSmall)
                                            Text(Formatters.formatChipDate(date))
                                        }
                                    }
                                )
                            }
                        }
                        IconButton(onClick = { showDateFilter = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Расширить диапазон")
                        }
                    }

                    ApexSecondaryButton(
                        text = "Расширить диапазон",
                        onClick = { showDateFilter = true },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (noSlotsWarning) {
                        Text(
                            "На выбранную дату слотов нет",
                            color = ApexError,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    when {
                        filteredSlots.isEmpty() -> EmptyState(
                            title = "Пока нет доступных заездов",
                            subtitle = "Загляните позже — расписание обновляется"
                        )
                        else -> LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredSlots, key = { it.id }) { slot ->
                                SlotCardItem(
                                    slot = slot,
                                    onClick = { onSlotClick(slot.id) },
                                    onInfoClick = { showTrackConfig = slot.trackConfiguration }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotCardItem(
    slot: Slot,
    onClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val clickable = slot.isBookable
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (clickable) 1f else 0.6f)
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${Formatters.formatTime(slot.startsAt)} · ${slot.trackConfiguration.label}",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onInfoClick, modifier = Modifier.height(24.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Конфигурация", modifier = Modifier.height(20.dp))
                }
            }
            Text("Маршал: ${slot.marshal.name}", color = ApexMuted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("С прокатом: ${Formatters.formatPrice(slot.priceWithRental)}")
            Text("Со своей: ${Formatters.formatPrice(slot.priceOwnGear)}")
            Spacer(Modifier.height(8.dp))
            when {
                slot.status == SlotStatus.CancelledByCenter -> Text("Отменён центром", color = ApexError)
                slot.safeAvailableSeats <= 0 -> Text("Мест нет", color = ApexError)
                else -> Text("Мест: ${slot.safeAvailableSeats}/${slot.totalSeats}")
            }
        }
    }
}