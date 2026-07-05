package com.apex.karting.data

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

enum class TrackConfiguration(val label: String, val description: String) {
    Short("Short", "Короткая конфигурация трассы (800 м) — идеально для новичков"),
    Long("Long", "Длинная конфигурация трассы (1400 м) — для опытных пилотов")
}

enum class SlotStatus { Open, CancelledByCenter }

enum class GearOption(val label: String) {
    Rental("С прокатной экипировкой"),
    Own("Со своей экипировкой")
}

enum class LoyaltyStatus(val label: String, val discountPercent: Int) {
    None("Нет статуса", 0),
    Bronze("Бронза", 10),
    Silver("Серебро", 20)
}

enum class BookingPaymentStatus { Pending, Paid, Expired }

enum class BookingStatus {
    Created, Paid, Completed, CancelledByClient, CancelledByCenter
}

enum class BookingGroup { active, past, cancelled }

enum class ApiErrorCode {
    INVALID_PHONE,
    INVALID_CODE,
    TOO_MANY_ATTEMPTS,
    RATE_LIMITED,
    NO_SEATS,
    BOOKING_ALREADY_EXISTS,
    SLOT_CANCELLED,
    CANCEL_WINDOW_CLOSED,
    ALREADY_RATED,
    RATING_NOT_ALLOWED,
    NOT_FOUND,
    NETWORK
}

class ApiException(val code: ApiErrorCode, override val message: String, val retryAfterSeconds: Int? = null) :
    Exception(message)

data class Marshal(val id: String, val name: String)

data class Slot(
    val id: String,
    val startsAt: Instant,
    val endsAt: Instant,
    val trackConfiguration: TrackConfiguration,
    val marshal: Marshal,
    val totalSeats: Int,
    val availableSeats: Int,
    val priceWithRental: Double,
    val priceOwnGear: Double,
    val status: SlotStatus,
    val cancellationReason: String? = null
) {
    val safeAvailableSeats: Int get() = availableSeats.coerceAtLeast(0)
    val isBookable: Boolean get() = status == SlotStatus.Open && safeAvailableSeats > 0
}

data class Client(
    val phone: String,
    val loyaltyStatus: LoyaltyStatus,
    val discountPercent: Int = loyaltyStatus.discountPercent
)

data class Booking(
    val id: String,
    val slotId: String,
    val gearOption: GearOption,
    val finalPrice: Double,
    val depositLink: String,
    val paymentStatus: BookingPaymentStatus,
    val status: BookingStatus,
    val rated: Boolean,
    val slot: Slot? = null,
    val cancellationReason: String? = null,
    val canCancel: Boolean = false,
    val createdAt: Instant = Instant.now()
) {
    val displayStatus: DisplayBookingStatus
        get() = when {
            status == BookingStatus.CancelledByClient -> DisplayBookingStatus.CancelledByClient
            status == BookingStatus.CancelledByCenter -> DisplayBookingStatus.CancelledByCenter
            status == BookingStatus.Completed -> DisplayBookingStatus.Completed
            paymentStatus == BookingPaymentStatus.Expired -> DisplayBookingStatus.Expired
            status == BookingStatus.Paid || paymentStatus == BookingPaymentStatus.Paid -> DisplayBookingStatus.Paid
            else -> DisplayBookingStatus.Pending
        }
}

enum class DisplayBookingStatus(val label: String) {
    Pending("Ожидает оплаты"),
    Paid("Оплачена"),
    Expired("Истекла"),
    Completed("Состоялась"),
    CancelledByClient("Отменена вами"),
    CancelledByCenter("Отменена центром")
}

data class DateRange(val from: LocalDate, val to: LocalDate)

fun Slot.finalPrice(gear: GearOption, discountPercent: Int): Double {
    val base = when (gear) {
        GearOption.Rental -> priceWithRental
        GearOption.Own -> priceOwnGear
    }
    return base * (1 - discountPercent / 100.0)
}

fun newId(): String = UUID.randomUUID().toString()