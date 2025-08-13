package com.airtable.interview.airtableschedule.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airtable.interview.airtableschedule.models.Event
import com.airtable.interview.airtableschedule.repositories.EventDataRepository
import com.airtable.interview.airtableschedule.repositories.EventDataRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the state of the timeline screen.
 */
class TimelineViewModel: ViewModel() {
    private val eventDataRepository: EventDataRepository = EventDataRepositoryImpl()

    private val _selectedEventId = MutableStateFlow<Int?>(null)
    private val _events = MutableStateFlow<List<Event>>(emptyList())

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            eventDataRepository.getTimelineItems().collect { events ->
                _events.value = events
            }
        }
    }

    val uiState: StateFlow<TimelineUiState> = combine(_events, _selectedEventId) { events, selectedId ->
        TimelineUiState(events = events, selectedEventId = selectedId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimelineUiState()
    )

    fun selectedEvent(eventId: Int) {
        _selectedEventId.value = eventId
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            eventDataRepository.updateEvent(event)

            val updatedEvents = _events.value.map { if (it.id == event.id) event else it }
            _events.value = updatedEvents
        }
    }
}
