package dev.gimme.command.discord

import dev.gimme.command.Command
import dev.gimme.command.manager.commandcollection.CommandMap
import dev.gimme.command.node.CommandNode
import dev.gimme.command.parameter.ParameterType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.commands.Command as JDACommand
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction

private fun <T> JDA.registerCommand(
    command: Command<T>,
    converter: (T) -> String? = { it?.toString() }
) {
    this.addEventListener(object : ListenerAdapter() {
        override fun onSlashCommand(event: SlashCommandEvent) {
            if (!command.pathAliases.contains(event.commandPath.split("/"))) return

            val response = command.execute(
                event.interaction.user.gimme,
                event.options.associate {
                    val parameter = command.parameters[it.name]!!
                    Pair(parameter, parameter.type.parse(it.asString))
                })

            converter(response)?.let { event.reply(it) }
        }
    })
}

private fun JDA.createCommands(
    vararg commands: Command<*>,
    guild: Guild? = null,
    exhaustive: Boolean = true,
) = this.createCommands(commands = commands.toSet(), guild = guild, exhaustive = exhaustive)

// TODO: simplify (reuse code)
private fun JDA.createCommands(
    commands: Set<Command<*>>,
    guild: Guild? = null,
    exhaustive: Boolean = true, // TODO
) {
    val commandMap = CommandMap()
    commandMap.addCommands(commands)

    commandMap.root.values.forEach { node ->
        val commandNode = node.commandNode!! // TODO: !!

        val commandCreateActions: List<CommandCreateAction> = (commandNode as? Command<*>)?.let { command ->
            registerCommand(command)
            command.aliases.map { alias ->
                guild?.upsertCommand(command.toCommandData())?.setName(alias)
                    ?: this.upsertCommand(command.toCommandData()).setName(alias)
            }
        } ?: commandNode.aliases.map { alias ->
            guild?.upsertCommand(alias, commandNode.description)
                ?: this.upsertCommand(alias, commandNode.description)
        }

        commandCreateActions.forEach { commandCreateAction ->
            node.values.forEach { subNode ->
                val subCommandNode = subNode.commandNode!!

                (subCommandNode as? Command<*>)?.also { command ->
                    registerCommand(command)
                    command.aliases.map { alias ->
                        commandCreateAction.addSubcommands(command.toSubCommandData()).setName(alias)
                    }
                } ?: run {
                    subCommandNode.aliases.map { alias ->
                        val subCommandGroupData = subCommandNode.toSubCommandGroupData().setName(alias)

                        subNode.values.forEach { subSubNode ->
                            val subSubCommandNode = subSubNode.commandNode!!

                            (subSubCommandNode as? Command<*>)?.also { command ->
                                registerCommand(command)
                                command.aliases.map { alias ->
                                    subCommandGroupData.addSubcommands(command.toSubCommandData()).setName(alias)
                                }
                            }
                                ?: throw IllegalArgumentException("Too many nested command layers: ${subSubCommandNode.path()}")
                        }

                        commandCreateAction.addSubcommandGroups(subCommandGroupData)
                    }
                }
            }
        }

        commandCreateActions.forEach(CommandCreateAction::queue)
    }
}

private fun Command<*>.toCommandData(): CommandData = CommandData(this.name, this.description)
    .addOptions(
        this.parameters.map { parameter ->
            OptionData(
                parameter.type.toOptionType(),
                parameter.id,
                parameter.description ?: "",
                !parameter.optional
            ).addChoices(parameter.type.values?.invoke()?.map { JDACommand.Choice(it, it) } ?: emptyList())
        }
    )

private fun Command<*>.toSubCommandData(): SubcommandData = this.toCommandData().toSubcommandData()

private fun CommandData.toSubcommandData(): SubcommandData = SubcommandData(this.name, this.description)
    .addOptions(this.options)

private fun CommandNode.toSubCommandGroupData(): SubcommandGroupData = SubcommandGroupData(this.name, this.description)

@Throws(RuntimeException::class) // TODO: exception type
private fun ParameterType<*>.toOptionType(): OptionType {
    when (this.clazz) {
        String::class.java -> OptionType.STRING
        Int::class.java -> OptionType.INTEGER
        Boolean::class.java -> OptionType.BOOLEAN
        Double::class.java -> OptionType.fromKey(10) // NUMBER
        java.lang.Integer::class.java -> OptionType.INTEGER
        java.lang.Boolean::class.java -> OptionType.BOOLEAN
        else -> null
    }?.let { return it }

    if (Number::class.java.isAssignableFrom(this.clazz)) return OptionType.fromKey(10) // NUMBER

    return OptionType.STRING // DEFAULT
}
