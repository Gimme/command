package dev.gimme.command.node

import dev.gimme.command.Command

/**
 * Represents a command node that can be used to create a hierarchical command structure.
 *
 * @property name                the command name (cannot contain spaces)
 * @property description         a description of what this command does
 * @property detailedDescription an optional more detailed description
 * @property aliases             aliases for the name
 * @property parent              the optional parent node
 * @property subcommands         the subcommands to this command
 * @property id                  the id of this command (unique among commands with different paths)
 * @property root                the root command in the parent-chain
 * @property isRoot              if this is a root command (no parent)
 * @property path                the full [name]-path to this command
 * @property pathAliases         all full paths to this command including [aliases]
 */
interface CommandNode {

    val name: String
    var description: String
    var detailedDescription: String?
    var aliases: Set<String>

    val parent: CommandNode?
    val subcommands: MutableMap<String, CommandNode>

    val id: String get() = path("/")
    val root: CommandNode get() = parent?.root ?: this
    val isRoot: Boolean get() = parent == null
    val path: List<String> get() = (parent?.path ?: listOf()) + name

    val pathAliases: List<List<String>>
        get() {
            return parent?.let { parent ->
                parent.pathAliases.flatMap { parentPath ->
                    (aliases + name).map { alias ->
                        parentPath + alias
                    }
                }
            } ?: (aliases + name).map { listOf(it) }
        }


    /**
     * Returns the full [name]-path to this command including all [parent]s separated by the [separator].
     */
    fun path(separator: String = "/"): String = path.joinToString(separator)

    /**
     * Returns this node as a command if possible.
     */
    fun asCommand(): Command<*>? = this as? Command<*>

    /**
     * Returns if this node is a command.
     */
    fun isCommand(): Boolean = this.asCommand() != null

    /**
     * Connects all parent nodes in the hierarchy so that the [subcommands] fields tell the same story as the [parent]
     * fields.
     */
    fun connectParents() {
        val parent = this.parent ?: return
        parent.subcommands[this.name] = this
        parent.connectParents()
    }

    /**
     * Returns all real [Command]s under this node in the hierarchy (including itself).
     */
    val leafCommands: Set<Command<*>> get() {
        val result = this.subcommands.values.flatMap(CommandNode::leafCommands).toMutableSet()
        this.asCommand()?.let { result.add(it) }
        return result
    }
}

