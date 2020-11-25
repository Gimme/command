package com.github.gimme.gimmebot.core.command

/**
 * Displays a list of available commands.
 */
class HelpCommand(
    private val commandManager: CommandManager,
) : BaseCommand("help") {

    override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
        val sb = StringBuilder("Commands:")

        commandManager.getCommands().forEach { command ->
            sb.append("\n")
            sb.append("  ")
            sb.append(command.name)
        }

        println(sb.toString())
        return CommandResponse(sb.toString())
    }
}
