package com.timeflow.model

import java.util.Date
import java.util.UUID

/** Enum class representing the different types of events. */
enum class EventType {
    /** Represents a note event. */
    NOTE,
    /** Represents a todo event. */
    TODO,
    /** Represents a scheduled event. */
    SCHEDULE,
    /** Represents a memo event. */
    MEMO
}

/**
 * Data class representing an event in the timeline.
 *
 * @property id The unique identifier of the event.
 * @property timestamp The date and time of the event.
 * @property eventType The type of the event.
 * @property title The title of the event.
 * @property description The description of the event.
 * @property imageUrl An optional URL for an image associated with the event.
 * @property attachment An optional attachment associated with the event.
 */
data class TimelineEvent(
    /** The unique identifier of the event. */
    val id: String = UUID.randomUUID().toString(),
    /** The date and time of the event. */
    val timestamp: Date = Date(),
    /** The type of the event. */
    val eventType: EventType,
    /** The title of the event. */
    val title: String,
    /** The description of the event. */
    val description: String,
    /** An optional URL for an image associated with the event. */
    val imageUrl: String? = null,
    /** An optional attachment associated with the event. */
    val attachment: Attachment? = null
) {
    /** Data class representing an attachment associated with an event. */
    data class Attachment(
        /** The name of the attachment. */
        val name: String,
        /** The optional file path of the attachment. */
        val path: String? = null
    )

    companion object {
        /**
         * Extracts a title from a description string.
         * @param description The description from which to extract a title.
         * @return The extracted title.
         */
        fun deriveTitle(description: String?): String {
            if (description.isNullOrEmpty()) return "新事件"

            val lines = description.split("\n")
            val firstLine = lines[0].trim()

            return if (firstLine.isNotEmpty()) {
                if (firstLine.length > 50) firstLine.substring(0, 47) + "..." else firstLine
            } else {
                val snippet = description.trim().take(50)
                if (snippet.length == 50) snippet + "..." else snippet.ifEmpty { "新事件" }
            }
        }
    }
}