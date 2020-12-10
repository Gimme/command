package com.github.gimme.gimmebot.core.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommandResponseTest {
    @Test
    fun `should send response message`() {
        val response = CommandResponse("message")

        var actual: String? = null
        response.sendTo { message -> actual = message }

        assertEquals("message", actual)
    }

    @Test
    fun `response should accept body`() {
        val response = CommandResponse(ResponseBody("abc", 3))

        assertEquals(ResponseBody("abc", 3), response.body)
    }

    private data class ResponseBody(
        val a: String,
        val b: Int,
    )
}
