package com.github.gimme.gimmebot.boot.command.property

import kotlin.reflect.KProperty

interface Property<out T, in S> {

    operator fun provideDelegate(thisRef: S, property: KProperty<*>): Delegate<T, S>
}

interface Delegate<out T, in S> {

    operator fun getValue(thisRef: S, property: KProperty<*>): T
}


interface CommandProperty<out T> : Property<T, PropertyCommand<*>>
interface CommandDelegate<out T> : Delegate<T, PropertyCommand<*>>
