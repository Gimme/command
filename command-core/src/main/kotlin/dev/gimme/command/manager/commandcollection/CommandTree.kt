package dev.gimme.command.manager.commandcollection

import dev.gimme.command.Command
import dev.gimme.command.CommandSearchResult
import dev.gimme.command.node.CommandNode

/**
 * A collection of commands implemented by the natural tree structure of the nodes.
 */
class CommandTree : CommandCollection {

    private val rootNodes: MutableMap<String, CommandNode> = mutableMapOf()

    override val commands: Set<Command<*>> get() = rootNodes.values.flatMap(CommandNode::leafCommands).toSet()

    override fun add(command: Command<*>) {
        command.connectParents()

        val root = command.root
        rootNodes[root.name] = root
    }

    override fun get(path: List<String>): Command<*>? = getNode(path)?.asCommand()

    override fun find(path: List<String>): CommandSearchResult {
        var node: CommandNode? = null

        val currentPath = mutableListOf<String>()

        var longestPathToCommand: List<String>? = null
        var commandNode: CommandNode? = null
        var command: Command<*>? = null

        for (s in path) {
            node = (if (node == null) rootNodes[s] else node.subcommands[s]) ?: break
            currentPath.add(s)

            longestPathToCommand = currentPath
            commandNode = node
            command = node.asCommand()
        }

        return CommandSearchResult(
            path = longestPathToCommand,
            command = command,
            commandNode = commandNode,
            subBranches = node?.subcommands?.keys ?: emptySet(),
        )
    }

    private fun getNode(path: List<String>): CommandNode? {
        if (path.isEmpty()) return null

        var node = rootNodes[path[0]] ?: return null

        path.drop(1).forEach {
            node = node.subcommands[it] ?: return null
        }

        return node
    }

    override fun isEmpty(): Boolean = rootNodes.isEmpty()
}
