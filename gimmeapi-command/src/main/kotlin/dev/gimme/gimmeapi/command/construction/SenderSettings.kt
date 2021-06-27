package dev.gimme.gimmeapi.command.construction

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

internal class SenderSettings(
    val klass: KClass<*>,
    val optional: Boolean,
) {
    companion object {
        fun fromType(type: KType) = SenderSettings(
            klass = type.jvmErasure,
            optional = type.isMarkedNullable,
        )
    }
}
