package dev.gimme.command.annotations

import dev.gimme.command.parameter.ParameterTypes
import kotlin.reflect.KClass

/**
 * Defines the default value used if the annotated parameter is optional.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Default(
    /**
     * The default value (in string form) used if this parameter is optional.
     */
    val value: String,

    /**
     * The representation of the default value to be displayed, or null if nothing to be displayed.
     */
    val valueRepresentation: String = "",
)

fun Default.getDefaultValue(klass: KClass<*>): (() -> Any?)? =
    value.ifEmpty { null }?.let { value -> { ParameterTypes.get(klass).parse(value) } }

fun Default.getDefaultValueString(): String? = valueRepresentation.ifEmpty { value.ifEmpty { null } }
