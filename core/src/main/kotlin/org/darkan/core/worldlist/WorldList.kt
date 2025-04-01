package org.darkan.core.worldlist

import org.darkan.core.EnvVars

class WorldList(val maxWorlds: Int) {
    private val worlds = sortedMapOf<Int, World>()
    private var _revision = 10

    init {
        if (EnvVars.debug)
            put(World(WorldMetadata(300, "localhost", 43595, "localhost:43595", Country.USA, false, true, true, false, false)))
    }

    val revision
        get() = _revision

    fun get(number: Int): World? = synchronized(worlds) {
        worlds[number] ?: worlds[number]
    }

    fun remove(number: Int): World? = synchronized(worlds) {
        var orig = worlds.remove(number)
        if (orig != null) {
            updateIndices()
            _revision++
        }
        return orig
    }

    fun put(world: World) = synchronized(worlds) {
        worlds[world.metadata.number] = world
        updateIndices()
        _revision++
    }

    fun getWorldArray(): Array<World> = synchronized(worlds) {
        val arr = arrayOfNulls<World>(worlds.size)
        var i = 0
        for (key in worlds.keys) {
            arr[i++] = worlds[key]
        }
        return arr.filterNotNull().toTypedArray()
    }

    fun getDefault(): World? = synchronized(worlds) {
        worlds[worlds.firstKey()]
    }

    private fun updateIndices() {
        var index = 0
        for (key in worlds.keys)
            worlds[key]?.index = index++
    }
}
