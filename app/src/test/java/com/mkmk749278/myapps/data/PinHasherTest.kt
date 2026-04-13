package com.mkmk749278.myapps.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {
    @Test
    fun `hash verification succeeds for the original pin`() {
        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hashPin("1234", salt)
        assertTrue(PinHasher.verify("1234", salt, hash))
    }

    @Test
    fun `hash verification fails for a different pin`() {
        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hashPin("1234", salt)
        assertFalse(PinHasher.verify("9999", salt, hash))
    }
}
