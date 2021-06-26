package dev.gimme.gimmeapi.command.property

import dev.gimme.gimmeapi.command.BaseCommand
import kotlin.reflect.KProperty

fun interface Property<out T, in S> {

    @JvmSynthetic
    operator fun provideDelegate(thisRef: S, property: KProperty<*>): Delegate<T, S>
}

fun interface Delegate<out T, in S> {

    @JvmSynthetic
    operator fun getValue(thisRef: S, property: KProperty<*>): T
}


fun interface CommandProperty<out T> : Property<T, BaseCommand<*>>
fun interface CommandDelegate<out T> : Delegate<T, BaseCommand<*>>
