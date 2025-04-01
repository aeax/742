package org.darkan.core.worldlist

import kotlinx.serialization.Serializable
import java.lang.System.currentTimeMillis

@Serializable
class World(val metadata: WorldMetadata) {
    var index = 0
    var offline = false
    var offlineSince = -1L

    var playersOnline: Int = 0
        set(value) {
            if (offlineSince >= 0) {
                if (playersOnline >= 0)
                    offlineSince = -1
            } else {
                if (playersOnline < 0)
                    this.offlineSince = currentTimeMillis()
            }
            playersOnline = value
        }

    val timeOffline get() = if (offlineSince == -1L) 0 else currentTimeMillis() - offlineSince
}