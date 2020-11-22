package com.github.gimme.gimmebot.discord

//TODO: remove
fun main() {
    val bot = object : DiscordGimmeBot() {
        override fun onStart() {}
    }

    bot.start()
}
