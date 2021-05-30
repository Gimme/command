package com.github.gimme.gimmebot.boot.command.executor.parameter

import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

/**
 * Thrown when a function parameter is not supported to be converted to a
 * [com.github.gimme.gimmebot.core.command.CommandParameter].
 */
class UnsupportedParameterException(kParameter: KParameter) :
    RuntimeException("Unsupported parameter \"${kParameter.name}\" with type: ${kParameter.type.javaType.typeName}")
