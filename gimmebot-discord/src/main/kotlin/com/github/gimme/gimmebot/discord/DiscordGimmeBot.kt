package com.github.gimme.gimmebot.discord

import com.github.gimme.gimmebot.core.GimmeBot
import com.github.gimme.gimmebot.core.data.yaml.loadYamlFromResource
import com.github.gimme.gimmebot.core.data.requireResource
import com.github.gimme.gimmebot.discord.config.DiscordConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import javax.security.auth.login.LoginException

/**
 * Represents a bot using the Discord platform.
 */
abstract class DiscordGimmeBot : GimmeBot() {

    private val discordResourcePath = "discord.yml"

    /** The Discord API interface. */
    lateinit var jda: JDA

    override fun connect(): Boolean {
        val discordConfig =
            requireResource(loadYamlFromResource(discordResourcePath, DiscordConfig::class.java), discordResourcePath)
        val token = discordConfig.token

        jda = try {
            JDABuilder.createDefault(token).build()
        } catch (e: LoginException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    override fun disconnect() {
        //TODO
    }
}
