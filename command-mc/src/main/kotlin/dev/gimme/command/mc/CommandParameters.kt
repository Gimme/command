package dev.gimme.command.mc

import dev.gimme.command.parameter.ParameterTypes.register
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player

@Suppress("RemoveExplicitTypeArguments")
internal fun registerBaseParameterTypes() {
    register<Player>(values = { Bukkit.getOnlinePlayers().map { it.name } }) { Bukkit.getPlayer(it) }
    register<OfflinePlayer>(values = { Bukkit.getOfflinePlayers().map { it.name.toString() } }) { input ->
        Bukkit.getOfflinePlayers().find { it.name == input }
    }

    register<World>(values = { Bukkit.getWorlds().map { it.name }}) { Bukkit.getWorld(it) }

    val materials = Material.values().map { it.name }
    register<Material>(values = { materials }) { Material.getMaterial(it) }
}
