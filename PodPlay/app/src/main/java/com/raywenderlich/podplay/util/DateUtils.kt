package com.raywenderlich.podplay.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun jsonDateToShortDate(jsonDate: String?): String {
        jsonDate?.let {
            val inFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val date = inFormat.parse(it)
            val outputFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())

            return outputFormat.format(date)
        }
        return "-"
    }
}