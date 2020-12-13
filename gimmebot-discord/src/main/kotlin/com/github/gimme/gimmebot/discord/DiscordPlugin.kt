package com.github.gimme.gimmebot.discord

import com.github.gimme.gimmebot.core.data.requireResource
import com.github.gimme.gimmebot.core.data.yaml.loadYamlFromResource
import com.github.gimme.gimmebot.core.plugin.GimmeBotPlugin
import com.github.gimme.gimmebot.discord.command.ChannelCommandInputMedium
import com.github.gimme.gimmebot.discord.config.DiscordConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import javax.security.auth.login.LoginException

/**
 * Represents a plugin that connects to a Discord bot.
 */
open class DiscordPlugin : GimmeBotPlugin() {

    private val discordResourcePath = "discord.yml"

    /** The Discord API interface. */
    protected lateinit var jda: JDA
        private set

    override fun onEnable() {
        val config =
            requireResource(loadYamlFromResource(discordResourcePath, DiscordConfig::class.java), discordResourcePath)

        try {
            jda = JDABuilder.createDefault(config.token).build()
        } catch (e: LoginException) {
            e.printStackTrace()
            return
        }

        bot.commandManager.install(ChannelCommandInputMedium(jda, config.prefix))
    }

    override fun onDisable() {
        jda.shutdownNow()
    }
}
