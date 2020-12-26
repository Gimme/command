package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * Represents a tree structure of commands.
 */
class CommandTree : CommandCollection {

    private val delimiter = "."
    private val root: Node = Node("", null)

    override fun <T> addCommand(command: Command<T>, responseParser: (T) -> Any?) {
        val words = command.name.split(delimiter)

        var currentNode = root
        for (word in words) {
            currentNode = currentNode.children.computeIfAbsent(word) { k -> Node(k, null) }
        }
        currentNode.data = Data(command, responseParser)
    }

    override fun getCommand(name: String): Command<*>? {
        var currentNode = root

        for (word in name.split(delimiter)) {
            currentNode = currentNode.children[word] ?: return null
        }

        return currentNode.data?.command
    }

    override fun getCommands(): List<Command<*>> {
        val list = mutableListOf<Command<*>>()
        root.fetchCommands(list)
        return list
    }

    override fun findCommand(path: List<String>): Command<*>? {
        var lastFoundCommand: Command<*>? = null

        var currentNode = root
        for (word in path) {
            currentNode = currentNode.children[word] ?: break
            lastFoundCommand = currentNode.data?.command
        }
        return lastFoundCommand
    }

    private data class Node(
        val name: String,
        var data: Data<*>?,
    ) {
        val children: MutableMap<String, Node> = mutableMapOf()

        fun fetchCommands(list: MutableList<Command<*>>) {
            data?.let { list.add(it.command) }
            children.values.forEach { child -> child.fetchCommands(list) }
        }
    }

    private data class Data<T>(
        val command: Command<T>,
        val responseParser: (T) -> Any?
    )
}
