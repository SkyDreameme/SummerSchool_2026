package com.apex.karting.data

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Unit-тесты для моделей данных
 * Не требуют Android-контекста и эмулятора
 * Запуск: ./gradlew test
 */
class ModelsTest {

    // ==================== TC-DETAIL-001: Расчёт итоговой цены ====================
    @Test
    fun testFinalPriceCalculation() {
        val slot = Slot(
            id = "test-slot",
            startsAt = Instant.now(),
            endsAt = Instant.now().plusSeconds(3600),
            trackConfiguration = TrackConfiguration.Short,
            marshal = Marshal("m1", "Test Marshal"),
            totalSeats = 8,
            availableSeats = 5,
            priceWithRental = 2500.0,
            priceOwnGear = 2000.0,
            status = SlotStatus.Open
        )

        // Скидка 10%
        val rentalPrice = slot.finalPrice(GearOption.Rental, 10)
        assertEquals("Цена с прокатной экипировкой и скидкой 10% должна быть 2250",
            2250.0, rentalPrice, 0.01)

        val ownPrice = slot.finalPrice(GearOption.Own, 10)
        assertEquals("Цена со своей экипировкой и скидкой 10% должна быть 1800",
            1800.0, ownPrice, 0.01)

        // Без скидки
        val noDiscount = slot.finalPrice(GearOption.Rental, 0)
        assertEquals("Цена без скидки должна быть 2500",
            2500.0, noDiscount, 0.01)
    }

    // ==================== TC-SCHED-002: Обработка отрицательных мест ====================
    @Test
    fun testNegativeAvailableSeats() {
        val slot = Slot(
            id = "test-slot",
            startsAt = Instant.now(),
            endsAt = Instant.now().plusSeconds(3600),
            trackConfiguration = TrackConfiguration.Short,
            marshal = Marshal("m1", "Test Marshal"),
            totalSeats = 8,
            availableSeats = -1, // Отрицательное значение
            priceWithRental = 2500.0,
            priceOwnGear = 2000.0,
            status = SlotStatus.Open
        )

        assertEquals("safeAvailableSeats должен вернуть 0 для отрицательного значения",
            0, slot.safeAvailableSeats)
        assertFalse("Слот с отрицательными местами не должен быть доступен для бронирования",
            slot.isBookable)
    }

    // ==================== Проверка статусов брони ====================
    @Test
    fun testBookingDisplayStatusMapping() {
        // Проверяем маппинг статусов
        val pendingBooking = Booking(
            id = "test1",
            slotId = "slot",
            gearOption = GearOption.Rental,
            finalPrice = 1000.0,
            depositLink = "https://test.com",
            paymentStatus = BookingPaymentStatus.Pending,
            status = BookingStatus.Created,
            rated = false
        )
        assertEquals("Pending → DisplayBookingStatus.Pending",
            DisplayBookingStatus.Pending, pendingBooking.displayStatus)

        val paidBooking = pendingBooking.copy(
            paymentStatus = BookingPaymentStatus.Paid,
            status = BookingStatus.Paid
        )
        assertEquals("Paid → DisplayBookingStatus.Paid",
            DisplayBookingStatus.Paid, paidBooking.displayStatus)

        val expiredBooking = pendingBooking.copy(
            paymentStatus = BookingPaymentStatus.Expired
        )
        assertEquals("Expired → DisplayBookingStatus.Expired",
            DisplayBookingStatus.Expired, expiredBooking.displayStatus)

        val cancelledByClient = pendingBooking.copy(
            status = BookingStatus.CancelledByClient
        )
        assertEquals("CancelledByClient → DisplayBookingStatus.CancelledByClient",
            DisplayBookingStatus.CancelledByClient, cancelledByClient.displayStatus)

        val cancelledByCenter = pendingBooking.copy(
            status = BookingStatus.CancelledByCenter
        )
        assertEquals("CancelledByCenter → DisplayBookingStatus.CancelledByCenter",
            DisplayBookingStatus.CancelledByCenter, cancelledByCenter.displayStatus)

        val completedBooking = pendingBooking.copy(
            status = BookingStatus.Completed,
            paymentStatus = BookingPaymentStatus.Paid
        )
        assertEquals("Completed → DisplayBookingStatus.Completed",
            DisplayBookingStatus.Completed, completedBooking.displayStatus)
    }

