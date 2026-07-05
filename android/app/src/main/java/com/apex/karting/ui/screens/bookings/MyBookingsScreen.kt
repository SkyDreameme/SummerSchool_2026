package com.apex.karting.ui.screens.bookings

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.apex.karting.data.ApexRepository
import com.apex.karting.data.Booking
import com.apex.karting.data.BookingGroup
import com.apex.karting.ui.components.EmptyState
import com.apex.karting.ui.components.ErrorState
import com.apex.karting.ui.components.SkeletonList
import com.apex.karting.ui.components.displayStatusColor
import com.apex.karting.util.Formatters
import kotlinx.coroutines.launch

enum class BookingTab(val label: String, val group: BookingGroup) {
    Active("Активные", BookingGroup.active),
    Past("Состоявшиеся", BookingGroup.past),
    Cancelled("Отменённые", BookingGroup.cancelled)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBookingClick: (String) -> Unit,
    onGoToSchedule: () -> Unit = {},
    modifier: Modifier = Modifier,
    initialTab: BookingTab = BookingTab.Active
) {
    val context = LocalContext.current
    val repository = remember { ApexRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(initialTab) }
    var bookings by remember { mutableStateOf<List<Booking>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    fun load(isRefresh: Boolean = false) {
        scope.launch {
            if (isRefresh) refreshing = true else loading = true
            error = false
            try {
                bookings = repository.getBookings(selectedTab.group)
            } catch (_: Exception) {
                error = true
            } finally {
                loading = false
                refreshing = false
            }
        }
    }

    LaunchedEffect(selectedTab) { load() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Мои заезды") },
                actions = {
                    IconButton(onClick = { load(isRefresh = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SingleChoiceSegmentedButtonRow(Modifier.padding(16.dp)) {
                BookingTab.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        shape = SegmentedButtonDefaults.itemShape(index, BookingTab.entries.size),
                        label = { Text(tab.label) }
                    )
                }
            }

            when {
                loading -> SkeletonList()
                error -> ErrorState(
                    title = "Не удалось загрузить записи",
                    subtitle = "Повторите через минуту",
                    onRetry = { load() }
                )
                bookings.isNullOrEmpty() -> when (selectedTab) {
                    BookingTab.Active -> EmptyState(
                        title = "У вас пока нет активных броней",
                        subtitle = "Забронируйте заезд в расписании",
                        actionLabel = "Посмотреть расписание",
                        onAction = onGoToSchedule
                    )
                    BookingTab.Past -> EmptyState(
                        title = "История пуста",
                        subtitle = "Ваш первый заезд появится здесь после оплаты"
                    )
                    BookingTab.Cancelled -> EmptyState(
                        title = "Отменённых заездов нет",
                        subtitle = ""
                    )
                }
                else -> {
                    // Здесь можно добавить индикатор обновления, если нужно
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bookings!!, key = { it.id }) { booking ->
                            BookingCardItem(booking, onClick = { onBookingClick(booking.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCardItem(booking: Booking, onClick: () -> Unit) {
    val slot = booking.slot
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (slot != null) {
                Text(Formatters.formatDateTime(slot.startsAt), style = MaterialTheme.typography.titleMedium)
                Text("${slot.trackConfiguration.label} · ${Formatters.formatPrice(booking.finalPrice)}")
            }
            Spacer(Modifier.height(8.dp))
            Row {
                Text(
                    booking.displayStatus.label,
                    color = displayStatusColor(booking.displayStatus),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}