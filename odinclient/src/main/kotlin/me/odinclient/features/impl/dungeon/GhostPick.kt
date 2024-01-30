package me.odinclient.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.runIn
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object GhostPick : Module(
    "Ghost Pickaxe",
    description = "Gives you a ghost pickaxe in your selected slot when you press the keybind.",
    category = Category.DUNGEON,
) {
    private val slot: Int by NumberSetting("Ghost pick slot", 1, 1.0, 9.0, 1.0)
    private val level: Int by NumberSetting("Efficiency level", 10, 1.0, 100.0, 1.0)
    private val delay: Int by NumberSetting("Delay to Create (ms)", 0, 0, 1000)

    override fun onKeybind() {
        if (mc.thePlayer == null || mc.currentScreen != null) return
        runIn(delay / 50) {
            val item = ItemStack(Item.getItemById(278), 1).apply {
                addEnchantment(Enchantment.getEnchantmentById(32), level)
                tagCompound?.setBoolean("Unbreakable", true)
            }
            if (enabled) mc.thePlayer?.inventory?.mainInventory?.set(slot - 1, item)
        }
    }
}