# Spigot Guide

1. Extend your plugin's main class from `SpigotGimmePlugin` instead of the usual `JavaPlugin`
2. Create another class extending `SpigotGimmeBot`
3. Override the `createBot` method in the plugin class and make it return a new instance of the bot class
4. Leave the plugin class as it is and use the bot class for everything else
