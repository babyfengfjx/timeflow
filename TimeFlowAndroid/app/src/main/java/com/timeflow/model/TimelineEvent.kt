package com.timeflow.model

import java.util.Date
import java.util.UUID

/**
 * 时间轴事件的数据模型
 */
enum class EventType {
    NOTE, TODO, SCHEDULE
}

data class TimelineEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Date = Date(),
    val eventType: EventType,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val attachment: Attachment? = null
) {
    data class Attachment(
        val name: String
    )
    
    companion object {
        /**
         * 从描述中提取标题
         * @param description 事件描述
         * @return 提取的标题
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