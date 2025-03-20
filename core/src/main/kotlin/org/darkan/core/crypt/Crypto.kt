package org.darkan.core.crypt

import de.mkammerer.argon2.Argon2Factory
import de.mkammerer.argon2.Argon2Factory.Argon2Types
import org.darkan.core.EnvVars
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

object Crypto {
    private val secureRandom = SecureRandom()
    private val argon2 = Argon2Factory.create(
        Argon2Types.ARGON2id,
        EnvVars.cryptoArgon2SaltLength,
        EnvVars.cryptoArgon2HashLength
    )

    fun hashPasswordArgon2(password: String): String {
        require(password.isNotEmpty()) { "Password cannot be empty" }
        return password.toCharArray().let { chars ->
            try {
                argon2.hash(
                    EnvVars.cryptoArgon2Iterations,
                    EnvVars.cryptoArgon2Memory,
                    EnvVars.cryptoArgon2Parallelism,
                    chars
                )
            } finally {
                chars.fill(0.toChar())
            }
        }
    }

    fun verifyPasswordArgon2(password: String, hash: String): Boolean {
        require(password.isNotEmpty()) { "Password cannot be empty" }
        require(hash.isNotEmpty()) { "Hash cannot be null or empty" }

        return password.toCharArray().let { chars ->
            try {
                argon2.verify(hash, chars)
            } finally {
                chars.fill(0.toChar())
            }
        }
    }

    fun generateSecureToken(byteLength: Int): String {
        val randomBytes = ByteArray(byteLength).apply { secureRandom.nextBytes(this) }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }

    /**
     * Legacy encryption function using AES/CBC/PKCS5Padding.
     * Retained for backward compatibility.
     *
     * @param strToEncrypt The string to encrypt
     * @return The encrypted bytes or null if encryption fails
     */
    @Deprecated("Legacy method, use modern crypto approaches instead")
    fun legacyEncrypt(strToEncrypt: String): ByteArray? {
        try {
            val ivspec = IvParameterSpec(ByteArray(16))
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(
                EnvVars.legacyCryptoSecret.toCharArray(),
                EnvVars.legacyCryptoSalt,
                65536,
                256
            )
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec)
            return cipher.doFinal(strToEncrypt.toByteArray(StandardCharsets.UTF_8))
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Legacy function to compare encrypted data with unencrypted string.
     * Retained for backward compatibility.
     *
     * @param encrypted The encrypted bytes
     * @param unencrypted The unencrypted string to compare against
     * @return True if they match, false otherwise
     */
    @Deprecated("Legacy method, use modern crypto approaches instead")
    fun legacyCompare(unencrypted: String, encrypted: ByteArray) = encrypted.contentEquals(legacyEncrypt(unencrypted))

    @Deprecated("Giga legacy method, use modern crypto approaches instead")
    fun gigaLegacyCompare(unencrypted: String, encrypted: String) = encrypted == gigaLegacyEncrypt(unencrypted)

    @Deprecated("Giga legacy method, use modern crypto approaches instead")
    fun gigaLegacyEncrypt(input: String): String {
        return try {
            MessageDigest.getInstance("SHA-1")
                .digest(input.toByteArray())
                .joinToString("") { "%02x".format(it and 0xff.toByte()) }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}