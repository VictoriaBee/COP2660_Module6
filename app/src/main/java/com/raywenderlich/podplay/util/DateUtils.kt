package com.raywenderlich.podplay.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun jsonDateToShortDate(jsonDate: String?): String {
        // 1 - Checks if jsonDate string is not null.
        if (jsonDate == null) {
            // If null, returns "-", showing no date was provided.
            return "-"
        }

        // 2 - Defines SimpleDateFormat to match date format returned by iTunes.
        val inFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
        Locale.getDefault())
        // 3 - Parse jsonDate string and places it into Date obj named date.
        val date = inFormat.parse(jsonDate) ?: return "-"
        // 4 - Output format is defined as a short date
        // to match the currently defined locale.
        val outputFormat =
            DateFormat.getDateInstance(DateFormat.SHORT,
            Locale.getDefault())
        // 5 - Date is formatted and returned.
        return outputFormat.format(date)
    }

    // Converts a date string found in the RSS XML feed to a Date object.
    fun xmlDateToDate(dateString: String?): Date {
        val date = dateString ?: return Date()
        val inFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
            Locale.getDefault())
        return inFormat.parse(date) ?: Date()
    }

    // Helper method to convert a Date object to
    // a short date formatted string.
    fun dateToShortDate(date: Date): String {
        val outputFormat = DateFormat.getDateInstance(
            DateFormat.SHORT, Locale.getDefault())
        return outputFormat.format(date)
    }
}