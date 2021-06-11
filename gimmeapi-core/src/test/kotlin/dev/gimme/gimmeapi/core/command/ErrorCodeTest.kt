package dev.gimme.gimmeapi.core.command

import dev.gimme.gimmeapi.core.command.exception.ErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ErrorCodeTest {

    private val errorCode = ErrorCode.INVALID_ARGUMENT

    @Test
    fun `command exception should include message`() {
        assertEquals(errorCode.message, errorCode.createException().message)
    }

    @Test
    fun `command exception with context should show context after colon`() {
        val context = "context"
        assertEquals(errorCode.message + ": $context", errorCode.createException(context).message)
    }
}
