package com.airtable.interview.airtableschedule.models

/**
 * Takes a list of [Event]s and assigns them to lanes based on start/end dates.
 */
fun assignLanes(events: List<Event>): List<List<Event>> {
    val sortedEvents = events.sortedBy { it.startDate }
    val lanes = mutableListOf<MutableList<Event>>()

    for(event in sortedEvents) {
        val lane = lanes.firstOrNull() { lane ->
            lane.last().endDay() < event.startDay()
        }
        if (lane != null) {
            lane.add(event)
        } else {
            lanes.add(mutableListOf(event))
        }
    }
    return lanes
}
