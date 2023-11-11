package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.DrawGuiScreenEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemSkull
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui.",
    category = Category.DUNGEON
) {
    @SubscribeEvent
    fun onDrawScreen(event: DrawGuiScreenEvent) {
        if (event.gui.inventorySlots.inventorySlots.firstOrNull()?.inventory?.name != "Spirit Leap") return
        //event.isCanceled = true
        val playerHeads = event.container.inventory?.subList(11, 15)?.filter { it?.item is ItemSkull }?: emptyList()
        playerHeads.forEach {
            val dungeonPlayer = DungeonUtils.teammates.find {
                player -> player.entity?.name == it.displayName.noControlCodes
            } ?: return@forEach

            GlStateManager.pushMatrix()
            GlStateManager.enableAlpha()
            GlStateManager.color(255f, 255f, 255f, 255f)
            GlStateManager.translate(100f, 100f, 0f)
            mc.textureManager.bindTexture(dungeonPlayer.locationSkin)

            Gui.drawScaledCustomSizeModalRect(-6, -6, 8f, 8f, 8, 8, 120, 120, 64f, 64f)
            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }
    }
}