package dev.gimme.gimmeapi.core.command.exception

import dev.gimme.gimmeapi.core.command.ParameterTypes
import dev.gimme.gimmeapi.core.command.parameter.ParameterType
import kotlin.reflect.KType

/**
 * Thrown when attempting to use a type that is not registered as a valid [ParameterType].
 *
 * @see ParameterTypes.register
 */
class UnsupportedParameterTypeException(type: KType) : RuntimeException(
    "Unsupported parameter type: \"${type}\"." +
            " Custom types can be registered manually with the help of ${ParameterTypes::class.qualifiedName}."
)
