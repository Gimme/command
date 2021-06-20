package dev.gimme.gimmeapi.command.annotations

import dev.gimme.gimmeapi.command.parameter.DefaultValue

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Parameter(
    val default: String = "",
    val defaultRepresentation: String = "",
    val description: String = "",
)

fun Parameter.defaultValue(): DefaultValue? {
    return if (default.isEmpty() && defaultRepresentation.isEmpty()) {
        null
    } else {
        DefaultValue(
            default.ifEmpty { null },
            defaultRepresentation.ifEmpty { null }
        )
    }
}