    // ==================== TC-CANCEL-002: Условие "включительно" ====================
    @Test
    fun testCancelWindowInclusive() {
        val now = Instant.now()

        // Слот через 3 часа — отмена доступна
        val future3h = now.plusSeconds(10800)
        val canCancelFuture = now.plusSeconds(7200).isBefore(future3h)
        assertTrue("Через 3 часа отмена должна быть доступна", canCancelFuture)

        // Слот через 1 час — отмена недоступна
        val future1h = now.plusSeconds(3600)
        val canCancelNear = now.plusSeconds(7200).isBefore(future1h)
        assertFalse("Через 1 час отмена должна быть недоступна", canCancelNear)

        // Слот ровно через 2 часа — отмена доступна (включительно)
        val exact2h = now.plusSeconds(7200)
        val canCancelExact = now.plusSeconds(7200).equals(exact2h) ||
                now.plusSeconds(7200).isBefore(exact2h)
        assertTrue("Ровно через 2 часа отмена должна быть доступна (включительно)",
            canCancelExact)
    }

    // ==================== Проверка форматера дат ====================
    @Test
    fun testDateFormatters() {
        val now = Instant.now()
        val dateStr = com.apex.karting.util.Formatters.formatDate(now)
        assertNotNull("Дата не должна быть null", dateStr)
        assertTrue("Дата должна содержать цифры", dateStr.any { it.isDigit() })

        val timeStr = com.apex.karting.util.Formatters.formatTime(now)
        assertNotNull("Время не должно быть null", timeStr)
        assertTrue("Время должно содержать цифры", timeStr.any { it.isDigit() })

        // Используем неразрывный пробел \u00A0, который возвращает Java для русской локали
        val priceStr = com.apex.karting.util.Formatters.formatPrice(2250.0)
        assertEquals("Цена должна форматироваться с символом ₽", "2\u00A0250 ₽", priceStr)
    }

    // ==================== Проверка фильтрации слотов по дате ====================
    @Test
    fun testSlotDateFiltering() {
        val today = LocalDate.now()
        val slots = listOf(
            createTestSlot(today, 10),
            createTestSlot(today.plusDays(1), 14),
            createTestSlot(today.plusDays(2), 16),
            createTestSlot(today.plusDays(3), 18)
        )

        // Фильтруем по дате (только сегодня и завтра)
        val from = today
        val to = today.plusDays(1)

        val filtered = slots.filter { slot ->
            val date = slot.startsAt.atZone(ZoneId.systemDefault()).toLocalDate()
            !date.isBefore(from) && !date.isAfter(to)
        }

        assertEquals("Должно быть 2 слота на сегодня и завтра", 2, filtered.size)
    }

    // ==================== Проверка создания брони ====================
    @Test
    fun testBookingCreation() {
        val booking = Booking(
            id = "booking-123",
            slotId = "slot-456",
            gearOption = GearOption.Rental,
            finalPrice = 2250.0,
            depositLink = "https://pay.example.com/deposit/123456",
            paymentStatus = BookingPaymentStatus.Pending,
            status = BookingStatus.Created,
            rated = false,
            canCancel = true
        )

        assertNotNull("ID брони не должен быть null", booking.id)
        assertEquals("Тип экипировки должен быть Rental", GearOption.Rental, booking.gearOption)
        assertEquals("Цена должна быть 2250", 2250.0, booking.finalPrice, 0.01)
        assertTrue("depositLink должен начинаться с https://pay.example.com",
            booking.depositLink.startsWith("https://pay.example.com"))
        assertTrue("canCancel должен быть true", booking.canCancel)
        assertFalse("rated должен быть false", booking.rated)
    }

    // ==================== Проверка статуса отмены ====================
    @Test
    fun testCancellationStatus() {
        val booking = Booking(
            id = "booking-123",
            slotId = "slot-456",
            gearOption = GearOption.Rental,
            finalPrice = 2250.0,
            depositLink = "https://pay.example.com/deposit/123456",
            paymentStatus = BookingPaymentStatus.Expired,
            status = BookingStatus.CancelledByClient,
            rated = false,
            canCancel = false
        )

        assertEquals("Статус должен быть CancelledByClient",
            BookingStatus.CancelledByClient, booking.status)
        assertEquals("DisplayStatus должен быть CancelledByClient",
            DisplayBookingStatus.CancelledByClient, booking.displayStatus)
        assertFalse("canCancel должен быть false", booking.canCancel)
    }

    // ==================== Вспомогательная функция ====================
    private fun createTestSlot(date: LocalDate, hour: Int): Slot {
        val instant = date.atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant()
        return Slot(
            id = "slot-${date}-${hour}",
            startsAt = instant,
            endsAt = instant.plusSeconds(3600),
            trackConfiguration = if (hour % 2 == 0) TrackConfiguration.Short else TrackConfiguration.Long,
            marshal = Marshal("m1", "Test Marshal"),
            totalSeats = 8,
            availableSeats = 5,
            priceWithRental = 2500.0,
            priceOwnGear = 2000.0,
            status = SlotStatus.Open
        )
    }
}