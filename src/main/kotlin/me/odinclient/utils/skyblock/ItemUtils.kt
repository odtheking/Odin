
package me.odinclient.utils.skyblock

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.Utils.noControlCodes
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import java.awt.Color

object ItemUtils {

    /**
     * Returns the ExtraAttribute Compound
     */
    private val ItemStack.extraAttributes: NBTTagCompound?
        get() {
            return this.getSubCompound("ExtraAttributes", false)
        }

    /**
     * Returns displayName without control codes.
     */
    private val ItemStack.unformattedName: String
        get() = this.displayName.noControlCodes

    /**
     * Returns Item ID for an Item
     */
    val ItemStack.itemID: String
        get() = this.extraAttributes?.getString("id") ?: ""

    inline val heldItem: ItemStack?
        get() = mc.thePlayer?.heldItem

    val ItemStack.enchants: Map<String, Int>?
        get() = this.extraAttributes?.let { extraAttributes ->
            if (!extraAttributes.hasKey("enchantments", 10)) return null
            val enchantments = extraAttributes.getCompoundTag("enchantments")
            enchantments.keySet.associateWith { enchantments.getInteger(it) }
        }
    /**
     * Returns the lore for an Item
     */
    val ItemStack.lore: List<String>
        get() = this.tagCompound?.getCompoundTag("display")?.getTagList("Lore", 8)?.let {
            val list = mutableListOf<String>()
            for (i in 0 until it.tagCount()) {
                list.add(it.getStringTagAt(i))
            }
            list
        } ?: emptyList()


    /**
     * Returns first slot of an Item
     */
    fun getItemSlot(item: String, ignoreCase: Boolean = true): Int? {
        val index = mc.thePlayer.inventory.mainInventory.indexOfFirst {
            it?.unformattedName?.contains(item, ignoreCase) == true
        }
        return index.takeIf { it != -1 }
    }

    /**
     * Gets index of an item in a chest.
     * @return null if not found.
     * @return null
     */
    fun getItemIndexInContainerChest(container: ContainerChest, item: String, ignoreCase: Boolean): Int? {
        return container.inventorySlots.subList(0, container.inventory.size - 36).firstOrNull {
            it.stack?.unformattedName?.contains(item, ignoreCase) == true
        }?.slotNumber
    }

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

    private val rarityRegex: Regex = Regex("§l(?<rarity>[A-Z]+) ?(?<type>[A-Z ]+)?(?:§[0-9a-f]§l§ka)?$")

    fun getRarity(lore: List<String>): ItemRarity? {
        // Start from the end since the rarity is usually the last line or one of the last.
        for (i in lore.indices.reversed()) {
            val currentLine = lore[i]
            val match = rarityRegex.find(currentLine) ?: continue
            val rarity: String = match.groups["rarity"]?.value ?: continue
            for (itemRarity in ItemRarity.values()) {
                if (currentLine.noControlCodes.startsWith(itemRarity.loreName)) {
                    return itemRarity
                }
            }
        }
        return null
    }
}