package com.apex.karting.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object MockData {
    private val zone = ZoneId.systemDefault()

    val marshals = listOf(
        Marshal("m1", "Иван Петров"),
        Marshal("m2", "Алексей Смирнов"),
        Marshal("m3", "Дмитрий Козлов")
    )

    fun instant(date: LocalDate, hour: Int, minute: Int = 0): Instant =
        date.atTime(LocalTime.of(hour, minute)).atZone(zone).toInstant()

    fun generateSlots(): List<Slot> {
        val today = LocalDate.now()
        val slots = mutableListOf<Slot>()
        var marshalIdx = 0

        for (dayOffset in 0..13) {
            val date = today.plusDays(dayOffset.toLong())
            val times = listOf(10 to 11, 14 to 15, 16 to 17, 18 to 19)
            times.forEachIndexed { idx, (startH, endH) ->
                val config = if ((dayOffset + idx) % 2 == 0) TrackConfiguration.Short else TrackConfiguration.Long
                val total = if (config == TrackConfiguration.Short) 8 else 14
                val available = when {
                    dayOffset == 0 && idx == 1 -> 0
                    dayOffset == 1 && idx == 2 -> 1
                    dayOffset == 3 && idx == 0 -> 0
                    else -> (1..total).random()
                }
                val status = if (dayOffset == 5 && idx == 0) SlotStatus.CancelledByCenter else SlotStatus.Open
                val marshal = marshals[marshalIdx % marshals.size]
                marshalIdx++

                val rental = if (config == TrackConfiguration.Short) 2500.0 else 3500.0
                val own = if (config == TrackConfiguration.Short) 2000.0 else 2800.0

                slots += Slot(
                    id = "slot-${dayOffset}-${idx}",
                    startsAt = instant(date, startH),
                    endsAt = instant(date, endH),
                    trackConfiguration = config,
                    marshal = marshal,
                    totalSeats = total,
                    availableSeats = if (status == SlotStatus.CancelledByCenter) 0 else available,
                    priceWithRental = rental,
                    priceOwnGear = own,
                    status = status,
                    cancellationReason = if (status == SlotStatus.CancelledByCenter) "Технические работы на трассе" else null
                )
            }
        }
        return slots
    }

    fun generateInitialBookings(slots: List<Slot>): List<Booking> {
        val pastSlot = slots.firstOrNull { it.startsAt.isBefore(Instant.now().minusSeconds(3600 * 24)) }
            ?: slots[2]
        val futureSlot = slots.firstOrNull { it.isBookable && it.startsAt.isAfter(Instant.now().plusSeconds(3600 * 4)) }
            ?: slots[4]
        val cancelledSlot = slots.firstOrNull { it.status == SlotStatus.CancelledByCenter } ?: slots.last()

        return listOf(
            Booking(
                id = "booking-completed",
                slotId = pastSlot.id,
                gearOption = GearOption.Rental,
                finalPrice = pastSlot.finalPrice(GearOption.Rental, 10),
                depositLink = "https://pay.example.com/deposit/completed",
                paymentStatus = BookingPaymentStatus.Paid,
                status = BookingStatus.Completed,
                rated = false,
                slot = pastSlot.copy(availableSeats = pastSlot.totalSeats - 1),
                canCancel = false
            ),
            Booking(
                id = "booking-pending",
                slotId = futureSlot.id,
                gearOption = GearOption.Own,
                finalPrice = futureSlot.finalPrice(GearOption.Own, 10),
                depositLink = "https://pay.example.com/deposit/pending",
                paymentStatus = BookingPaymentStatus.Pending,
                status = BookingStatus.Created,
                rated = false,
                slot = futureSlot,
                canCancel = futureSlot.startsAt.isAfter(Instant.now().plusSeconds(7200))
            ),
            Booking(
                id = "booking-cancelled",
                slotId = cancelledSlot.id,
                gearOption = GearOption.Rental,
                finalPrice = cancelledSlot.finalPrice(GearOption.Rental, 10),
                depositLink = "https://pay.example.com/deposit/cancelled",
                paymentStatus = BookingPaymentStatus.Paid,
                status = BookingStatus.CancelledByCenter,
                rated = false,
                slot = cancelledSlot,
                cancellationReason = cancelledSlot.cancellationReason,
                canCancel = false
            )
        )
    }

    const val DEMO_SMS_CODE = "1234"
    const val SUPPORT_PHONE = "+74951234567"
    const val SUPPORT_TELEGRAM = "https://t.me/apex_karting"
}