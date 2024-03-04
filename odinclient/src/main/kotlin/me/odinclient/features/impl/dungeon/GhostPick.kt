package me.odinclient.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.features.settings.impl.Keybinding
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.runIn
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import org.lwjgl.input.Keyboard

object GhostPick : Module(
    "Ghost Pickaxe",
    description = "Gives you a ghost pickaxe in your selected slot when you press the keybind.",
    category = Category.DUNGEON,
) {
    private val slot: Int by NumberSetting("Ghost pick slot", 1, 1.0, 9.0, 1.0)
    private val level: Int by NumberSetting("Efficiency level", 10, 1.0, 100.0, 1.0)
    private val delay: Int by NumberSetting("Delay to Create (ms)", 0, 0, 1000)
    private val axeKeybind: Keybinding by KeybindSetting("Axe Keybind", Keyboard.KEY_NONE, description = "Keybind to create an axe.").onPress {
        giveItem(279)
    }

    override fun onKeybind() {
        giveItem(278)
    }

    private fun giveItem(id: Int) {
        if (mc.thePlayer == null || mc.currentScreen != null) return
        runIn(delay / 50) {
            val item = ItemStack(Item.getItemById(id), 1).apply {
                addEnchantment(Enchantment.getEnchantmentById(32), level)
                tagCompound?.setBoolean("Unbreakable", true)
            }
            if (enabled) mc.thePlayer?.inventory?.mainInventory?.set(slot - 1, item)
        }
    }
}