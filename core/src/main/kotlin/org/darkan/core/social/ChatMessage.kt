package org.darkan.core.social

interface ChatMessage {
    val effects: Int
    val message: String
}

data class PublicChatMessage(
    override val message: String,
    override val effects: Int = 0
) : ChatMessage

data class QuickChatMessage(
    override val message: String,
    val fileId: Int,
    val data: ByteArray? = null,
    override val effects: Int = 0
) : ChatMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as QuickChatMessage
        if (fileId != other.fileId) return false
        if (effects != other.effects) return false
        if (message != other.message) return false
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = fileId
        result = 31 * result + effects
        result = 31 * result + message.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}