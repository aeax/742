package org.darkan.core

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv

object EnvVars {
    private val dotenv: Dotenv = dotenv {
        ignoreIfMissing = true
    }

    //server settings
    val debug: Boolean = dotenv.get("DEBUG", "false").toBooleanStrict()
    val serverName: String = dotenv.get("SERVER_NAME", "Darkan")
    val multiLogWhitelist: String = dotenv.get("MULTILOG_WHITELIST", "localhost,127.0.0.1")
    val multiLogLimit: Int = dotenv.get("MULTILOG_LIMIT", "3").toInt()
    val js5ServerToken: String = dotenv.get("JS5_SERVER_TOKEN", "ev9+VAp5/tMKeNR/7MOuH6lKWS+rGkHK")
    val loginServerToken: String = dotenv.get("LOGIN_SERVER_TOKEN", "wwGlrZHF5gKN6D3mDdihco3oPeYN2KFybL9hUUFqOvk")
    val clientKey: String = dotenv.get("CLIENT_KEY", "29EDD9FDC775629058FBBF106C5B0E0A3A8028FE0037D1737B8EC3EA2F4E8B8FD6F54EF2F4E65862")
    val js5RsaModulus: String = dotenv.get("RSA_JS5_MODULUS", "117525752735533423040644219776209926525585489242340044375332234679786347045466594509203355398209678968096551043842518449703703964361320462967286756268851663407950384008240524570966471744081769815157355561961607944067477858512067883877129283799853947605780903005188603658779539811385137666347647991072028080201")
    val js5RsaExponent: String = dotenv.get("RSA_JS5_EXPONENT", "45769714620275867926001532284788836149236590657678028481492967724067121406860916606777808563536714166085238449913676219414798301454048585933351540049893959827785868628572203706265915752274580525376826724019249600701154664022299724373133271944352291456503171589594996734220177420375212353960806722706846977073")
    //val js5PrefetchKeys: String = dotenv.get("JS5_PREFETCH_KEYS", "1441,78700,44880,39771,363186,44375,0,16140,7316,271148,810710,216189,379672,454149,933950,21006,25367,17247,1244,1,14856,1494,119,882901,1818764,3963,3618")
    val lobbyPort: Int = dotenv.get("LOBBY_PORT", "43594").toInt()
    val lobbyApiPort: Int = dotenv.get("LOBBY_API_PORT", "4040").toInt()
    val apiKey: String = dotenv.get("API_KEY", "TEST_API_KEY")
    val cachePath: String = dotenv.get("CACHE_PATH", "../cache/")
    val memCache: Boolean = dotenv.get("MEM_CACHE", (!debug).toString()).toBooleanStrict()
    val majorVersion: Int = dotenv.get("MAJOR_VERSION", "727").toInt()
    val minorVersion: Int = dotenv.get("MINOR_VERSION", "1").toInt()
    val packetSizeLimit: Int = dotenv.get("PACKET_SIZE_LIMIT", "7500").toInt()
    val worldCycleNanos: Long = dotenv.get("WORLD_CYCLE_NANOS", "600000000").toLong()
    val worldCycleMillis: Long = worldCycleNanos / 1000000L
    val cacheThreadUsage: Double = dotenv.get("CACHE_THREAD_USAGE", "1.0").toDouble()

    val cryptoArgon2Memory: Int = dotenv.get("CRYPTO_ARGON2_MEMORY", "65536").toInt() // 64mb (1048576) for 1GB in KB
    val cryptoArgon2Iterations: Int = dotenv.get("CRYPTO_ARGON2_ITERATIONS", "5").toInt()
    val cryptoArgon2HashLength: Int = dotenv.get("CRYPTO_ARGON2_HASH_LENGTH", "32").toInt()
    val cryptoArgon2SaltLength: Int = dotenv.get("CRYPTO_ARGON2_SALT_LENGTH", "16").toInt()
    val cryptoArgon2Parallelism: Int = dotenv.get("CRYPTO_ARGON2_PARALLELISM", "4").toInt()

    val legacyCryptoSecret: String = dotenv.get("LEGACY_CRYPTO_SECRET", "secrettt")
    val legacyCryptoSalt: ByteArray = dotenv.get("LEGACY_CRYPTO_SALT", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0").split(",").map { it.toByte() }.toByteArray()

    //mongo settings
    val mongoUri: String = dotenv.get("MONGO_URI", "mongodb://localhost:27017/darkan-server?retryWrites=true&w=majority")
}