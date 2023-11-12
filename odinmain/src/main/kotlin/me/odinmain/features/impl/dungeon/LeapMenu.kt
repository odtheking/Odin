package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.DrawGuiScreenEvent
import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.DungeonPlayer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemSkull
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui.",
    category = Category.DUNGEON
) {

    private var leapTeammates = mutableListOf<DungeonPlayer>()

    @SubscribeEvent
    fun onGuiLoad(event: GuiLoadedEvent) {
        if (event.name != "Spirit Leap") return
        val playerHeads = event.gui.inventory?.subList(11, 15)?.filter { it?.item is ItemSkull } ?: emptyList()

        leapTeammates =
            DungeonUtils.teammates
                .filter { playerHeads.any { head -> head.displayName.noControlCodes == it.entity?.name } }
                .sortedBy {
                    it.clazz.ordinal
                }.toMutableList()
    }

    @SubscribeEvent
    fun onDrawScreen(event: DrawGuiScreenEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap") return
        leapTeammates.forEachIndexed { index, it ->
            GlStateManager.pushMatrix()
            GlStateManager.enableAlpha()
            GlStateManager.color(255f, 255f, 255f, 255f)
            GlStateManager.translate(100 + (index % 2 * 300f), if (index > 2) 400f else 100f, 0f)
            mc.textureManager.bindTexture(it.locationSkin)

            Gui.drawScaledCustomSizeModalRect(-6, -6, 8f, 8f, 8, 8, 120, 120, 64f, 64f)
            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }
    }
}