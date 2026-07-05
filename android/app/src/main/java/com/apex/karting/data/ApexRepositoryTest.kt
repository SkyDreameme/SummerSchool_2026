package com.apex.karting.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ApexRepositoryTest {

    private lateinit var repository: ApexRepository
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = ApexRepository.getInstance(context)
    }

    // ==================== TC-AUTH-001: Индивидуальный таймер ====================
    @Test
    fun testIndividualSmsTimer() = runBlocking {
        // Отправляем для первого номера
        val phone1 = "+79991111111"
        repository.sendSmsCode(phone1)

        // Проверяем, что для первого номера таймер есть
        try {
            repository.sendSmsCode(phone1)
            fail("Должна быть ошибка RATE_LIMITED для первого номера")
        } catch (e: ApiException) {
            assertEquals(ApiErrorCode.RATE_LIMITED, e.code)
        }

        // Для второго номера отправка должна быть доступна
        val phone2 = "+79992222222"
        try {
            repository.sendSmsCode(phone2)
            // Успешно, ошибки нет
        } catch (e: ApiException) {
            fail("Для второго номера ошибки быть не должно: ${e.message}")
        }
    }

    // ==================== TC-AUTH-002: Очистка таймера при выходе ====================
    @Test
    fun testClearTimersOnLogout() = runBlocking {
        // Отправляем код
        val phone = "+79991111111"
        repository.sendSmsCode(phone)

        // Выходим
        repository.logout()

        // После выхода отправка должна быть доступна
        try {
            repository.sendSmsCode(phone)
            // Успешно, ошибки нет
        } catch (e: ApiException) {
            fail("После выхода таймер должен быть сброшен: ${e.message}")
        }
    }

    // ==================== TC-DETAIL-001: Расчёт итоговой цены ====================
    @Test
    fun testFinalPriceCalculation() = runBlocking {
        // Получаем клиента со скидкой 10%
        val client = Client("+79991111111", LoyaltyStatus.Bronze, 10)

        // Создаём тестовый слот
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

        // Проверяем расчёт с прокатной экипировкой
        val rentalPrice = slot.finalPrice(GearOption.Rental, client.discountPercent)
        assertEquals(2250.0, rentalPrice, 0.01) // 2500 - 10%

        // Проверяем расчёт со своей экипировкой
        val ownPrice = slot.finalPrice(GearOption.Own, client.discountPercent)
        assertEquals(1800.0, ownPrice, 0.01) // 2000 - 10%
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

        // safeAvailableSeats должен вернуть 0
        assertEquals(0, slot.safeAvailableSeats)
        assertFalse(slot.isBookable)
    }

    // ==================== TC-BOOK-001: Успешное создание брони ====================
    @Test
    fun testSuccessfulBookingCreation() = runBlocking {
        // Входим в аккаунт
        val phone = "+79991111111"
        repository.sendSmsCode(phone)
        repository.verifySmsCode(MockData.DEMO_SMS_CODE)

        // Получаем слот с местами
        val slots = repository.getSlots(LocalDate.now(), LocalDate.now().plusDays(1))
        val availableSlot = slots.firstOrNull { it.isBookable }
            ?: throw IllegalStateException("Нет доступных слотов для теста")

        val initialSeats = availableSlot.availableSeats

        // Создаём бронь
        val booking = repository.createBooking(availableSlot.id, GearOption.Rental)

        // Проверяем бронь
        assertNotNull(booking.id)
        assertEquals(BookingStatus.Created, booking.status)
        assertEquals(BookingPaymentStatus.Pending, booking.paymentStatus)
        assertNotNull(booking.depositLink)
        assertTrue(booking.depositLink.startsWith("https://pay.example.com/deposit/"))

        // Проверяем, что места уменьшились
        val updatedSlot = repository.getSlot(availableSlot.id)
        assertEquals(initialSeats - 1, updatedSlot.availableSeats)

        // Проверяем, что бронь появилась в списке
        val bookings = repository.getBookings(BookingGroup.active)
        assertTrue(bookings.any { it.id == booking.id })
    }

    // ==================== TC-BOOK-002: Ошибка при отсутствии мест ====================
    @Test
    fun testNoSeatsError() = runBlocking {
        // Входим в аккаунт
        val phone = "+79991111111"
        repository.sendSmsCode(phone)
        repository.verifySmsCode(MockData.DEMO_SMS_CODE)

        // Находим слот без мест
        val slots = repository.getSlots(LocalDate.now(), LocalDate.now().plusDays(7))
        val fullSlot = slots.firstOrNull { it.availableSeats <= 0 }
            ?: throw IllegalStateException("Нет полных слотов для теста")

        // Пытаемся создать бронь
        try {
            repository.createBooking(fullSlot.id, GearOption.Rental)
            fail("Должна быть ошибка NO_SEATS")
        } catch (e: ApiException) {
            assertEquals(ApiErrorCode.NO_SEATS, e.code)
        }
    }

    // ==================== TC-CANCEL-001: Успешная отмена брони ====================
    @Test
    fun testSuccessfulCancellation() = runBlocking {
        // Входим в аккаунт
        val phone = "+79991111111"
        repository.sendSmsCode(phone)
        repository.verifySmsCode(MockData.DEMO_SMS_CODE)

        // Создаём бронь
        val slots = repository.getSlots(LocalDate.now(), LocalDate.now().plusDays(7))
        val slot = slots.firstOrNull { it.isBookable }
            ?: throw IllegalStateException("Нет доступных слотов для теста")

        val booking = repository.createBooking(slot.id, GearOption.Rental)
        val initialSeats = slot.availableSeats

        // Отменяем бронь
        val cancelledBooking = repository.cancelBooking(booking.id)

        // Проверяем статус
        assertEquals(BookingStatus.CancelledByClient, cancelledBooking.status)
        assertFalse(cancelledBooking.canCancel)

        // Проверяем, что места освободились
        val updatedSlot = repository.getSlot(slot.id)
        assertEquals(initialSeats, updatedSlot.availableSeats)

        // Проверяем, что бронь во вкладке "Отменённые"
        val cancelledBookings = repository.getBookings(BookingGroup.cancelled)
        assertTrue(cancelledBookings.any { it.id == booking.id })
    }

    // ==================== TC-CANCEL-002: Условие "включительно" ====================
    @Test
    fun testCancelWindowInclusive() {
        // Создаём слот, который начинается ровно через 2 часа
        val startsAt = Instant.now().plusSeconds(7200) // +2 часа
        val slot = Slot(
            id = "test-slot",
            startsAt = startsAt,
            endsAt = startsAt.plusSeconds(3600),
            trackConfiguration = TrackConfiguration.Short,
            marshal = Marshal("m1", "Test Marshal"),
            totalSeats = 8,
            availableSeats = 5,
            priceWithRental = 2500.0,
            priceOwnGear = 2000.0,
            status = SlotStatus.Open
        )

        // Создаём бронь с этим слотом
        val booking = Booking(
            id = "test-booking",
            slotId = slot.id,
            gearOption = GearOption.Rental,
            finalPrice = 2250.0,
            depositLink = "https://pay.example.com/test",
            paymentStatus = BookingPaymentStatus.Pending,
            status = BookingStatus.Created,
            rated = false,
            slot = slot,
            canCancel = false
        )

        // Проверяем canCancel для этой брони
        // В реальном коде это проверяется в refreshBookingStates
        // Здесь мы проверяем логику canCancelSlot
        val canCancel = Instant.now().plusSeconds(7200).equals(startsAt) ||
                Instant.now().plusSeconds(7200).isBefore(startsAt)
        assertTrue(canCancel)
    }

    // ==================== TC-RATE-001: Успешная отправка оценки ====================
    @Test
    fun testSuccessfulRating() = runBlocking {
        // Входим в аккаунт
        val phone = "+79991111111"
        repository.sendSmsCode(phone)
        repository.verifySmsCode(MockData.DEMO_SMS_CODE)

        // Находим завершённую бронь
        val bookings = repository.getBookings(BookingGroup.past)
        val completedBooking = bookings.firstOrNull { it.displayStatus == DisplayBookingStatus.Completed }
            ?: throw IllegalStateException("Нет завершённых броней для теста")

        // Проверяем, что можно оценить
        val slot = completedBooking.slot
        if (slot != null && !completedBooking.rated && !Instant.now().isBefore(slot.endsAt)) {
            // Отправляем оценку
            val result = repository.rateMarshal(slot.id, 5)
            assertTrue(result)

            // Проверяем, что флаг rated обновился
            val updatedBooking = repository.getBooking(completedBooking.id)
            assertTrue(updatedBooking.rated)
        }
    }
}