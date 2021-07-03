package dev.gimme.command.construction

import dev.gimme.command.BaseCommand
import dev.gimme.command.Command
import dev.gimme.command.parameter.ParameterTypes
import dev.gimme.command.annotations.Sender
import dev.gimme.command.exception.CommandException
import dev.gimme.command.function.CommandFunction
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.parameter.CommandParameterSet
import dev.gimme.command.parameter.DefaultValue
import dev.gimme.command.sender.CommandSender
import dev.gimme.command.common.splitCamelCase
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinProperty

internal fun BaseCommand<*>.generateParameters(): CommandParameterSet {
    val paramSettings = mutableListOf<ParameterSettings>()

    this.javaClass.declaredFields.forEach { field ->
        ParameterSettings.fromField(field, this)?.let {
            paramSettings.add(it)
        }
    }

    this.getFirstCommandFunction()?.also { commandFunction ->
        paramSettings.addAll(
            commandFunction.parameters
                .filter { it.isCommandParameter() }
                .map { param ->
                    ParameterSettings.fromFunctionParameter(param)
                }
        )
    }

    return CommandParameterSet(
        generateParameters(paramSettings).onEach { (settings, parameter) ->
            settings.setValue?.let { this.argumentPropertySetters[parameter] = it }
        }.values
    )
}

internal fun BaseCommand<*>.generateSenders(): Set<KClass<*>>? {
    val senderSettings = mutableListOf<SenderSettings>()

    this.javaClass.declaredFields.forEach { field ->
        if (field.kotlinProperty?.hasAnnotation<Sender>() == true) {
            senderFields.add(field)

            senderSettings.add(SenderSettings.fromType(type = field.kotlinProperty!!.returnType))
        }
    }

    getFirstCommandFunction()?.also { commandFunction ->
        senderSettings.addAll(
            commandFunction.parameters
                .filter { it.isSenderParameter() }
                .map { SenderSettings.fromType(type = it.type) }
        )
    }

    return generateSenders(senderSettings)
}

/**
 * Generates a set of [CommandParameter]s based on the supplied [parameterSettings].
 */
private fun generateParameters(parameterSettings: List<ParameterSettings>): Map<ParameterSettings, CommandParameter> {
    val usedFlags = mutableSetOf<Char>()

    return parameterSettings.associate { settings ->
        val name = settings.name

        val id = name.splitCamelCase("-")
        val flags = generateFlags(id, usedFlags)
        val defaultValue: DefaultValue? = settings.defaultValue
        usedFlags.addAll(flags)

        val form = settings.form
        val klass: KClass<*> = settings.klass

        val type = ParameterTypes.get(klass)
        val suggestions: () -> Set<String> = settings.suggestions ?: type.values ?: { setOf() }

        val description = settings.description

        settings to CommandParameter(
            id = id,
            name = name,
            type = type,
            form = form,
            suggestions = suggestions,
            flags = flags,
            defaultValue = defaultValue,
            description = description
        )
    }.also { parameterMap ->
        val groupedParameters: Map<String, Int> =
            parameterMap.values.groupingBy { it.id }.eachCount().filter { it.value > 1 }

        groupedParameters.keys.firstOrNull()?.let {
            throw RuntimeException("A parameter with the name \"${it}\" already exists") // TODO: exception type
        }
    }
}

/**
 * Generates a set of allowed sender types based on the supplied [senderSettings].
 */
private fun generateSenders(senderSettings: List<SenderSettings>): Set<KClass<*>>? {
    var requiredSender: KClass<*>? = null
    var optionalSenders: MutableSet<KClass<*>>? = null

    senderSettings.forEach {
        if (!it.optional) {
            if (requiredSender != null) throw IllegalStateException("Only one sender type can be required (i.e., non-null)") // TODO: exception type
            requiredSender = it.klass
        } else {
            optionalSenders = (optionalSenders ?: mutableSetOf()).apply { add(it.klass) }
        }
    }

    return requiredSender?.let { setOf(it) } ?: optionalSenders
}

internal fun KParameter.isSenderParameter() = this.kind == KParameter.Kind.VALUE &&
        (this.type.isSubtypeOf(CommandSender::class.createType(nullable = true)) || this.hasAnnotation<Sender>())

internal fun KParameter.isCommandParameter() = !this.isSenderParameter() && this.kind == KParameter.Kind.VALUE

/**
 * Generates a set of flags from the [string] without clashing with any of the [unavailableFlags].
 */
internal fun generateFlags(string: String, unavailableFlags: Set<Char> = setOf()): Set<Char> {
    require(string.isNotEmpty())

    val flags = mutableSetOf<Char>()

    val firstLetterLower = string.first().let { if (it.isUpperCase()) it.lowercaseChar() else it }
    val firstLetterUpper = firstLetterLower.uppercaseChar()

    if (!unavailableFlags.contains(firstLetterLower)) flags.add(firstLetterLower)
    else if (!unavailableFlags.contains(firstLetterUpper)) flags.add(firstLetterUpper)

    return flags
}

/**
 * Returns the first found method that is annotated with @[CommandFunction].
 *
 * @throws IllegalStateException if there is no method annotated with @[CommandFunction] or if it has the wrong
 * return type
 */
@Throws(CommandException::class)
internal fun <R> Command<R>.getFirstCommandFunction(): KFunction<R>? {
    // Look through the public methods in the command class
    for (function in this::class.declaredMemberFunctions) {
        // Make sure it has the right annotation
        if (!function.hasAnnotation<CommandFunction>()) continue

        return try {
            @Suppress("UNCHECKED_CAST")
            function as KFunction<R>
        } catch (e: ClassCastException) {
            throw ClassCastException(
                "The return type: \"${function.returnType.jvmErasure.qualifiedName}\"" +
                        " of the command function: \"${function.name}\"" +
                        " in ${this::class.qualifiedName}" +
                        " does not match the command's return type."
            )
        }
    }

    return null
}

/**
 * Generates a "usage string" that matches the [Command.parameters].
 */
internal fun Command<*>.generateUsage(): String {
    val sb = StringBuilder(name)

    parameters.forEach { parameter ->
        val defaultValueRepresentation = parameter.defaultValue?.representation
        val wrap = if (parameter.optional) Pair("[", "]") else Pair("<", ">")

        sb.append(" ${wrap.first}${parameter.id}${defaultValueRepresentation?.let { "=$defaultValueRepresentation" } ?: ""}${wrap.second}")
    }

    return sb.toString()
}
