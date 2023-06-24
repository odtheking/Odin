package me.odinclient.utils.skyblock

import cc.polyfrost.oneconfig.libs.universal.ChatColor.Companion.stripControlCodes
import me.odinclient.OdinClient.Companion.mc
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraft.util.StringUtils
import java.awt.Color

object ItemUtils {

    fun getItemSlot(item: String, ignoreCase: Boolean = true): Int =
        List(35) { mc.thePlayer.inventory.getStackInSlot(it) }
            .indexOfFirst { i -> i?.displayName?.contains(item, ignoreCase) == true }

    val ItemStack.itemID: String
        get() {
            if (this.hasTagCompound() && this.tagCompound.hasKey("ExtraAttributes")) {
                val attributes = this.getSubCompound("ExtraAttributes", false)
                if (attributes.hasKey("id", 8)) {
                    return attributes.getString("id")
                }
            }
            return ""
        }


    fun getItemIndexInContainerChest(item: String, container: ContainerChest, contains: Boolean): Int {
        for (i in 0 until container.inventory.size - 36) {
            val itemStack: ItemStack = container.inventory[i] ?: continue
            return if (
                if (contains)
                    StringUtils.stripControlCodes(itemStack.displayName).contains(item)
                else
                    StringUtils.stripControlCodes(itemStack.displayName) == item
            )  i
            else continue
        }
        return -1
    }

    fun getItemIndexInInventory(item: String, contains: Boolean): Int {
        val inventory = mc.thePlayer.inventory.mainInventory
        for (i in inventory.indices) {
            val itemStack: ItemStack = inventory[i] ?: continue
            return if (
                if (contains)
                    StringUtils.stripControlCodes(itemStack.displayName).contains(item)
                else
                    StringUtils.stripControlCodes(itemStack.displayName) == item
            )  i
            else continue
        }
        return -1
    }

    val ItemStack.lore: List<String>
        get() = this.tagCompound?.getCompoundTag("display")?.getTagList("Lore", 8)?.let {
            val list = mutableListOf<String>()
            for (i in 0 until it.tagCount()) {
                list.add(it.getStringTagAt(i))
            }
            list
        } ?: emptyList()


    enum class ItemRarity(
        val loreName: String,
        val colorCode: String,
        val color: Color
    ) {
        COMMON("COMMON", "§f", Color.WHITE),
        UNCOMMON("UNCOMMON", "§2", Color.GREEN),
        RARE("RARE", "§9", Color.BLUE),
        EPIC("EPIC", "§5", Color.MAGENTA),
        LEGENDARY("LEGENDARY", "§6", Color.ORANGE),
        MYTHIC("MYTHIC", "§d", Color.PINK),
        DIVINE("DIVINE", "§b", Color.CYAN),
        SPECIAL("SPECIAL", "§c", Color.RED),
        VERY_SPECIAL("VERY SPECIAL", "§c", Color.RED);
    }

    private val rarityRegex: Regex =
        Regex("§l(?<rarity>[A-Z]+) ?(?<type>[A-Z ]+)?(?:§[0-9a-f]§l§ka)?$")
    /**
     * Returns the rarity of a Skyblock item given its lore. This method takes the item's lore as a string list as input.
     * This method is split up from the method that takes the `ItemStack` instance for easier unit testing.
     *
     * @param lore the `List<String>` containing the item's lore
     * @return the rarity of the item if a valid rarity is found, or `null` if item is `null` or no valid rarity is found
     */
     fun getRarity(lore: List<String>): ItemRarity? {
        // Start from the end since the rarity is usually the last line or one of the last.
        for (i in lore.indices.reversed()) {
            val currentLine = lore[i]
            val match = rarityRegex.find(currentLine) ?: continue
            val rarity: String = match.groups["rarity"]?.value ?: continue
            for (itemRarity in ItemRarity.values()) {
                if (stripControlCodes(currentLine)?.startsWith(itemRarity.loreName) == true) {
                    return itemRarity
                }
            }
        }
        return null
    }
}