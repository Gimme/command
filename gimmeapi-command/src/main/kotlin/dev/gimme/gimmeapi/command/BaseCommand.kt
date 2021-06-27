package dev.gimme.gimmeapi.command

import dev.gimme.gimmeapi.command.annotations.Parameter
import dev.gimme.gimmeapi.command.annotations.Sender
import dev.gimme.gimmeapi.command.annotations.getDefaultValue
import dev.gimme.gimmeapi.command.exception.CommandException
import dev.gimme.gimmeapi.command.exception.ErrorCode
import dev.gimme.gimmeapi.command.function.CommandFunction
import dev.gimme.gimmeapi.command.function.UnsupportedParameterException
import dev.gimme.gimmeapi.command.node.BaseCommandNode
import dev.gimme.gimmeapi.command.node.CommandNode
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.parameter.CommandParameterSet
import dev.gimme.gimmeapi.command.parameter.DefaultValue
import dev.gimme.gimmeapi.command.property.CommandDelegate
import dev.gimme.gimmeapi.command.property.CommandProperty
import dev.gimme.gimmeapi.command.sender.CommandSender
import dev.gimme.gimmeapi.core.common.splitCamelCase
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinProperty

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

    private lateinit var _commandSender: CommandSender
    private lateinit var _args: Map<CommandParameter, Any?>

    private var paramFieldById: MutableMap<String, Field> = mutableMapOf()
    private var paramBuilderFieldById: MutableMap<String, Param<*>> = mutableMapOf()
    // TODO: same for senders as above
    private var senderFields: MutableSet<Field> = mutableSetOf()

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())

    override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): R {
        if (senderTypes?.any {
                commandSender::class.isSubclassOf(it) || SenderTypes.adapt(commandSender, it) != null
            } == false) throw ErrorCode.INCOMPATIBLE_SENDER.createException()

        _commandSender = commandSender
        _args = args

        paramFieldById.forEach { (id, field) ->
            field.isAccessible = true
            field.set(this, args.entries.firstOrNull { it.key.id == id }?.value)
        }

        paramBuilderFieldById.forEach { (id, param) ->
            param.set(args.entries.firstOrNull { it.key.id == id }?.value)
        }

        senderFields.forEach { field ->
            val klass = field.type.kotlin
            var value: Any? = null

            if (commandSender::class.isSubclassOf(klass)) {
                value = commandSender
            } else {
                SenderTypes.adapt(_commandSender, klass)?.also {
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
    private fun executeByFunction(function: KFunction<R>, commandSender: CommandSender, args: Map<CommandParameter, Any?>): R {
        val params: List<KParameter> = function.parameters

        // First argument has to be the instance (command)
        val typedArgsMap: MutableMap<KParameter, Any?> = mutableMapOf(params[0] to this)

        // Inject command senders
        params.forEach { param ->
            val sender: Any? = when {
                commandSender::class.isSubclassOf(param.type.jvmErasure) -> {
                    param.type.jvmErasure.safeCast(commandSender)
                }
                param.hasAnnotation<dev.gimme.gimmeapi.command.annotations.Sender>() -> {
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

    protected class ParameterSettings private constructor(
        val name: String,
        val form: CommandParameter.Form,
        val klass: KClass<*>,
        val suggestions: (() -> Set<String>)?,
        val defaultValue: DefaultValue?,
        val description: String?,
    ) {
        companion object {
            fun fromType(
                name: String,
                annotation: Parameter?,
                type: KType,
                suggestions: (() -> Set<String>)? = null,
                defaultValue: DefaultValue? = null,
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
                )
            }
        }
    }

    protected class SenderSettings(
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

    private fun generateParameters(): CommandParameterSet {
        val paramSettings = mutableListOf<ParameterSettings>()

        val declaredFields = this.javaClass.declaredFields
        println(declaredFields)

        this.javaClass.declaredFields.forEach { field ->
            val paramAnnotation: Parameter? = field.kotlinProperty?.findAnnotation()

            val settings = when {
                paramAnnotation != null -> {
                    val name = field.name

                    paramFieldById[name] = field

                    ParameterSettings.fromType(
                        name = name,
                        annotation = paramAnnotation,
                        type = field.kotlinProperty!!.returnType
                    )
                }
                Param::class.java.isAssignableFrom(field.type) -> {
                    val name = field.name.removeSuffix("\$delegate")

                    field.isAccessible = true
                    @Suppress("UNCHECKED_CAST")
                    val value = field.get(this) as Param<*>

                    paramBuilderFieldById[name] = value

                    val type: KType = field.kotlinProperty!!.returnType.let {
                        if (it.jvmErasure.isSubclassOf(Param::class)) {
                            it.arguments.first().type!!
                        } else {
                            it
                        }
                    }

                    ParameterSettings.fromType(
                        name = name,
                        annotation = null,
                        type = type,
                        suggestions = value.suggestions,
                        defaultValue = value.defaultValue,
                    )
                }
                else -> null
            }

            if (settings != null) {
                paramSettings.add(settings)
            }
        }

        commandFunction?.also { commandFunction ->
            val senderParameters: List<KParameter> = commandFunction.parameters
                .filter {
                    it.kind == KParameter.Kind.VALUE &&
                            (it.type.isSubtypeOf(CommandSender::class.createType(nullable = true)) || it.hasAnnotation<dev.gimme.gimmeapi.command.annotations.Sender>())
                }

            val valueParameters: List<KParameter> = commandFunction.parameters
                .minus(senderParameters)
                .filter { it.kind == KParameter.Kind.VALUE }

            paramSettings.addAll(valueParameters.map { param ->
                ParameterSettings.fromType(
                    name = param.name ?: throw UnsupportedParameterException(param),
                    annotation = param.findAnnotation(),
                    type = param.type,
                )
            })
        }

        return generateParameters(paramSettings)
    }

    private fun generateSenders(): Set<KClass<*>>? {
        val senderSettings = mutableListOf<SenderSettings>()

        this.javaClass.declaredFields.forEach { field ->
            val senderAnnotation: Sender? = field.kotlinProperty?.findAnnotation()

            if (senderAnnotation != null) {
                senderFields.add(field)

                senderSettings.add(SenderSettings.fromType(type = field.kotlinProperty!!.returnType))
            } else if (S::class.java.isAssignableFrom(field.type)) {
                field.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val value = field.get(this) as S<*>

                senderSettings.add(
                    SenderSettings(
                        klass = value.klass,
                        optional = value.optional,
                    )
                )
            }
        }

        commandFunction?.also { commandFunction ->
            val senderParameters: List<KParameter> = commandFunction.parameters
                .filter {
                    it.kind == KParameter.Kind.VALUE &&
                            (it.type.isSubtypeOf(CommandSender::class.createType(nullable = true)) || it.hasAnnotation<dev.gimme.gimmeapi.command.annotations.Sender>())
                }

            senderSettings.addAll(senderParameters.map { SenderSettings.fromType(type = it.type) })
        }

        return generateSenders(senderSettings)
    }

    /**
     * Generates a set of [CommandParameter]s based on the supplied [parameterSettings].
     */
    private fun generateParameters(parameterSettings: List<ParameterSettings>): CommandParameterSet {
        val usedFlags = mutableSetOf<Char>()

        return CommandParameterSet(parameterSettings.map { settings ->
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

            CommandParameter(
                id = id,
                name = name,
                type = type,
                form = form,
                suggestions = suggestions,
                flags = flags,
                defaultValue = defaultValue,
                description = description
            )
        }.also { parameterList ->
            val groupedParameters: Map<String, Int> =
                parameterList.groupingBy { it.id }.eachCount().filter { it.value > 1 }

            groupedParameters.keys.firstOrNull()?.let {
                throw RuntimeException("A parameter with the name \"${it}\" already exists") // TODO: exception type
            }
        })
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

    /**
     * Generates a set of flags from the [string] without clashing with any of the [unavailableFlags].
     */
    private fun generateFlags(string: String, unavailableFlags: Set<Char> = setOf()): Set<Char> {
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
    private fun getFirstCommandFunction(): KFunction<R>? {
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
    private fun Command<*>.generateUsage(): String {
        val sb = StringBuilder(name)

        parameters.forEach { parameter ->
            val defaultValueRepresentation = parameter.defaultValue?.representation
            val wrap = if (parameter.optional) Pair("[", "]") else Pair("<", ">")

            sb.append(" ${wrap.first}${parameter.id}${defaultValueRepresentation?.let { "=$defaultValueRepresentation" } ?: ""}${wrap.second}")
        }

        return sb.toString()
    }

    protected inner class S<out T>(val klass: KClass<*>, val optional: Boolean) : CommandDelegate<T> {

        @JvmSynthetic
        override operator fun getValue(thisRef: BaseCommand<*>, property: KProperty<*>): T = get()

        @Suppress("UNCHECKED_CAST")
        fun get(): T {
            if (_commandSender::class.isSubclassOf(klass)) {
                return _commandSender as T
            }

            SenderTypes.adapt(_commandSender, klass)?.let {
                return it as T
            }

            return null as T
        }
    }

    protected interface Param<out T> {
        val form: CommandParameter.Form
        val suggestions: (() -> Set<String>)?
        val defaultValue: DefaultValue?

        fun get(): T
        fun set(value: Any?)
    }

    protected inner class ParamBuilder<out T> internal constructor() : CommandProperty<T>, CommandDelegate<T>, Param<T> {

        @JvmSynthetic
        override operator fun getValue(thisRef: BaseCommand<*>, property: KProperty<*>): T = get()

        private var value: Any? = null
        @Suppress("UNCHECKED_CAST")
        override fun get() = value as T
        override fun set(value: Any?) {
            this.value = value
        }

        private var _defaultValue: DefaultValue? = null
        private var _form: CommandParameter.Form? = null
        private var _suggestions: (() -> Set<String>)? = null

        /** @see DefaultValue */
        @JvmOverloads
        @JvmName("defaultValue")
        fun default(value: String?, representation: String? = value) =
            apply { this._defaultValue = DefaultValue(value, representation) }

        private fun form(form: CommandParameter.Form) = apply { this._form = form }

        fun suggestions(suggestions: () -> Set<String>) = apply { this._suggestions = suggestions }

        @JvmSynthetic
        override operator fun provideDelegate(thisRef: BaseCommand<*>, property: KProperty<*>): ParamBuilder<T> {
            return this
        }

        override val form: CommandParameter.Form
            get() = _form ?: CommandParameter.Form.VALUE
        override val suggestions: (() -> Set<String>)?
            get() = _suggestions
        override val defaultValue: DefaultValue?
            get() = _defaultValue
    }

    @JvmSynthetic
    protected fun <T> sender(): SenderProperty<T> = SenderProperty()

    @JvmOverloads
    protected fun <T : Any> sender(klass: Class<T>, optional: Boolean = false): S<T> =
        createSenderDelegate(klass.kotlin, optional)

    protected fun <T> param(): ParamBuilder<T> = ParamBuilder()

    private fun <T> createSenderDelegate(klass: KClass<*>, optional: Boolean): S<T> {
        return S(klass, optional)
    }

    protected inner class SenderProperty<out T> internal constructor() : CommandProperty<T> {

        @JvmSynthetic
        override operator fun provideDelegate(thisRef: BaseCommand<*>, property: KProperty<*>): CommandDelegate<T> {
            @Suppress("UNCHECKED_CAST")
            val klass = property.returnType.jvmErasure
            val optional = property.returnType.isMarkedNullable

            return createSenderDelegate(klass, optional)
        }
    }
}
