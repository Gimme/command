package dev.gimme.gimmeapi.mc.item

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * Represents a custom item that can be used to create item stacks.
 *
 * @param id          a unique name for this item
 * @param displayName the display name of this item
 * @param type        the item type of the generated item stacks
 * @param info        the rows of item info displayed in game
 * @param glowing     if the item icon should be glowing
 */
abstract class CustomItem private constructor(
    private val id: String,
    private val displayName: String,
    private val type: Material,
    private var info: List<String> = listOf(),
    private var glowing: Boolean = true,
) {

    @Suppress("DEPRECATION")
    private val ID_KEY: NamespacedKey = NamespacedKey("gimme-api", "custom_item_id")
    private val ID_DATA_TYPE = PersistentDataType.STRING

    constructor(
        name: String,
        type: Material,
        info: List<String>,
        glowing: Boolean,
    ) : this(
        ChatColor.stripColor(name)!!
            .lowercase()
            .replace(" ", "_")
            .replace("[^a-z0-9_]".toRegex(), ""),
        name,
        type,
        info,
        glowing
    )

    /**
     * Does something with the newly created item stack.
     *
     * This can be implemented to do extra changes on the item stack before finishing the creation of it.
     *
     * [ItemStack.setItemMeta] will be called automatically after this method returns and does not need
     * to be explicitly called.
     */
    protected abstract fun onCreate(itemStack: ItemStack, itemMeta: ItemMeta)

    /**
     * Creates an [ItemStack] from this custom item with the specified stack size [amount].
     */
    fun createItemStack(amount: Int = 1): ItemStack {
        val itemStack = ItemStack(type, amount)

        updateLore(itemStack)

        val itemMeta = itemStack.itemMeta!!

        itemMeta.setDisplayName(displayName)

        itemMeta.setGlowing(glowing)

        val dataContainer: PersistentDataContainer = itemMeta.persistentDataContainer
        dataContainer.set(ID_KEY, ID_DATA_TYPE, id)

        itemStack.itemMeta = itemMeta
        onCreate(itemStack, itemMeta)
        itemStack.itemMeta = itemMeta

        return itemStack
    }

    /**
     * Updates the lore of the [itemStack] with the [header], item [info] and [footer].
     */
    protected fun updateLore(itemStack: ItemStack, header: List<String> = listOf(), footer: List<String> = listOf()) {
        val itemMeta: ItemMeta = itemStack.itemMeta!!

        val lore = mutableListOf<String>()
        lore.addAll(header)
        lore.addAll(info)
        lore.addAll(footer)

        itemMeta.lore = lore
        itemStack.itemMeta = itemMeta
    }

    /**
     * Sets this item meta to be [glowing] (enchanted) or not.
     */
    protected open fun ItemMeta.setGlowing(glowing: Boolean) {
        if (glowing) {
            this.addEnchant(Enchantment.LUCK, 1, true)
            this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        } else {
            this.removeEnchant(Enchantment.LUCK)
            this.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
    }
}
