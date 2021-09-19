package dev.gimme.command.construction

import dev.gimme.command.BaseCommand
import dev.gimme.command.annotations.Parameter
import dev.gimme.command.annotations.getDefaultValue
import dev.gimme.command.annotations.getDefaultValueString
import dev.gimme.command.common.splitCamelCase
import dev.gimme.command.function.UnsupportedParameterException
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.parameter.ParameterTypes
import java.lang.reflect.Field
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinProperty

internal class ParameterSettings {
    companion object {
        fun fromParamField(field: Field, param: BaseCommand.Param<*>): CommandParameter {
            val type: KType = field.kotlinProperty!!.returnType.let {
                if (it.jvmErasure.isSubclassOf(BaseCommand.Param::class)) {
                    it.arguments.first().type!!
                } else {
                    it
                }
            }

            return fromType(
                name = field.name.removeSuffix("\$delegate"),
                annotation = null,
                type = type,
                suggestions = param.suggestions,
                defaultValue = param.defaultValue,
                defaultValueString = param.defaultValueString,
                optional = param.optional,
            )
        }

        fun fromField(field: Field, annotation: Parameter): CommandParameter = fromType(
            name = field.name,
            annotation = annotation,
            type = field.kotlinProperty!!.returnType,
        )

        fun fromFunctionParameter(functionParameter: KParameter): CommandParameter = fromType(
            name = functionParameter.name ?: throw UnsupportedParameterException(functionParameter),
            annotation = functionParameter.findAnnotation(),
            type = functionParameter.type,
        )

        private fun fromType(
            name: String,
            annotation: Parameter?,
            type: KType,
            suggestions: (() -> Set<String>)? = null,
            defaultValue: Any? = null,
            defaultValueString: String? = null,
            optional: Boolean? = null,
        ): CommandParameter {
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

            var _defaultValue = defaultValue ?: annotation?.getDefaultValue(klass)
            val _defaultValueString = defaultValueString ?: annotation?.getDefaultValueString()
            var _optional = optional ?: (_defaultValue != null)

            if (_defaultValue == null && type.isMarkedNullable) {
                _optional = true
                _defaultValue = null
            }

            if (!type.isMarkedNullable && _optional && _defaultValue == null) {
                throw IllegalStateException("Parameter \"${name}\" has to be marked nullable when having a null default value") // TODO: exception type
            }

            val description = annotation?.description?.ifEmpty { null }

            val id = name.splitCamelCase("-")
            val parameterType = ParameterTypes.get(klass)

            return CommandParameter(
                id = id,
                name = name,
                type = parameterType,
                suggestions = suggestions ?: parameterType.values ?: { setOf() },
                description = description,
                form = form,
                optional = _optional,
                defaultValue = _defaultValue,
                defaultValueString = _defaultValueString,
            )
        }
    }
}
