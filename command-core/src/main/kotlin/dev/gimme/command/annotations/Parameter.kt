package dev.gimme.command.annotations

import dev.gimme.command.parameter.ParameterTypes
import kotlin.reflect.KClass

/**
 * Marks a command parameter.
 *
 * @property default               the default value (in string form) used if this parameter is optional
 * @property defaultRepresentation the representation of the default value to be displayed, or null if nothing to be
 * displayed.
 * @property description           the description of this parameter
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Parameter(
    @get:JvmName("defaultValue")
    val default: String = "",
    val defaultRepresentation: String = "",
    val description: String = "",
)

fun Parameter.getDefaultValue(klass: KClass<*>): Any? = default.ifEmpty { null }?.let { ParameterTypes.get(klass).parse(it) }
fun Parameter.getDefaultValueString(): String? = defaultRepresentation.ifEmpty { default.ifEmpty { null } }
