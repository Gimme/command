package dev.gimme.command

import dev.gimme.command.annotations.Sender
import dev.gimme.command.construction.generateParameters
import dev.gimme.command.construction.generateSenders
import dev.gimme.command.construction.generateUsage
import dev.gimme.command.construction.getDeclaredOverride
import dev.gimme.command.construction.getCommandFunction
import dev.gimme.command.exception.CommandException
import dev.gimme.command.exception.ErrorCode
import dev.gimme.command.annotations.CommandFunction
import dev.gimme.command.common.JAVA_ONLY
import dev.gimme.command.common.JAVA_ONLY_WARNING
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
    description: String = "",
    detailedDescription: String? = null,
    override val permission: String? = null,
) : BaseCommandNode(
    name = name,
    parent = parent,
    aliases = aliases,
    description = description,
    detailedDescription = detailedDescription
), Command<R> {

    override val parameters: CommandParameterSet by lazy { generateParameters() }
    private val senderTypes: Set<KClass<*>>? by lazy { generateSenders() }

    override val usage: String by lazy { generateUsage() }

    internal val commandFunction: KFunction<R>? = getCommandFunction()

    private val argumentPropertySetters: MutableMap<CommandParameter, (Any?) -> Unit> = mutableMapOf()
    private val initializedProperties: MutableSet<Param<*>> = mutableSetOf()
    private val senderFields: MutableSet<Field> = mutableSetOf()

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())

    final override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): R {
        if (senderTypes?.any {
                commandSender::class.isSubclassOf(it) || SenderTypes.adapt(commandSender, it) != null
            } == false) throw ErrorCode.INCOMPATIBLE_SENDER.createException()

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

        @Suppress("NAME_SHADOWING")
        val args = args.toMutableMap()
        initializedProperties.clear()

        parameters.forEach { parameter ->
            if (parameter.optional && args[parameter] == null) {
                args[parameter] = parameter.defaultValue?.invoke()
            }
            val arg = args[parameter]
            argumentPropertySetters[parameter]?.invoke(arg)
        }

        val response = if (commandFunction != null) {
            executeByFunction(commandFunction, commandSender, args)
        } else {
            call()
        }

        argumentPropertySetters.values.forEach { it(null) }

        return response
    }

    /**
     * Calls this command.
     *
     * Does not get called if there is another defined command function.
     *
     * @throws CommandException if the call failed
     */
    @Throws(CommandException::class)
    protected open fun call(): R {
        throw NotImplementedError()
    }

    /**
     * Returns the overridden version of the [call] function if exists.
     */
    internal fun getCallFunctionOverride(): KFunction<*>? = this::call.getDeclaredOverride(this::class)

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
        var order = 0
        params
            .filter { it.kind == KParameter.Kind.VALUE }
            .forEach { param ->
                val senderIsSubclass = commandSender::class.isSubclassOf(param.type.jvmErasure)
                if (senderIsSubclass || param.hasAnnotation<Sender>()) { // Sender
                    val sender: Any? =
                        if (senderIsSubclass) param.type.jvmErasure.safeCast(commandSender)
                        else SenderTypes.adapt(commandSender, param.type.jvmErasure)

                    val optional = param.type.isMarkedNullable
                    if (sender == null && !optional) throw ErrorCode.INCOMPATIBLE_SENDER.createException()

                    typedArgsMap[param] = sender
                } else { // Argument
                    val arg = args[parameters.getAt(order++)]
                    if (!(param.isOptional && arg == null)) {
                        typedArgsMap[param] = arg
                    }
                }
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

    protected inner class ParamBuilder<T> internal constructor() : CommandProperty<T> {

        private val param = object : Param<T> {
            override var suggestions: (() -> Set<String>)? = null
            override var optional: Boolean = false
            override var defaultValue: (() -> T)? = null
            override var defaultValueString: String? = null

            private var value: Any? = null

            @Suppress("UNCHECKED_CAST")
            override fun get(): T {
                if (!initializedProperties.contains(this)) throw UninitializedPropertyAccessException("Trying to access argument before it has been initialized")
                return value as T
            }

            override fun set(value: Any?) {
                this.value = value
                initializedProperties.add(this)
            }
        }

        @JvmName("defaultValue")
        fun default(value: T) = default(value?.toString()) { value }

        @JvmName("defaultValue")
        fun default(representation: String? = null, value: () -> T) = apply {
            param.optional = true
            param.defaultValue = value
            param.defaultValueString = representation
        }

        fun suggestions(suggestions: () -> Set<String>) = apply { param.suggestions = suggestions }

        @JvmSynthetic
        override operator fun provideDelegate(thisRef: BaseCommand<*>, property: KProperty<*>): Param<T> = buildParam()

        @Suppress(JAVA_ONLY_WARNING, "UNCHECKED_CAST")
        @SinceKotlin(JAVA_ONLY)
        fun <T> build(): Param<T> = buildParam() as Param<T>

        private fun buildParam(): Param<T> = param
    }

    interface Param<out T> : CommandDelegate<T> {
        val suggestions: (() -> Set<String>)?
        val optional: Boolean
        val defaultValue: (() -> T)?
        val defaultValueString: String?

        fun get(): T
        fun set(value: Any?)

        @JvmSynthetic
        override operator fun getValue(thisRef: BaseCommand<*>, property: KProperty<*>): T = get()
    }
}
