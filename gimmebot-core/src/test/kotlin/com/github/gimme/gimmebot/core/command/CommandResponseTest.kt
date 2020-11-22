package com.github.gimme.gimmebot.core.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommandResponseTest {
    @Test
    fun `should send response message`() {
        val response = CommandResponse("message")

        var actual: String? = null
        response.send { message -> actual = message }

        assertEquals("message", actual)
    }
}
