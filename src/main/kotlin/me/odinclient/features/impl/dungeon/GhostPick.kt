package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.NumberSetting
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object GhostPick : Module(
    "Ghost Pickaxe",
    description = "Gives you a ghost pickaxe in your selected slot when you press the keybind.",
    category = Category.DUNGEON
) {
    private val slot: Int by NumberSetting("Ghost pick slot", 1, 1.0, 9.0, 1.0)

    private val item = ItemStack(Item.getItemById(278), 1).apply {
        addEnchantment(Enchantment.getEnchantmentById(32), 10)
        tagCompound?.setBoolean("Unbreakable", true)
    }

    override fun onKeybind() {
        if (mc.thePlayer == null || mc.currentScreen != null) return
        if (enabled) mc.thePlayer?.inventory?.mainInventory?.set(slot - 1, item)
    }
}