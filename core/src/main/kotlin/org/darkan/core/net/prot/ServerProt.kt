package org.darkan.core.net.prot

import org.darkan.core.clientwatch.ReflectionCheckType
import org.darkan.core.type.RegionSize

interface ServerProt

class Pong : ServerProt

data class MapRegion(
    val regionIds: IntArray,
    val mapSize: RegionSize = RegionSize.SIZE_104,
    val xteas: Array<IntArray>,
    val chunkX: Int = 0,
    val chunkY: Int = 0,
    val forceMapRefresh: Boolean = false,
    val localPlayerPid: Int? = null,
    val localPlayerBaseTileHash: Int? = null,
    val otherPlayerRegionIds: IntArray? = null
) : ServerProt {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapRegion

        if (chunkX != other.chunkX) return false
        if (chunkY != other.chunkY) return false
        if (forceMapRefresh != other.forceMapRefresh) return false
        if (localPlayerPid != other.localPlayerPid) return false
        if (localPlayerBaseTileHash != other.localPlayerBaseTileHash) return false
        if (!regionIds.contentEquals(other.regionIds)) return false
        if (mapSize != other.mapSize) return false
        if (!xteas.contentDeepEquals(other.xteas)) return false
        if (!otherPlayerRegionIds.contentEquals(other.otherPlayerRegionIds)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chunkX
        result = 31 * result + chunkY
        result = 31 * result + forceMapRefresh.hashCode()
        result = 31 * result + (localPlayerPid ?: 0)
        result = 31 * result + (localPlayerBaseTileHash ?: 0)
        result = 31 * result + regionIds.contentHashCode()
        result = 31 * result + mapSize.hashCode()
        result = 31 * result + xteas.contentDeepHashCode()
        result = 31 * result + (otherPlayerRegionIds?.contentHashCode() ?: 0)
        return result
    }
}

data class ReflectionRequest(
    val type: ReflectionCheckType,
    val className: String,
    val methodName: String,
    val returnType: String? = null,
    val paramTypes: Array<String> = emptyArray(),
    val paramValues: Array<Any> = emptyArray(),
    val fieldValue: Int? = null
) : ServerProt {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReflectionRequest

        if (fieldValue != other.fieldValue) return false
        if (type != other.type) return false
        if (className != other.className) return false
        if (methodName != other.methodName) return false
        if (returnType != other.returnType) return false
        if (!paramTypes.contentEquals(other.paramTypes)) return false
        if (!paramValues.contentEquals(other.paramValues)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fieldValue ?: 0
        result = 31 * result + type.hashCode()
        result = 31 * result + className.hashCode()
        result = 31 * result + methodName.hashCode()
        result = 31 * result + (returnType?.hashCode() ?: 0)
        result = 31 * result + paramTypes.contentHashCode()
        result = 31 * result + paramValues.contentHashCode()
        return result
    }
}