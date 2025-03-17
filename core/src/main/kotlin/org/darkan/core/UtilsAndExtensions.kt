package org.darkan.core

import java.util.Locale

object UtilsAndExtensions {

}

private const val FNV1aPrime = 16777619u
fun String.hashToShort(): Short {
    var hash = 0u
    for (char in this) {
        hash = hash xor char.code.toUInt()
        hash = (hash * FNV1aPrime) % 65536u
    }
    return hash.toShort()
}

val currentTimeTicks get() = System.currentTimeMillis() / 600L

fun String.formatPlayerNameForProtocol(): String {
    return this.lowercase().replace(" ", "_") ?: ""
}

fun String.formatPlayerNameForDisplay(): String {
    return this.replace("_", " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}