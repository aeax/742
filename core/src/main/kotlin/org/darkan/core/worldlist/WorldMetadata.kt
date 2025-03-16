package org.darkan.core.worldlist

data class WorldMetadata(
    val number: Int,
    val ipAddress: String,
    val port: Int,
    val activity: String,
    val country: Country,
    val quickchat: Boolean,
    val lootShare: Boolean,
    val members: Boolean,
    val pvp: Boolean,
    val highlighted: Boolean)
{
    val isLobby get() = number >= 1000
}