package com.apex.karting.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val dateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormat = DateTimeFormatter.ofPattern("d MMMM, HH:mm", Locale("ru"))
    private val dayShortFormat = DateTimeFormatter.ofPattern("EE", Locale("ru"))
    private val chipDateFormat = DateTimeFormatter.ofPattern("d")

    fun formatDate(instant: Instant): String =
        instant.atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormat)

    fun formatTime(instant: Instant): String =
        instant.atZone(ZoneId.systemDefault()).toLocalTime().format(timeFormat)

    fun formatDateTime(instant: Instant): String =
        instant.atZone(ZoneId.systemDefault()).format(dateTimeFormat)

    fun formatPrice(price: Double): String = String.format(Locale("ru"), "%,d ₽", price.toInt())

    fun formatDayShort(date: LocalDate): String = date.format(dayShortFormat)

    fun formatChipDate(date: LocalDate): String = date.format(chipDateFormat)

    fun toLocalDate(instant: Instant): LocalDate =
        instant.atZone(ZoneId.systemDefault()).toLocalDate()
}