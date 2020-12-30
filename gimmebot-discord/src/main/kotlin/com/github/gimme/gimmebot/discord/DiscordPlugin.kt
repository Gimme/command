package com.github.gimme.gimmebot.discord

import com.github.gimme.gimmebot.core.command.medium.CommandMedium
import com.github.gimme.gimmebot.core.data.requireResource
import com.github.gimme.gimmebot.core.data.yaml.loadYamlFromResource
import com.github.gimme.gimmebot.core.plugin.TextPlatformPlugin
import com.github.gimme.gimmebot.discord.command.mediums.ChannelCommandMedium
import com.github.gimme.gimmebot.discord.config.DiscordConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import javax.security.auth.login.LoginException

/**
 * A plugin that connects the bot to Discord.
 */
open class DiscordPlugin : TextPlatformPlugin() {

    private val discordResourcePath = "discord.yml"

    /** The Discord API interface. */
    protected lateinit var jda: JDA
        private set

    /** The Discord-related config. */
    protected lateinit var config: DiscordConfig
        private set

    @Throws(LoginException::class)
    override fun initCommandMedium(): CommandMedium<String?> {
        config =
            requireResource(loadYamlFromResource(discordResourcePath, DiscordConfig::class.java), discordResourcePath)

        jda = JDABuilder.createDefault(config.token).build()

        return ChannelCommandMedium(jda, config.prefix)
    }

    override fun onDisable() {
        jda.shutdownNow()
    }
}
