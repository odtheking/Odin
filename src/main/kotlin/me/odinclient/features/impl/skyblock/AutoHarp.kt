package me.odinclient.features.impl.skyblock

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoHarp : Module(
    "Auto Melody",
    category = Category.SKYBLOCK
) {

    private var lastInv = 0
    private var melodyOpen = false

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui !is GuiChest) return
        melodyOpen = false
        val openChestName = Utils.getGuiName(event.gui);
        if (openChestName.startsWith("Harp")) {
            melodyOpen = true
        }
    }

    @SubscribeEvent
    fun onRender(event: GuiScreenEvent.BackgroundDrawnEvent) {
        if (!melodyOpen) return
        if (Minecraft.getMinecraft().currentScreen !is GuiChest) {
            melodyOpen = false
            return
        }
        val container = mc.thePlayer.openContainer ?: return
        val newHash = container.inventorySlots.subList(0,36).joinToString("") { it?.stack?.displayName ?: "" }.hashCode()
        if (lastInv == newHash) return
        lastInv = newHash
        for (ii in 0..6) {
            val slot = container.inventorySlots[37 + ii]
            if ((slot.stack?.item as? ItemBlock)?.block === Blocks.quartz_block) {
                mc.playerController.windowClick(
                    container.windowId,
                    slot.slotNumber,
                    2,
                    3,
                    mc.thePlayer
                )
                break
            }
        }
    }
}