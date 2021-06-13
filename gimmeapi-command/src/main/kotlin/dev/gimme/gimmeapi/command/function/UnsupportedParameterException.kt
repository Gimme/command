package dev.gimme.gimmeapi.command.function

import dev.gimme.gimmeapi.command.parameter.CommandParameter
import kotlin.reflect.KParameter

/**
 * Thrown when a function parameter is not supported to be converted to a [CommandParameter].
 */
class UnsupportedParameterException(kParameter: KParameter) :
    RuntimeException("Unsupported parameter \"${kParameter.name}\" of type: ${kParameter.type}")
