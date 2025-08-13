package com.airtable.interview.airtableschedule.repositories

import com.airtable.interview.airtableschedule.models.Event
import com.airtable.interview.airtableschedule.models.SampleTimelineItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * A store for data related to events. Currently, this just returns sample data.
 */
interface EventDataRepository {
    fun getTimelineItems(): Flow<List<Event>>
    suspend fun updateEvent(event: Event)
}

class EventDataRepositoryImpl : EventDataRepository {
    private var events = SampleTimelineItems.timelineItems.toMutableList()

    override fun getTimelineItems(): Flow<List<Event>> = flowOf(events.toList())

    override suspend fun updateEvent(event: Event) {
        val index = events.indexOfFirst { it.id == event.id }
        if (index != -1) {
            events[index] = event
        }
    }
}
