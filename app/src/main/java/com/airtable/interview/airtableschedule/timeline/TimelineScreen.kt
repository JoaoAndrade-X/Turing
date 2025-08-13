package com.airtable.interview.airtableschedule.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airtable.interview.airtableschedule.models.Event
import com.airtable.interview.airtableschedule.models.assignLanes
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimelineView(
        events = uiState.events,
        selectedEventId = uiState.selectedEventId,
        onEventSelected = { viewModel.selectedEvent(it) },
        onEventUpdated = { viewModel.updateEvent(it) }
    )
}

@Composable
private fun TimelineView(
    events: List<Event>,
    selectedEventId: Int?,
    onEventSelected: (Int) -> Unit,
    onEventUpdated: (Event) -> Unit
) {
    val lanes = remember(events) { assignLanes(events) }
    val allEvents = events
    val minDate = remember(allEvents) {
        allEvents.minOfOrNull { it.startDay() } ?: Date()
    }
    val maxDate = remember(allEvents) {
        allEvents.maxOfOrNull { it.endDay() } ?: Date()
    }
    val totalDays = remember(minDate, maxDate) {
        val start = minDate.time
        val end = maxDate.time
        val dayMs = 24 * 60 * 60 * 1000L
        ((end - start) / dayMs + 1).toInt()
    }

    val density = LocalDensity.current
    var dayWidthPx by remember { mutableStateOf(100f) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { dayWidthPx *= 1.2f }) {
                Text("Zoom In")
            }
            Button(onClick = { dayWidthPx *= 0.8f }) {
                Text("Zoom Out")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 20.dp)
                .onGloballyPositioned { cords ->
                    dayWidthPx = (cords.size.width.toFloat() / totalDays)
                },
            horizontalArrangement = Arrangement.Start
        ) {
            for (i in 0 until totalDays) {
                val date = Date(minDate.year, minDate.month, minDate.date + i)
                Text(
                    text = "${date.month + 1}/${date.date}",
                    modifier = Modifier.width(with(density) { dayWidthPx.dp }),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        HorizontalDivider()

        LazyColumn {
            items(lanes) { lane ->
                TimelineLane(
                    lane = lane,
                    minDate = minDate,
                    dayWidthPx = dayWidthPx,
                    selectedEventId = selectedEventId,
                    onEventSelected = onEventSelected,
                    onEventUpdated = onEventUpdated,
                )
            }
        }
    }
}

@Composable
fun TimelineLane(
    lane: List<Event>,
    minDate: Date,
    dayWidthPx: Float,
    selectedEventId: Int?,
    onEventSelected: (Int) -> Unit,
    onEventUpdated: (Event) -> Unit
) {
    val dayMs = 24 * 60 * 60 * 1000L
    val minTime = minDate.time

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier
            .padding(horizontal = 12.dp)
            .weight(1f)
        ) {
            for(event in lane) {
                val duration = event.durationInDays()
                val offsetDays = ((event.startDay().time - minTime) / dayMs).toInt()
                val left by remember { mutableStateOf(offsetDays * dayWidthPx) }
                val width by remember { mutableStateOf(duration * dayWidthPx) }

                var position by remember(event) { mutableStateOf(Offset(left, 0f)) }

                val isSelected = selectedEventId == event.id
                val color = if (isSelected) Color(0xFF2196F3) else MaterialTheme.colorScheme.primary

                Box(
                    modifier = Modifier
                        .offset(x = position.x.dp)
                        .width(width.dp)
                        .height(40.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(color)
                        .clickable { onEventSelected(event.id) }
                        .pointerInput(event) {
                            detectDragGestures(
                                onDragStart = { onEventSelected(event.id) },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    position = Offset(position.x + dragAmount.x, 0f)
                                },
                                onDragEnd = {
                                    val daysFromStart = (position.x / dayWidthPx).roundToInt()
                                    val durationInDays = (width / dayWidthPx).roundToInt()

                                    val newStart = Date(minDate.time + daysFromStart * dayMs)
                                    val newEnd = Date(newStart.time + (durationInDays - 1) * dayMs)

                                    val finalEvent = event.copy(
                                        startDate = newStart,
                                        endDate = newEnd
                                    )
                                    onEventUpdated(finalEvent)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.name,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                if (isSelected) {
                    Canvas(
                        modifier = Modifier
                            .offset(x = position.x.dp)
                            .width(4.dp)
                            .height(40.dp)
                    ) {
                        drawCircle(Color.White, radius = 6f, center = Offset(2f, size.height / 2))
                    }
                    Canvas(
                        modifier = Modifier
                            .offset(x = (position.x + width - 4).dp)
                            .width(4.dp)
                            .height(40.dp)
                    ) {
                        drawCircle(Color.White, radius = 6f, center = Offset(2f, size.height / 2))
                    }
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
fun Preview() {
    TimelineScreen()
}
