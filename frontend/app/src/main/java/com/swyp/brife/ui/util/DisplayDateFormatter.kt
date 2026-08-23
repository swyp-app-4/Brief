package com.swyp.brife.ui.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun formatDisplayDate(value: String): String {
    val trimmedValue = value.trim()
    val datePart = ISO_DATE_OR_TIMESTAMP_PATTERN
        .matchEntire(trimmedValue)
        ?.groupValues
        ?.get(1)
        ?: return value

    return runCatching {
        LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
            .format(DISPLAY_DATE_FORMATTER)
    }.getOrElse { value }
}

private val ISO_DATE_OR_TIMESTAMP_PATTERN = Regex(
    """^(\d{4}-\d{2}-\d{2})(?:T\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:\d{2})?)?$"""
)
private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")
