package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * A collection of commands mapped by name.
 */
class CommandMap : CommandCollection {

    private val map = mutableMapOf<String, Command<*>>()

    override val commands: Set<Command<*>>
        get() = map.values.toSet()

    override fun addCommand(command: Command<*>) {
        map[command.name] = command

        command.aliases.forEach {
            map.putIfAbsent(it, command)
        }
    }

    override fun getCommand(name: String): Command<*>? = map[name]

    override fun containsCommand(name: String): Boolean = map.containsKey(name)

    override fun iterator(): Iterator<Command<*>> = map.values.iterator()
}
