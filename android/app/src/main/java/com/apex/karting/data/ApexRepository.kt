package com.apex.karting.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val Context.dataStore by preferencesDataStore("apex_session")

class ApexRepository private constructor(private val context: Context) {

    private val tokenKey = stringPreferencesKey("access_token")
    private val phoneKey = stringPreferencesKey("phone")

    private val _slots = MutableStateFlow(MockData.generateSlots())
    private val _bookings = MutableStateFlow(MockData.generateInitialBookings(_slots.value))
    private var currentClient: Client? = null

    private var smsAttemptsLeft = 5
    private var codeAttemptsLeft = 5
    private var pendingPhone: String? = null
    // Исправление: таймер теперь привязан к номеру телефона
    private val smsSentMap = mutableMapOf<String, Instant>()

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        !prefs[tokenKey].isNullOrBlank()
    }

    val slots: Flow<List<Slot>> = _slots.asStateFlow()
    val bookings: Flow<List<Booking>> = _bookings.asStateFlow()

    suspend fun checkSession(): Boolean {
        val prefs = context.dataStore.data.first()
        val token = prefs[tokenKey]
        val phone = prefs[phoneKey]
        if (token.isNullOrBlank() || phone.isNullOrBlank()) return false
        currentClient = Client(phone, LoyaltyStatus.Bronze, 10)
        return true
    }

    suspend fun sendSmsCode(phone: String) {
        simulateNetwork()
        if (!phone.matches(Regex("^\\+7\\d{10}$"))) {
            throw ApiException(ApiErrorCode.INVALID_PHONE, "Проверьте номер телефона")
        }
        if (smsAttemptsLeft <= 0) {
            throw ApiException(ApiErrorCode.TOO_MANY_ATTEMPTS, "Слишком много попыток. Свяжитесь с поддержкой")
        }
        // Исправление: проверяем таймер для конкретного номера
        val lastSent = smsSentMap[phone]
        if (lastSent != null && Instant.now().isBefore(lastSent.plusSeconds(60))) {
            val retry = 60 - (Instant.now().epochSecond - lastSent.epochSecond)
            throw ApiException(ApiErrorCode.RATE_LIMITED, "Повторная отправка доступна через $retry секунд", retry.toInt())
        }
        pendingPhone = phone
        // Исправление: сохраняем время отправки для конкретного номера
        smsSentMap[phone] = Instant.now()
        smsAttemptsLeft--
    }

    suspend fun verifySmsCode(code: String): Client {
        simulateNetwork()
        if (!code.matches(Regex("^\\d{4,6}$"))) {
            throw ApiException(ApiErrorCode.INVALID_CODE, "Код должен содержать 4–6 цифр")
        }
        if (code != MockData.DEMO_SMS_CODE) {
            codeAttemptsLeft--
            if (codeAttemptsLeft <= 0) {
                throw ApiException(ApiErrorCode.TOO_MANY_ATTEMPTS, "Слишком много попыток. Свяжитесь с поддержкой")
            }
            throw ApiException(ApiErrorCode.INVALID_CODE, "Неверный код. Осталось попыток — $codeAttemptsLeft")
        }
        val phone = pendingPhone ?: throw ApiException(ApiErrorCode.INVALID_PHONE, "Сначала запросите SMS-код")
        val client = Client(phone, LoyaltyStatus.Bronze, 10)
        currentClient = client
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = "mock-jwt-${System.currentTimeMillis()}"
            prefs[phoneKey] = phone
        }
        codeAttemptsLeft = 5
        return client
    }

    fun getCodeAttemptsLeft(): Int = codeAttemptsLeft

    suspend fun logout() {
        currentClient = null
        pendingPhone = null
        // Исправление: очищаем все таймеры при выходе
        smsSentMap.clear()
        context.dataStore.edit { it.clear() }
    }

    suspend fun getClient(): Client {
        simulateNetwork()
        return currentClient ?: throw ApiException(ApiErrorCode.NETWORK, "Не авторизован")
    }

    suspend fun getSlots(from: LocalDate, to: LocalDate): List<Slot> {
        simulateNetwork()
        return _slots.value.filter { slot ->
            val date = slot.startsAt.atZone(ZoneId.systemDefault()).toLocalDate()
            !date.isBefore(from) && !date.isAfter(to)
        }
    }

    suspend fun getSlot(slotId: String): Slot {
        simulateNetwork()
        return _slots.value.find { it.id == slotId }
            ?: throw ApiException(ApiErrorCode.NOT_FOUND, "Слот не найден")
    }

    suspend fun getBookings(group: BookingGroup): List<Booking> {
        simulateNetwork()
        refreshBookingStates()
        return _bookings.value.filter { booking ->
            when (group) {
                BookingGroup.active -> booking.displayStatus in listOf(
                    DisplayBookingStatus.Pending,
                    DisplayBookingStatus.Paid,
                    DisplayBookingStatus.Expired
                )
                BookingGroup.past -> booking.displayStatus == DisplayBookingStatus.Completed
                BookingGroup.cancelled -> booking.displayStatus in listOf(
                    DisplayBookingStatus.CancelledByClient,
                    DisplayBookingStatus.CancelledByCenter
                )
            }
        }.sortedByDescending { it.slot?.startsAt ?: Instant.EPOCH }
    }

    suspend fun getBooking(bookingId: String): Booking {
        simulateNetwork()
        refreshBookingStates()
        return _bookings.value.find { it.id == bookingId }
            ?: throw ApiException(ApiErrorCode.NOT_FOUND, "Бронь не найдена")
    }

    suspend fun createBooking(slotId: String, gearOption: GearOption): Booking {
        simulateNetwork()
        val slot = _slots.value.find { it.id == slotId }
            ?: throw ApiException(ApiErrorCode.NOT_FOUND, "Слот не найден")

        if (slot.status == SlotStatus.CancelledByCenter) {
            throw ApiException(
                ApiErrorCode.SLOT_CANCELLED,
                "${slot.cancellationReason ?: "Заезд был отменён центром"}. Выберите другое время"
            )
        }
        if (slot.safeAvailableSeats <= 0) {
            throw ApiException(ApiErrorCode.NO_SEATS, "Мест на этот заезд больше нет")
        }
        val activeExists = _bookings.value.any {
            it.displayStatus in listOf(DisplayBookingStatus.Pending, DisplayBookingStatus.Paid) &&
                    it.slotId == slotId
        }
        if (activeExists) {
            throw ApiException(ApiErrorCode.BOOKING_ALREADY_EXISTS, "У вас уже есть бронь на этот заезд")
        }
        // Временно закомментировано для тестирования (чтобы можно было создавать несколько броней)
        // В продакшене эту проверку нужно включить обратно
        /*
        val hasAnyActive = _bookings.value.any {
            it.displayStatus in listOf(DisplayBookingStatus.Pending, DisplayBookingStatus.Paid)
        }
        if (hasAnyActive) {
            throw ApiException(ApiErrorCode.BOOKING_ALREADY_EXISTS, "У вас уже есть активная бронь")
        }
        */

        val client = getClient()
        val finalPrice = slot.finalPrice(gearOption, client.discountPercent)
        val updatedSlot = slot.copy(availableSeats = slot.availableSeats - 1)
        _slots.value = _slots.value.map { if (it.id == slotId) updatedSlot else it }

        val booking = Booking(
            id = newId(),
            slotId = slotId,
            gearOption = gearOption,
            finalPrice = finalPrice,
            depositLink = "https://pay.example.com/deposit/${System.currentTimeMillis()}",
            paymentStatus = BookingPaymentStatus.Pending,
            status = BookingStatus.Created,
            rated = false,
            slot = updatedSlot,
            canCancel = canCancelSlot(updatedSlot.startsAt)
        )
        _bookings.value = _bookings.value + booking
        return booking
    }

    suspend fun cancelBooking(bookingId: String): Booking {
        simulateNetwork()
        val booking = getBooking(bookingId)
        if (!booking.canCancel) {
            throw ApiException(
                ApiErrorCode.CANCEL_WINDOW_CLOSED,
                "Время отмены истекло. Свяжитесь с администратором"
            )
        }
        val slot = booking.slot
        if (slot != null) {
            _slots.value = _slots.value.map {
                if (it.id == slot.id) it.copy(availableSeats = it.availableSeats + 1) else it
            }
        }
        val updated = booking.copy(
            status = BookingStatus.CancelledByClient,
            paymentStatus = BookingPaymentStatus.Expired,
            canCancel = false,
            slot = slot?.copy(availableSeats = (slot.availableSeats + 1).coerceAtMost(slot.totalSeats))
        )
        _bookings.value = _bookings.value.map { if (it.id == bookingId) updated else it }
        return updated
    }

    suspend fun rateMarshal(slotId: String, score: Int): Boolean {
        simulateNetwork()
        val booking = _bookings.value.find { it.slotId == slotId && it.displayStatus == DisplayBookingStatus.Completed }
            ?: throw ApiException(ApiErrorCode.RATING_NOT_ALLOWED, "Оценка доступна только после завершения заезда")

        if (booking.rated) {
            throw ApiException(ApiErrorCode.ALREADY_RATED, "Вы уже оценили этот заезд")
        }
        val slot = booking.slot
        if (slot != null && Instant.now().isBefore(slot.endsAt)) {
            throw ApiException(ApiErrorCode.RATING_NOT_ALLOWED, "Оценка доступна только после завершения заезда")
        }
        _bookings.value = _bookings.value.map {
            if (it.id == booking.id) it.copy(rated = true) else it
        }
        return true
    }

    private fun refreshBookingStates() {
        _bookings.value = _bookings.value.map { booking ->
            val slot = booking.slot ?: return@map booking
            var updated = booking
            if (booking.displayStatus == DisplayBookingStatus.Pending &&
                booking.createdAt.isBefore(Instant.now().minusSeconds(1800))
            ) {
                updated = updated.copy(paymentStatus = BookingPaymentStatus.Expired)
            }
            if (updated.displayStatus == DisplayBookingStatus.Paid && Instant.now().isAfter(slot.endsAt)) {
                updated = updated.copy(status = BookingStatus.Completed)
            }
            updated.copy(canCancel = canCancelSlot(slot.startsAt) && updated.displayStatus in listOf(
                DisplayBookingStatus.Pending,
                DisplayBookingStatus.Paid
            ))
        }
    }

    private fun canCancelSlot(startsAt: Instant): Boolean =
        Instant.now().plusSeconds(7200).isBefore(startsAt) ||
                Instant.now().plusSeconds(7200).equals(startsAt)

    private suspend fun simulateNetwork() {
        delay(400)
    }

    companion object {
        @Volatile
        private var instance: ApexRepository? = null

        fun getInstance(context: Context): ApexRepository {
            return instance ?: synchronized(this) {
                instance ?: ApexRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}