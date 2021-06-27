package dev.gimme.gimmeapi.command.exception

import dev.gimme.gimmeapi.command.parameter.ParameterTypes
import dev.gimme.gimmeapi.command.parameter.ParameterType
import kotlin.reflect.KClass

/**
 * Thrown when attempting to use a type that is not registered as a valid [ParameterType].
 *
 * @see ParameterTypes.register
 */
class UnsupportedParameterTypeException(klass: KClass<*>) : RuntimeException(
    "Unsupported parameter type: \"${klass}\"." +
            " Custom types can be registered manually with the help of ${ParameterTypes::class.qualifiedName}."
)
