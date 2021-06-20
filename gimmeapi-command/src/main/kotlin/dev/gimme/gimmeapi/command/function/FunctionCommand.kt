package dev.gimme.gimmeapi.command.function

import dev.gimme.gimmeapi.command.BaseCommand
import dev.gimme.gimmeapi.command.Command
import dev.gimme.gimmeapi.command.SenderTypes
import dev.gimme.gimmeapi.command.annotations.Sender
import dev.gimme.gimmeapi.command.exception.CommandException
import dev.gimme.gimmeapi.command.exception.ErrorCode
import dev.gimme.gimmeapi.command.node.CommandNode
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.sender.CommandSender
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

/**
 * Represents an easy-to-set-up command with automatic generation of some properties derived from a member function
 * marked with @[CommandFunction].
 *
 * If a method in this is marked with @[CommandFunction], the command's [parameters] and [usage] are automatically
 * derived from it, and it gets called called when the command is executed.
 *
 * @param T the response type
 */
abstract class FunctionCommand<out T>(
    name: String,
    parent: CommandNode? = null,
    aliases: Set<String> = setOf(),
    summary: String = "",
    description: String = "",
) : BaseCommand<T>(
    name = name,
    parent = parent,
    aliases = aliases,
    summary = summary,
    description = description,
) {

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())

    private val commandFunction: KFunction<T> = getFirstCommandFunction()
    private val commandFunctionAnnotation: CommandFunction = commandFunction.findAnnotation()!!

    final override var usage: String = generateUsage()

    init {
        val senderParameters: List<KParameter> = commandFunction.parameters
            .filter {
                it.kind == KParameter.Kind.VALUE &&
                        (it.type.isSubtypeOf(CommandSender::class.createType(nullable = true)) || it.hasAnnotation<Sender>())
            }
        val valueParameters: List<KParameter> = commandFunction.parameters
            .minus(senderParameters)
            .filter { it.kind == KParameter.Kind.VALUE }

        generateSenders(senderParameters.map { SenderSettings(type = it.type) })
        generateParameters(valueParameters.map { param ->
            ParameterSettings(
                name = param.name ?: throw UnsupportedParameterException(param),
                annotation = param.findAnnotation(),
                type = param.type,
            )
        })
    }

    /**
     * Attempts to execute this command as the [commandSender] with the args mapping of parameters to arguments and
     * returns the result.
     *
     * The [args] have to fit the parameters of the function annotated with @[CommandFunction].
     *
     * @throws CommandException if the command execution was unsuccessful
     */
    @Throws(CommandException::class)
    override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): T {
        val function = commandFunction

        val params: List<KParameter> = function.parameters

        // First argument has to be the instance (command)
        val typedArgsMap: MutableMap<KParameter, Any?> = mutableMapOf(params[0] to this)

        // Inject command senders
        params.forEach { param ->
            val sender: Any? = when {
                commandSender::class.isSubclassOf(param.type.jvmErasure) -> {
                    param.type.jvmErasure.safeCast(commandSender)
                }
                param.hasAnnotation<Sender>() -> {
                    SenderTypes.adapt(commandSender, param.type.jvmErasure)
                }
                else -> return@forEach
            }

            val optional = param.type.isMarkedNullable
            if (sender == null && !optional) throw ErrorCode.INCOMPATIBLE_SENDER.createException()

            typedArgsMap[param] = sender
        }

        args.forEach { (key, value) ->
            val p = params.find { it.name == key.id } ?: throw ErrorCode.INVALID_PARAMETER.createException(key.id)
            typedArgsMap[p] = value
        }

        function.isAccessible = true
        return function.callBy(typedArgsMap)
    }

    /**
     * Generates a "usage string" that matches the [Command.parameters].
     */
    private fun Command<*>.generateUsage(): String {
        val sb = StringBuilder(name)

        parameters.forEach { parameter ->
            val defaultValueRepresentation = parameter.defaultValue?.representation
            val wrap = if (parameter.optional) Pair("[", "]") else Pair("<", ">")

            sb.append(" ${wrap.first}${parameter.id}${defaultValueRepresentation?.let { "=$defaultValueRepresentation" } ?: ""}${wrap.second}")
        }

        return sb.toString()
    }

    /**
     * Returns the first found method that is annotated with @[CommandFunction].
     *
     * @param T the command response type
     * @throws IllegalStateException if there is no method annotated with @[CommandFunction] or if it has the wrong return
     * type
     */
    @Throws(CommandException::class)
    internal fun <T> Command<T>.getFirstCommandFunction(): KFunction<T> {
        // Look through the public methods in the command class
        for (function in this::class.memberFunctions) {
            // Make sure it has the right annotation
            if (!function.hasAnnotation<CommandFunction>()) continue

            return try {
                @Suppress("UNCHECKED_CAST")
                function as KFunction<T>
            } catch (e: ClassCastException) {
                throw ClassCastException(
                    "The return type: \"${function.returnType.jvmErasure.qualifiedName}\"" +
                            " of the command function: \"${function.name}\"" +
                            " in ${this::class.qualifiedName}" +
                            " does not match the command's return type."
                )
            }
        }

        throw IllegalStateException("No function marked with @${CommandFunction::class.simpleName} in ${this::class.qualifiedName}")
    }
}
