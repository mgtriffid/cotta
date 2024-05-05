package com.mgtriffid.games.cotta.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RingBufferTest {
    private lateinit var buffer: RingBuffer<String>

    @BeforeEach
    fun setUp() {
        buffer = RingBuffer(8)
    }
    @Test
    fun `basic set and get`() {
        buffer[0] = "zero"
        assertEquals("zero", buffer[0])
    }

    @Test
    fun `should return null when nothing ever put`() {
        assertNull(buffer[0])
    }

    @Test
    fun `should return null when key is ahead of the lastSet`() {
        buffer[0] = "zero"
        assertNull(buffer[1])
    }

    @Test
    fun `should return null when key is too far behind`() {
        buffer[8] = "zero"
        assertNull(buffer[0])
    }

    @Test
    fun `should return value if key is within capacity`() {
        buffer[1] = "one"
        buffer[5] = "five"
        assertEquals("one", buffer[1])
    }

    @Test
    fun `should return null if value is there but key is wrong`() {
        buffer[5] = "five"
        buffer[14] = "fourteen"
        assertNull(buffer[13])
    }
}
