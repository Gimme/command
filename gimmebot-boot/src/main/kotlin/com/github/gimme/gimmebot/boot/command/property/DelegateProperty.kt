package com.github.gimme.gimmebot.boot.command.property

import kotlin.reflect.KProperty

fun interface Property<out T, in S> {

    operator fun provideDelegate(thisRef: S, property: KProperty<*>): Delegate<T, S>
}

fun interface Delegate<out T, in S> {

    operator fun getValue(thisRef: S, property: KProperty<*>): T
}


fun interface CommandProperty<out T> : Property<T, PropertyCommand<*>>
fun interface CommandDelegate<out T> : Delegate<T, PropertyCommand<*>>
