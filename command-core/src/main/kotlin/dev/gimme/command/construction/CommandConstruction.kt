package dev.gimme.command.construction

import dev.gimme.command.BaseCommand
import dev.gimme.command.Command
import dev.gimme.command.annotations.Parameter
import dev.gimme.command.annotations.Sender
import dev.gimme.command.annotations.CommandFunction
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.parameter.CommandParameterSet
import dev.gimme.command.sender.CommandSender
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinProperty

internal fun BaseCommand<*>.generateParameters(): CommandParameterSet {
    val parameters = mutableListOf<CommandParameter>()

    this.commandFunction?.also { commandFunction ->
        commandFunction.parameters
            .filter { it.isCommandParameter() }
            .onEach { param ->
                parameters.add(ParameterSettings.fromFunctionParameter(param))
            }
    }

    this.javaClass.declaredFields.forEach { field ->
        if (BaseCommand.Param::class.java.isAssignableFrom(field.type)) {
            field.isAccessible = true
            val param = field.get(this) as BaseCommand.Param<*>

            parameters.add(this.registerParameter(ParameterSettings.fromParamField(field, param)) { param.set(it) })
        } else {
            field.kotlinProperty?.let {
                if (it.findAnnotation<Parameter>() == null) return@let

                parameters.add(this.registerParameter(ParameterSettings.fromField(field, it)) { arg ->
                    field.isAccessible = true
                    field.set(this, arg)
                })
            }
        }
    }

    val usedFlags = mutableSetOf<Char>()

    parameters
        .groupingBy { it.id }
        .eachCount()
        .filter { it.value > 1 }
        .keys.firstOrNull()?.let {
            throw RuntimeException("A parameter with the name \"${it}\" already exists") // TODO: exception type
        }

    parameters.forEach {
        val duplicateFlags = it.flags.filter { flag -> usedFlags.contains(flag) }.toSet()
        it.flags.removeAll(duplicateFlags)
        usedFlags.addAll(it.flags)
    }

    parameters.forEach {
        if (it.flags.isNotEmpty()) return@forEach
        it.flags.addAll(it.generateFlags(usedFlags))
        usedFlags.addAll(it.flags)
    }

    return CommandParameterSet(parameters)
}

internal fun BaseCommand<*>.generateSenders(): Set<KClass<*>>? {
    val senderSettings = mutableListOf<SenderSettings>()

    this.javaClass.declaredFields.forEach { field ->
        if (field.kotlinProperty?.hasAnnotation<Sender>() == true) {
            registerSender(field)

            senderSettings.add(SenderSettings.fromType(type = field.kotlinProperty!!.returnType))
        }
    }

    this.commandFunction?.also { commandFunction ->
        senderSettings.addAll(
            commandFunction.parameters
                .filter { it.isSenderParameter() }
                .map { SenderSettings.fromType(type = it.type) }
        )
    }

    return generateSenders(senderSettings)
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
 * Returns a set of flags for this [CommandParameter] without clashing with any of the [unavailableFlags].
 */
internal fun CommandParameter.generateFlags(unavailableFlags: Set<Char> = setOf()): Set<Char> {
    require(this.id.isNotEmpty())

    val flags = mutableSetOf<Char>()

    val firstLetterLower = this.id.first().let { if (it.isUpperCase()) it.lowercaseChar() else it }
    val firstLetterUpper = firstLetterLower.uppercaseChar()

    if (!unavailableFlags.contains(firstLetterLower)) flags.add(firstLetterLower)
    else if (!unavailableFlags.contains(firstLetterUpper)) flags.add(firstLetterUpper)

    return flags
}

/**
 * Returns the overridden version of this function in the [clazz], or null if this function is not declared in the
 * [clazz].
 */
internal fun KFunction<*>.getDeclaredOverride(clazz: KClass<*>): KFunction<*>? =
    clazz.declaredMemberFunctions.find { function ->
        function.name == this.name &&
                function.parameters.filter { it.kind == KParameter.Kind.VALUE } ==
                this.parameters.filter { it.kind == KParameter.Kind.VALUE } &&
                function != this
    }

/**
 * Returns the command function to use in this command, or null if another strategy is used to handle command execution.
 *
 * The function to use is found by the following priority:
 * - first method annotated with @[CommandFunction]
 * - first public method
 * - first internal method
 * - first method
 *
 * @throws ClassCastException if the function exists but has the wrong return type
 */
@Throws(ClassCastException::class)
internal fun <R> BaseCommand<R>.getCommandFunction(): KFunction<R>? {
    var firstCommandFunction: KFunction<*>? = null
    var firstPublicFunction: KFunction<*>? = null
    var firstInternalFunction: KFunction<*>? = null
    var firstFunction: KFunction<*>? = null

    for (function in this::class.declaredMemberFunctions) {
        if (function.hasAnnotation<CommandFunction>()) {
            firstCommandFunction = function
        } else if (function.visibility == KVisibility.PUBLIC) {
            firstPublicFunction = function
        } else if (function.visibility == KVisibility.INTERNAL) {
            firstInternalFunction = function
        } else {
            firstFunction = function
        }
    }

    if (firstCommandFunction == null && this.getCallFunctionOverride() != null) return null

    val function: KFunction<*> =
        firstCommandFunction ?: firstPublicFunction ?: firstInternalFunction ?: firstFunction ?: return null

    return try {
        @Suppress("UNCHECKED_CAST")
        function as KFunction<R>
    } catch (e: ClassCastException) {
        throw ClassCastException(
            "The return type: \"${function.returnType.jvmErasure.qualifiedName}\"" +
                    " of the command function: \"${function.name}\"" +
                    " in ${this::class.qualifiedName}" +
                    " does not match the command's return type."
        ).initCause(e)
    }
}

/**
 * Generates a "usage string" that matches the [Command.parameters].
 */
internal fun Command<*>.generateUsage(): String {
    val sb = StringBuilder(name)

    parameters.forEach { parameter ->
        val defaultValueRepresentation = parameter.defaultValueString
        val wrap = if (parameter.optional) Pair("[", "]") else Pair("<", ">")

        sb.append(" ${wrap.first}${parameter.id}${defaultValueRepresentation?.let { "=$defaultValueRepresentation" } ?: ""}${wrap.second}")
    }

    return sb.toString()
}
