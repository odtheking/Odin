package me.odinclient.features.qol

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object GhostPick {
    private var item = ItemStack(Item.getItemById(278), 1)
    init {
        item.addEnchantment(Enchantment.getEnchantmentById(32), 10)
        item.tagCompound?.setBoolean("Unbreakable", true)
    }
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!config.ghostPickKeybind.isActive || mc.thePlayer == null || mc.currentScreen != null) return
        mc.thePlayer?.inventory?.mainInventory?.set(config.ghostPickSlot.toInt() - 1, item)
    }
}