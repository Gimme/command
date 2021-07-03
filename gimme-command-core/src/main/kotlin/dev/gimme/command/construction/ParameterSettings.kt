package dev.gimme.command.construction

import dev.gimme.command.BaseCommand
import dev.gimme.command.annotations.Parameter
import dev.gimme.command.annotations.getDefaultValue
import dev.gimme.command.function.UnsupportedParameterException
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.parameter.DefaultValue
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinProperty

internal class ParameterSettings(
    val name: String,
    val form: CommandParameter.Form,
    val klass: KClass<*>,
    val suggestions: (() -> Set<String>)?,
    val defaultValue: DefaultValue?,
    val description: String?,
    val setValue: ((value: Any?) -> Unit)? = null,
) {
    companion object {
        fun fromField(field: Field, obj: Any): ParameterSettings? {
            val paramAnnotation: Parameter? = field.kotlinProperty?.findAnnotation()

            return when {
                BaseCommand.Param::class.java.isAssignableFrom(field.type) -> {
                    val name = field.name.removeSuffix("\$delegate")

                    field.isAccessible = true
                    @Suppress("UNCHECKED_CAST")
                    val value = field.get(obj) as BaseCommand.Param<*>

                    val type: KType = field.kotlinProperty!!.returnType.let {
                        if (it.jvmErasure.isSubclassOf(BaseCommand.Param::class)) {
                            it.arguments.first().type!!
                        } else {
                            it
                        }
                    }

                    fromType(
                        name = name,
                        annotation = null,
                        type = type,
                        suggestions = value.suggestions,
                        defaultValue = value.defaultValue,
                        setValue = { value.set(it) },
                    )
                }
                paramAnnotation != null -> {
                    val name = field.name

                    fromType(
                        name = name,
                        annotation = paramAnnotation,
                        type = field.kotlinProperty!!.returnType,
                        setValue = {
                            field.isAccessible = true
                            field.set(obj, it)
                        },
                    )
                }
                else -> null
            }
        }

        fun fromFunctionParameter(functionParameter: KParameter): ParameterSettings {
            return fromType(
                name = functionParameter.name ?: throw UnsupportedParameterException(functionParameter),
                annotation = functionParameter.findAnnotation(),
                type = functionParameter.type,
            )
        }

        fun fromType(
            name: String,
            annotation: Parameter?,
            type: KType,
            suggestions: (() -> Set<String>)? = null,
            defaultValue: DefaultValue? = null,
            setValue: ((value: Any?) -> Unit)? = null,
        ): ParameterSettings {
            annotation?.getDefaultValue()?.also {
                if (!type.isMarkedNullable && it.value == null) {
                    throw IllegalStateException("Parameter \"$name\" has a null default value for a type marked as non-nullable") // TODO: exception type
                }
            }

            val jvmErasure = type.jvmErasure

            val form = when {
                jvmErasure.isSuperclassOf(MutableList::class) -> CommandParameter.Form.LIST
                jvmErasure.isSuperclassOf(MutableSet::class) -> CommandParameter.Form.SET
                else -> CommandParameter.Form.VALUE
            }

            val klass = if (form.isCollection) {
                type.arguments.firstOrNull()?.type?.jvmErasure
                    ?: throw RuntimeException("Unsupported parameter type: $type") // TODO: exception type
            } else {
                jvmErasure
            }

            var _defaultValue = defaultValue ?: annotation?.getDefaultValue()

            if (_defaultValue == null && type.isMarkedNullable) _defaultValue = DefaultValue(null, null)

            _defaultValue?.let {
                if (!type.isMarkedNullable && it.value == null) {
                    throw IllegalStateException("Parameter \"${name}\" has to be marked nullable when having a null default value") // TODO: exception type
                }
            }

            val description = annotation?.description?.ifEmpty { null }

            return ParameterSettings(
                name = name,
                form = form,
                klass = klass,
                suggestions = suggestions,
                defaultValue = _defaultValue,
                description = description,
                setValue = setValue,
            )
        }
    }
}
