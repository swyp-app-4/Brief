package com.swyp.brife.ui.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun formatDisplayDate(value: String): String {
    val trimmedValue = value.trim()
    if (!ISO_LOCAL_DATE_PATTERN.matches(trimmedValue)) return value

    return runCatching {
        LocalDate.parse(trimmedValue, DateTimeFormatter.ISO_LOCAL_DATE)
            .format(DISPLAY_DATE_FORMATTER)
    }.getOrElse { value }
}

private val ISO_LOCAL_DATE_PATTERN = Regex("""\d{4}-\d{2}-\d{2}""")
private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")
