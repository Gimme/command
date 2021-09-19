package dev.gimme.command

import dev.gimme.command.annotations.Sender
import dev.gimme.command.construction.generateParameters
import dev.gimme.command.construction.generateSenders
import dev.gimme.command.construction.generateUsage
import dev.gimme.command.construction.getFirstCommandFunction
import dev.gimme.command.exception.CommandException
import dev.gimme.command.exception.ErrorCode
import dev.gimme.command.function.CommandFunction
import dev.gimme.command.node.BaseCommandNode
import dev.gimme.command.node.CommandNode
import dev.gimme.command.parameter.CommandParameter
import dev.gimme.command.parameter.CommandParameterSet
import dev.gimme.command.property.CommandDelegate
import dev.gimme.command.property.CommandProperty
import dev.gimme.command.sender.CommandSender
import dev.gimme.command.sender.SenderTypes
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

/**
 * A base implementation of command with useful "hashCode" and "equals" methods.
 *
 * @param R the response type
 * @property senderTypes the only types of senders allowed to execute this command, or null if no limitation
 */
abstract class BaseCommand<out R>(
    name: String,
    parent: CommandNode? = null,
    aliases: Set<String> = setOf(),
    override var summary: String = "",
    override var description: String = "",
) : BaseCommandNode(name, parent, aliases), Command<R> {

    override val parameters: CommandParameterSet by lazy { generateParameters() }
    private val senderTypes: Set<KClass<*>>? by lazy { generateSenders() }

    override val usage: String by lazy { generateUsage() }

    private val commandFunction: KFunction<R>? = getFirstCommandFunction()

    private val argumentPropertySetters: MutableMap<CommandParameter, (Any?) -> Unit> = mutableMapOf()
    private val senderFields: MutableSet<Field> = mutableSetOf()

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())

    final override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): R {
        if (senderTypes?.any {
                commandSender::class.isSubclassOf(it) || SenderTypes.adapt(commandSender, it) != null
            } == false) throw ErrorCode.INCOMPATIBLE_SENDER.createException()

        @Suppress("NAME_SHADOWING")
        val args = args.toMutableMap()
        parameters.filter { it.optional && args[it] == null }.forEach {
            args[it] = it.defaultValue
        }

        args.forEach { (parameter, arg) ->
            argumentPropertySetters[parameter]?.invoke(arg)
        }

        senderFields.forEach { field ->
            val klass = field.type.kotlin
            var value: Any? = null

            if (commandSender::class.isSubclassOf(klass)) {
                value = commandSender
            } else {
                SenderTypes.adapt(commandSender, klass)?.also {
                    value = it
                }
            }

            field.isAccessible = true
            field.set(this, value)
        }

        return if (commandFunction != null) {
            executeByFunction(commandFunction, commandSender, args)
        } else {
            call()
        }
    }

    @Throws(CommandException::class)
    protected open fun call(): R {
        throw NotImplementedError()
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
    private fun executeByFunction(
        function: KFunction<R>,
        commandSender: CommandSender,
        args: Map<CommandParameter, Any?>
    ): R {
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

    internal fun registerParameter(parameter: CommandParameter, onExecute: ((Any?) -> Unit)): CommandParameter {
        argumentPropertySetters[parameter] = onExecute
        return parameter
    }

    internal fun registerSender(field: Field): Field {
        senderFields.add(field)
        return field
    }

    protected fun <T> param(): ParamBuilder<T> = ParamBuilder()

    protected inner class ParamBuilder<out T> internal constructor() : CommandProperty<T>, CommandDelegate<T>,
        Param<T> {

        override var suggestions: (() -> Set<String>)? = null
        override var optional: Boolean = false
        override var defaultValue: Any? = null
        override var defaultValueString: String? = null

        private var value: Any? = null

        @Suppress("UNCHECKED_CAST")
        override fun get() = value as T
        override fun set(value: Any?) {
            this.value = value
        }

        @JvmOverloads
        @JvmName("defaultValue")
        @Suppress("UNCHECKED_CAST")
        fun <T> default(value: T?, representation: String? = value?.toString()) =
            apply {
                this.optional = true
                this.defaultValue = value
                this.defaultValueString = representation
            } as ParamBuilder<T>

        fun suggestions(suggestions: () -> Set<String>) = apply { this.suggestions = suggestions }

        @JvmSynthetic
        override operator fun provideDelegate(thisRef: BaseCommand<*>, property: KProperty<*>): ParamBuilder<T> {
            return this
        }

        @JvmSynthetic
        override operator fun getValue(thisRef: BaseCommand<*>, property: KProperty<*>): T = get()
    }

    interface Param<out T> {
        val suggestions: (() -> Set<String>)?
        val optional: Boolean
        val defaultValue: Any?
        val defaultValueString: String?

        fun get(): T
        fun set(value: Any?)
    }
}
