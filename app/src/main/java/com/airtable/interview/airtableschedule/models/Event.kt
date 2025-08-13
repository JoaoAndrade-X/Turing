package com.airtable.interview.airtableschedule.models

import java.util.Date

data class Event(
    val id: Int,
    val startDate: Date,
    val endDate: Date,
    val name: String
) {
    fun startDay(): Date = Date(startDate.year, startDate.month, startDate.date)
    fun endDay(): Date = Date(endDate.year, endDate.month, endDate.date)

    fun durationInDays(): Int {
        val start = startDay().time
        val end = endDay().time
        val millisPerDay = 1000 * 60 * 60 * 24
        return ((end - start) / millisPerDay).toInt() + 1
    }
}