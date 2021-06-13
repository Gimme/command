package dev.gimme.gimmeapi.command.property

import dev.gimme.gimmeapi.command.BaseCommand
import dev.gimme.gimmeapi.command.ParameterTypes
import dev.gimme.gimmeapi.command.node.CommandNode
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.parameter.CommandParameterSet
import dev.gimme.gimmeapi.command.parameter.DefaultValue
import dev.gimme.gimmeapi.command.parameter.ParameterType
import dev.gimme.gimmeapi.command.sender.CommandSender
import dev.gimme.gimmeapi.core.common.splitCamelCase
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
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

    final override var parameters: CommandParameterSet = CommandParameterSet()
    final override var usage: String = "" // TODO

    private lateinit var _commandSender: CommandSender
    private lateinit var _args: Map<CommandParameter, Any?>

    override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): R {
        _commandSender = commandSender
        _args = args

        return call()
    }

    abstract fun call(): R

    fun <T : CommandSender> sender(): CommandProperty<T> = SenderProperty()

    @JvmSynthetic
    fun <T> param(): ParamBuilder<T> = ParamBuilder(null)
    fun <T : Any> param(klass: KClass<T>): ParamBuilder<T> = ParamBuilder(klass)
    fun <T : Any> param(klass: Class<T>): ParamBuilder<T> = ParamBuilder(klass.kotlin)

    inner class ParamBuilder<out T> internal constructor(
        private var klass: KClass<*>?
    ) : CommandProperty<T> {

        private var name: String? = null
        private var vararg: Boolean? = null
        private var optional: Boolean? = null

        override operator fun provideDelegate(thisRef: PropertyCommand<*>, property: KProperty<*>): Param<T> {
            if (name == null) name(property.name)
            val isList = property.returnType.jvmErasure.isSuperclassOf(List::class)
            if (klass == null) {
                klass = if (isList) {
                    property.returnType.arguments.firstOrNull()?.type?.jvmErasure
                        ?: throw RuntimeException("Unsupported parameter type: ${property.returnType}") // TODO: exception type
                } else {
                    property.returnType.jvmErasure
                }
            }
            if (vararg == null) vararg(isList)
            if (optional == null && property.returnType.isMarkedNullable) optional()

            return build()
        }

        fun name(name: String) = apply { this.name = name }
        fun vararg(vararg: Boolean) = apply { this.vararg = vararg }
        fun optional() = apply { this.optional = true }

        fun build() = buildOfType<T>()
        fun buildList(): Param<List<T>> {
            vararg = true

            return buildOfType()
        }

        private fun <S> buildOfType(): Param<S> {
            val name = name!! // TODO: Error message on null
            val klass = klass!! // TODO: Error message on null
            val vararg = vararg ?: false

            val id = name.splitCamelCase("-")
            val displayName = name.splitCamelCase(" ")
            val flags = setOf<Char>() // TODO
            val defaultValue: DefaultValue? = null // TODO
            val isOptional = optional ?: vararg

            val type = ParameterTypes.get(klass)

            val param = Param<S>(
                id = id,
                displayName = displayName,
                type = type,
                vararg = vararg,
                optional = isOptional, // TODO: check if default value exists?
                suggestions = type.values ?: { setOf() },
                description = null, // TODO
                flags = flags,
                defaultValue = defaultValue
            )
            if (!parameters.add(param)) throw RuntimeException("A parameter with the name \"${param.id}\" has already exists") // TODO: exception type
            return param
        }
    }

    inner class Param<out T>(
        id: String,
        displayName: String,
        type: ParameterType<*>,
        vararg: Boolean,
        optional: Boolean,
        suggestions: () -> Set<String>,
        description: String?,
        flags: Set<Char>,
        defaultValue: DefaultValue?,
    ) : CommandParameter(
        id = id,
        displayName = displayName,
        type = type,
        suggestions = suggestions,
        description = description,
        vararg = vararg,
        optional = optional,
        flags = flags,
        defaultValue = defaultValue
    ), CommandDelegate<T> {

        override operator fun getValue(thisRef: PropertyCommand<*>, property: KProperty<*>): T = getArg()

        @Suppress("UNCHECKED_CAST")
        fun getArg() = _args[this] as T
    }

    private class SenderProperty<out T : CommandSender> : CommandProperty<T>, CommandDelegate<T> {

        override operator fun provideDelegate(thisRef: PropertyCommand<*>, property: KProperty<*>): CommandDelegate<T> {
            return this
        }

        override operator fun getValue(thisRef: PropertyCommand<*>, property: KProperty<*>): T {
            val value: CommandSender = thisRef._commandSender

            // TODO: If value is subtype of T, return value as T.
            //       Else, return null if optional; throw command exception if required.

            @Suppress("UNCHECKED_CAST")
            return value as T
        }
    }
}

