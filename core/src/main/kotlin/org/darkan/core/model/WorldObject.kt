package org.darkan.core.model

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.type.Tile

data class WorldObject(
    var id: Int,
    var type: ObjectShape,
    var rotation: Int,
    var tile: Tile
) {
    constructor(id: Int, rotation: Int, x: Int, y: Int, plane: Int) : this(
        id = id,
        type = Cache.objects[id].shapes?.get(0) ?: ObjectShape.SCENERY_INTERACT,
        rotation = rotation,
        tile = Tile(x, y, plane)
    )

    constructor(id: Int, type: ObjectShape, rotation: Int, x: Int, y: Int, plane: Int) : this(
        id = id,
        type = type,
        rotation = rotation,
        tile = Tile(x, y, plane)
    )

    constructor(other: WorldObject) : this(
        id = other.id,
        type = other.type,
        rotation = other.rotation,
        tile = other.tile
    )

    val definitions
        get() = Cache.objects[id]

    val coordFaceX: Int
        get() = tile.getCoordFaceX(definitions.sizeX, definitions.sizeY, rotation)

    val coordFaceY: Int
        get() = tile.getCoordFaceY(definitions.sizeX, definitions.sizeY, rotation)

    val coordFace: Tile
        get() = Tile(coordFaceX, coordFaceY, tile.level)

    fun getRotation(turn: Int): Int = (rotation + turn) and 0x3

    val slot: Int
        get() = type.slot

    val x: Int
        get() = tile.x

    val y: Int
        get() = tile.y

    val level: Int
        get() = tile.level
}