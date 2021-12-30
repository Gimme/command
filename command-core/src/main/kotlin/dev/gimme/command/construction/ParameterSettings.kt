package dev.gimme.command.construction

import dev.gimme.command.BaseCommand
import dev.gimme.command.annotations.Default
import dev.gimme.command.annotations.Description
import dev.gimme.command.annotations.Name
import dev.gimme.command.annotations.Parameter
import dev.gimme.command.annotations.getDefaultValue
import dev.gimme.command.annotations.getDefaultValueString
import dev.gimme.command.common.splitCamelCase
import dev.gimme.command.exception.UnsupportedParameterException
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.parameter.ParameterTypes
import java.lang.reflect.Field
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
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
            val property = field.kotlinProperty!!
            val type: KType = property.returnType.let {
                if (it.jvmErasure.isSubclassOf(BaseCommand.Param::class)) {
                    it.arguments.first().type!!
                } else {
                    it
                }
            }

            return fromType(
                name = param.name ?: field.name.removeSuffix("\$delegate"),
                element = property,
                type = type,
                suggestions = param.suggestions,
                defaultValue = param.defaultValue,
                defaultValueString = param.defaultValueString,
                optional = param.optional,
                description = param.description,
                flags = param.flags,
            )
        }

        fun fromField(field: Field, element: KAnnotatedElement): CommandParameter = fromType(
            name = field.name,
            element = element,
            type = field.kotlinProperty!!.returnType,
        )

        fun fromFunctionParameter(functionParameter: KParameter): CommandParameter = fromType(
            name = functionParameter.name ?: throw UnsupportedParameterException(functionParameter),
            element = functionParameter,
            type = functionParameter.type,
            optional = if (functionParameter.isOptional) true else null
        )

        private fun fromType(
            name: String,
            element: KAnnotatedElement,
            type: KType,
            suggestions: (() -> Set<String>)? = null,
            defaultValue: (() -> Any?)? = null,
            defaultValueString: String? = null,
            optional: Boolean? = null,
            description: String? = null,
            flags: Set<Char> = setOf(),
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

            val values: ParameterSettingsValues = ParameterSettingsValues(
                name = null,
                description = description,
                defaultValue = defaultValue,
                defaultValueString = defaultValueString,
            ).populateFromAnnotations(element, klass)

            val _optional = optional ?: (values.defaultValue != null || type.isMarkedNullable)
            val _name = values.name ?: name
            val id = _name.splitCamelCase("-")
            val parameterType = ParameterTypes.get(klass)

            return CommandParameter(
                id = id,
                name = _name,
                type = parameterType,
                suggestions = suggestions ?: parameterType.values ?: { setOf() },
                description = values.description,
                form = form,
                optional = _optional,
                defaultValue = values.defaultValue,
                defaultValueString = values.defaultValueString,
                flags = flags.toMutableSet(),
            )
        }

        private fun ParameterSettingsValues.populateFromAnnotations(element: KAnnotatedElement, klass: KClass<out Any>): ParameterSettingsValues {
            val parameterA = element.findAnnotation<Parameter>()
            val nameA = element.findAnnotation<Name>()
            val descriptionA = element.findAnnotation<Description>()
            val defaultA = element.findAnnotation<Default>()

            name = name ?: nameA?.value ?: parameterA?.value?.ifEmpty { null }
            description = description ?: descriptionA?.value ?: parameterA?.description?.ifEmpty { null }
            defaultValue = defaultValue ?: (defaultA ?: parameterA?.def)?.getDefaultValue(klass)
            defaultValueString = defaultValueString ?: (defaultA ?: parameterA?.def)?.getDefaultValueString()

            return this
        }

        private data class ParameterSettingsValues(
            var name: String?,
            var description: String?,
            var defaultValue: (() -> Any?)?,
            var defaultValueString: String?,
        )
    }
}
