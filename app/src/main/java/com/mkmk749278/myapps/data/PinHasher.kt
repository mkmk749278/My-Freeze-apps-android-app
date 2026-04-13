package com.mkmk749278.myapps.data

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256

    fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hashPin(pin: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val spec = PBEKeySpec(pin.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return Base64.encodeToString(factory.generateSecret(spec).encoded, Base64.NO_WRAP)
    }

    fun verify(pin: String, salt: String, expectedHash: String): Boolean {
        val actualHash = Base64.decode(hashPin(pin, salt), Base64.NO_WRAP)
        val expectedBytes = Base64.decode(expectedHash, Base64.NO_WRAP)
        return MessageDigest.isEqual(actualHash, expectedBytes)
    }
}
