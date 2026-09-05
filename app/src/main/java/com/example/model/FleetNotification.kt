package com.example.model

enum class NotificationStatusDot {
    GREEN,
    YELLOW,
    ORANGE,
    RED
}

enum class NotificationCardType {
    NONE,
    FILE_APPROVAL,
    MENTION_QUOTE,
    FILE_ATTACHMENT
}

data class FleetNotification(
    val id: String,
    val senderName: String,
    val senderRole: String,
    val senderAvatarIndex: Int,
    val statusDot: NotificationStatusDot = NotificationStatusDot.GREEN,
    val headlinePrefix: String, // e.g. "Hailey Garza"
    val actionText: String, // e.g. "added new tags to"
    val targetSubject: String, // e.g. "Ease Design System"
    val timeAgo: String, // e.g. "1 mins. ago"
    val projectName: String, // e.g. "Easy 2023 Project"
    val pillTags: List<String> = emptyList(), // e.g. ["UI Design", "Dashboard", "Design system"]
    val cardType: NotificationCardType = NotificationCardType.NONE,
    val fileName: String? = null,
    val fileEditedAgo: String? = null,
    val quoteText: String? = null,
    val isUnread: Boolean = true,
    val approvalState: Boolean? = null, // null = pending, true = accepted, false = declined
    val reactions: List<String> = emptyList(),
    val tabCategory: String = "Inbox" // "Inbox" or "Team"
)
