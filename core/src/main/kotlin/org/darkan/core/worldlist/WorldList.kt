package org.darkan.core.worldlist

class WorldList(val maxWorlds: Int) {
    private val worlds = sortedMapOf<Int, World>()
    internal var revision = 0

    fun get(number: Int): World? = synchronized(worlds) {
        worlds[number] ?: worlds[number]
    }

    fun remove(number: Int): World? = synchronized(worlds) {
        var orig = worlds.remove(number)
        if (orig != null) {
            updateIndices()
            revision++
        }
        return orig
    }

    fun put(number: Int, world: World) = synchronized(worlds) {
        worlds[number] = world
        updateIndices()
        revision++
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
