package dev.gimme.gimmeapi.discord

import dev.gimme.gimmeapi.command.channel.CommandChannel
import dev.gimme.gimmeapi.core.data.requireResource
import dev.gimme.gimmeapi.core.data.yaml.loadYamlFromResource
import dev.gimme.gimmeapi.discord.command.ChannelCommandChannel
import dev.gimme.gimmeapi.discord.config.DiscordConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import javax.security.auth.login.LoginException

/**
 * A plugin that connects the bot to Discord.
 */
open class DiscordPlugin /*: TextPlatformPlugin()*/ {

    private val discordResourcePath = "discord.yml"

    /** The Discord API interface. */
    protected lateinit var jda: JDA
        private set

    /** The Discord-related config. */
    protected lateinit var config: DiscordConfig
        private set

    @Throws(LoginException::class)
    /*override*/ fun initCommandMedium(): CommandChannel<String?> {
        config =
            requireResource(loadYamlFromResource(discordResourcePath, DiscordConfig::class.java), discordResourcePath)

        jda = JDABuilder.createDefault(config.token).build()

        return ChannelCommandChannel(jda, config.prefix)
    }

    /*override*/ fun onDisable() {
        //super.onDisable()

        jda.shutdownNow()
    }
}
