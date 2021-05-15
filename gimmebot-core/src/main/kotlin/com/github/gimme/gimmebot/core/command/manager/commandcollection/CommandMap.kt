package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * A collection of commands mapped by name.
 */
class CommandMap : CommandCollection {

    private val root = Node()

    override val commands: MutableSet<Command<*>> = mutableSetOf()

    override fun addCommand(command: Command<*>) {
        commands.add(command)
        addCommandWithAliases(command)
    }

    private fun addCommandWithAliases(command: Command<*>): List<Node> {
        val parentNodes: List<Node> = command.parent?.let { parent ->
            addCommandWithAliases(parent)
        } ?: listOf(root)

        val list: MutableList<Node> = mutableListOf()

        parentNodes.forEach { node ->
            list.add(node.addCommand(command.name, command, true))

            command.aliases.forEach { name ->
                list.add(node.addCommand(name, command, false))
            }
        }

        return list
    }

    private fun Node.addCommand(name: String, command: Command<*>, overwrite: Boolean = true): Node {
        return if (overwrite) {
            val node = Node(command)
            this[name] = node
            node
        } else computeIfAbsent(name) { Node(command) }
    }

    override fun getCommand(path: List<String>): Command<*>? {
        var node = root
        path.forEach { node = node[it] ?: return null }

        return node.command
    }

    override fun containsCommand(path: List<String>): Boolean = getCommand(path) != null

    override fun iterator(): Iterator<Command<*>> = commands.iterator()

    private class Node(val command: Command<*>? = null): LinkedHashMap<String, Node>()
}
