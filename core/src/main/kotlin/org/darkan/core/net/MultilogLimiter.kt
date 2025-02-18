package org.darkan.core.net

import org.darkan.core.EnvVars
import java.util.concurrent.ConcurrentHashMap

class MultilogLimiter() {
    private val connected = ConcurrentHashMap<String, Int>()
    private val whitelistedIps: Set<String> = parseWhitelist(EnvVars.multiLogWhitelist)

    private fun parseWhitelist(whitelistStr: String?): Set<String> =
        whitelistStr?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

    fun add(ip: String): Boolean {
        if (ip in whitelistedIps) return true
        var success = true
        connected.compute(ip) { _, count ->
            when (count) {
                null -> 1
                in 0 until EnvVars.multiLogLimit -> count + 1
                else -> {
                    success = false
                    count
                }
            }
        }
        return success
    }

    fun remove(ip: String) {
        if (ip in whitelistedIps) return
        connected.computeIfPresent(ip) { _, count ->
            when {
                count <= 1 -> null
                else -> count - 1
            }
        }
    }

    fun clear() = connected.clear()

    fun getCount(ip: String) =
        if (ip in whitelistedIps) 0 else connected[ip] ?: 0
}