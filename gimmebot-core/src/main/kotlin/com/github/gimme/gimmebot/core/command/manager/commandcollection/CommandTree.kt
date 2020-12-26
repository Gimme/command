package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * Represents a tree structure of commands.
 *
 * @param R the response type
 */
class CommandTree<R> : CommandCollection<R> {

    private val delimiter = "."
    private val root: Node<R> = Node("", null)

    override fun <T> addCommand(command: Command<T>, responseParser: ((T) -> R)?) {
        val words = command.name.split(delimiter)

        var currentNode = root
        for (word in words) {
            currentNode = currentNode.children.computeIfAbsent(word) { k -> Node(k, null) }
        }
        currentNode.commandNode = CommandCollection.CommandNode(command, responseParser)
    }

    override fun getCommand(name: String): CommandCollection.CommandNode<*, R>? {
        var currentNode = root

        for (word in name.split(delimiter)) {
            currentNode = currentNode.children[word] ?: return null
        }

        return currentNode.commandNode
    }

    override fun getCommands(): List<Command<*>> {
        val list = mutableListOf<Command<*>>()
        root.fetchCommands(list)
        return list
    }

    override fun findCommand(path: List<String>): CommandCollection.CommandNode<*, R>? {
        var lastFound: CommandCollection.CommandNode<*, R>? = null

        var currentNode = root
        for (word in path) {
            currentNode = currentNode.children[word] ?: break
            lastFound = currentNode.commandNode
        }
        return lastFound
    }

    private data class Node<R>(
        val name: String,
        var commandNode: CommandCollection.CommandNode<*, R>?,
    ) {
        val children: MutableMap<String, Node<R>> = mutableMapOf()

        fun fetchCommands(list: MutableList<Command<*>>) {
            commandNode?.let { list.add(it.command) }
            children.values.forEach { child -> child.fetchCommands(list) }
        }
    }
}
