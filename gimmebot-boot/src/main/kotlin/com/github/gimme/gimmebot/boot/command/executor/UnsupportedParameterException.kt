package com.github.gimme.gimmebot.boot.command.executor

import kotlin.reflect.KParameter

/**
 * Thrown when a function parameter is not supported to be converted to a
 * [com.github.gimme.gimmebot.core.command.parameter.CommandParameter].
 */
class UnsupportedParameterException(kParameter: KParameter) :
    RuntimeException("Unsupported parameter \"${kParameter.name}\" of type: ${kParameter.type}")
