package dev.gimme.gimmeapi.command.property

import dev.gimme.gimmeapi.command.BaseCommand
import dev.gimme.gimmeapi.command.ParameterTypes
import dev.gimme.gimmeapi.command.SenderTypes
import dev.gimme.gimmeapi.command.node.CommandNode
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.parameter.DefaultValue
import dev.gimme.gimmeapi.command.parameter.ParameterType
import dev.gimme.gimmeapi.command.sender.CommandSender
import dev.gimme.gimmeapi.core.common.splitCamelCase
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

/**
 * TODO
 *
 * @param R the type of the result of the command
 */
abstract class PropertyCommand<out R>(
    name: String,
    parent: CommandNode? = null,
    aliases: Set<String> = setOf(),
    summary: String = "",
    description: String = "",
) : BaseCommand<R>(
    name = name,
    parent = parent,
    aliases = aliases,
    summary = summary,
    description = description,
) {

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())

    final override var usage: String = "" // TODO

    private var requiredSender: KClass<*>? = null
    private var optionalSenders: MutableSet<KClass<*>>? = null
    override val senderTypes: Set<KClass<*>>? get() = requiredSender?.let { setOf(it) } ?: optionalSenders

    private lateinit var _commandSender: CommandSender
    private lateinit var _args: Map<CommandParameter, Any?>

    override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): R {
        _commandSender = commandSender
        _args = args

        return call()
    }

    abstract fun call(): R

    @JvmSynthetic
    protected fun <T> sender(): SenderProperty<T> = SenderProperty()

    @JvmOverloads
    protected fun <T : Any> sender(klass: Class<T>, optional: Boolean = false): Sender<T> =
        createSenderDelegate(klass.kotlin, optional)

    @JvmSynthetic
    protected fun <T> param(): ParamBuilder<T> = ParamBuilder(null)

    protected fun <T : Any> param(klass: Class<T>): ParamBuilder<T> = ParamBuilder(klass.kotlin)

    protected inner class ParamBuilder<out T> internal constructor(
        private var klass: KClass<*>?
    ) : CommandProperty<T> {

        private var name: String? = null
        private var defaultValue: DefaultValue? = null
        private var form: CommandParameter.Form? = null
        private var suggestions: (() -> Set<String>)? = null

        fun name(name: String) = apply { this.name = name }

        /** @see DefaultValue */
        @JvmOverloads
        @JvmName("defaultValue")
        fun default(value: String?, representation: String? = value) =
            apply { this.defaultValue = DefaultValue(value, representation) }

        private fun form(form: CommandParameter.Form) = apply { this.form = form }

        fun suggestions(suggestions: () -> Set<String>) = apply { this.suggestions = suggestions }

        @JvmSynthetic
        override operator fun provideDelegate(thisRef: PropertyCommand<*>, property: KProperty<*>): Param<T> {
            if (name == null) name(property.name)

            val jvmErasure = property.returnType.jvmErasure
            val form = when {
                jvmErasure.isSuperclassOf(List::class) -> CommandParameter.Form.LIST
                jvmErasure.isSuperclassOf(Set::class) -> CommandParameter.Form.SET
                else -> CommandParameter.Form.VALUE
            }
            form(form)

            if (klass == null) {
                klass = if (form.isCollection) {
                    property.returnType.arguments.firstOrNull()?.type?.jvmErasure
                        ?: throw RuntimeException("Unsupported parameter type: ${property.returnType}") // TODO: exception type
                } else {
                    jvmErasure
                }
            }
            if (defaultValue == null && property.returnType.isMarkedNullable) default(null)

            defaultValue?.let { defaultValue ->
                if (!property.returnType.isMarkedNullable && defaultValue.value == null) {
                    throw IllegalStateException("Parameter \"$name\" has a null default value for a type marked as non-nullable") // TODO: exception type
                }
            }

            return build()
        }

        fun build() = buildOfType<T>()
        fun buildList(): Param<List<T>> = form(CommandParameter.Form.LIST).buildOfType()
        fun buildSet(): Param<Set<T>> = form(CommandParameter.Form.SET).buildOfType()

        private fun <S> buildOfType(): Param<S> {
            val name = name ?: "param${parameters.size + 1}"
            val klass = klass!! // TODO: Error message on null
            val form = form ?: CommandParameter.Form.VALUE

            val type = ParameterTypes.get(klass)
            val suggestions = suggestions ?: type.values ?: { setOf() }

            val id = name.splitCamelCase("-")
            val displayName = name.splitCamelCase(" ")
            val flags = setOf<Char>() // TODO

            val param = Param<S>(
                id = id,
                displayName = displayName,
                type = type,
                form = form,
                suggestions = suggestions,
                description = null, // TODO
                flags = flags,
                defaultValue = defaultValue
            )
            if (!parameters.add(param)) throw RuntimeException("A parameter with the name \"${param.id}\" has already exists") // TODO: exception type
            return param
        }
    }

    protected inner class Param<out T>(
        id: String,
        displayName: String,
        type: ParameterType<*>,
        form: Form,
        suggestions: () -> Set<String>,
        description: String?,
        flags: Set<Char>,
        defaultValue: DefaultValue?,
    ) : CommandParameter(
        id = id,
        displayName = displayName,
        type = type,
        form = form,
        suggestions = suggestions,
        description = description,
        flags = flags,
        defaultValue = defaultValue
    ), CommandDelegate<T> {

        @JvmSynthetic
        override operator fun getValue(thisRef: PropertyCommand<*>, property: KProperty<*>): T = get()

        @Suppress("UNCHECKED_CAST")
        fun get() = _args[this] as T
    }

    private fun <T> createSenderDelegate(klass: KClass<*>, optional: Boolean): Sender<T> {
        if (!optional) {
            if (requiredSender != null) throw IllegalStateException("Only one sender type can be required (i.e., non-null)") // TODO: exception type
            requiredSender = klass
        } else {
            optionalSenders = (optionalSenders ?: mutableSetOf()).apply { add(klass) }
        }

        return Sender(klass)
    }

    protected inner class SenderProperty<out T> internal constructor() : CommandProperty<T> {

        @JvmSynthetic
        override operator fun provideDelegate(thisRef: PropertyCommand<*>, property: KProperty<*>): CommandDelegate<T> {
            @Suppress("UNCHECKED_CAST")
            val klass = property.returnType.jvmErasure
            val optional = property.returnType.isMarkedNullable

            return createSenderDelegate(klass, optional)
        }
    }

    protected inner class Sender<out T>(private val klass: KClass<*>) : CommandDelegate<T> {

        @JvmSynthetic
        override operator fun getValue(thisRef: PropertyCommand<*>, property: KProperty<*>): T = get()

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
}

