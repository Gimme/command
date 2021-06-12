package dev.gimme.gimmeapi.boot.command.property

import dev.gimme.gimmeapi.command.BaseCommand
import dev.gimme.gimmeapi.command.ParameterTypes
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.parameter.CommandParameterSet
import dev.gimme.gimmeapi.command.parameter.DefaultValue
import dev.gimme.gimmeapi.command.sender.CommandSender
import dev.gimme.gimmeapi.core.common.splitCamelCase
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

/**
 * TODO
 *
 * @param R the type of the result of the command
 */
abstract class PropertyCommand<out R>(name: String) : BaseCommand<R>(name) {

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

    fun <T: CommandSender> sender(): CommandProperty<T> = SenderProperty()
    fun <T> param(): ParamBuilder<T> = ParamBuilder()

    inner class ParamBuilder<out T> : CommandProperty<T> {

        private var name: String? = null
        private var returnType: KType? = null

        override operator fun provideDelegate(thisRef: PropertyCommand<*>, property: KProperty<*>): Param<T> {
            this.name = property.name
            this.returnType = property.returnType

            return build()
        }

        fun setName(name: String): ParamBuilder<T> {
            this.name = name
            return this
        }

        fun setType(type: KType): ParamBuilder<T> {
            this.returnType = type
            return this
        }

        fun build(): Param<T> {
            val name = name!! // TODO: Error message on null
            val returnType = returnType!! // TODO: Error message on null

            val id = name.splitCamelCase("-")
            val displayName = name.splitCamelCase(" ")
            val commandParameterType = ParameterTypes.get(returnType)
            val flags = setOf<Char>() // TODO
            val defaultValue: DefaultValue? = null // TODO

            val param = Param<T>(
                id = id,
                displayName = displayName,
                type = returnType,
                suggestions = commandParameterType.values ?: { setOf() },
                vararg = returnType.isSubtypeOf(ITERABLE_TYPE), // TODO
                optional = returnType.isMarkedNullable,
                flags = flags,
                defaultValue = defaultValue
            )
            parameters.add(param)
            return param
        }

        private val ITERABLE_TYPE = Iterable::class.createType(listOf(KTypeProjection.STAR))
    }

    inner class Param<out T>(
        id: String,
        displayName: String,
        type: KType,
        suggestions: () -> Set<String> = { setOf() },
        description: String? = null,
        vararg: Boolean = false,
        optional: Boolean = false,
        flags: Set<Char> = setOf(),
        defaultValue: DefaultValue? = null,
    ) : CommandParameter(
        id, displayName, type, suggestions, description, vararg, optional, flags, defaultValue
    ), CommandDelegate<T> {

        override operator fun getValue(thisRef: PropertyCommand<*>, property: KProperty<*>): T = getValue()

        @Suppress("UNCHECKED_CAST")
        fun getValue() = _args[this] as T
    }

    private class SenderProperty<out T : CommandSender>: CommandProperty<T>, CommandDelegate<T> {

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

