package com.mkmk749278.myapps.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256

    fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPin(pin: String, salt: String): String {
        val saltBytes = Base64.getDecoder().decode(salt)
        val spec = PBEKeySpec(pin.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return Base64.getEncoder().encodeToString(factory.generateSecret(spec).encoded)
    }

    fun verify(pin: String, salt: String, expectedHash: String): Boolean {
        val actualHash = Base64.getDecoder().decode(hashPin(pin, salt))
        val expectedBytes = Base64.getDecoder().decode(expectedHash)
        return MessageDigest.isEqual(actualHash, expectedBytes)
    }
}
