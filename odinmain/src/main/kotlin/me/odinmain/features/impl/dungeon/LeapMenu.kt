package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.DrawGuiScreenEvent
import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.DungeonPlayer
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemSkull
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui.",
    category = Category.DUNGEON
) {

    private var leapTeammates = mutableListOf(
        DungeonPlayer("Bonzi", Classes.Mage, ResourceLocation("textures/entity/steve.png")),
        DungeonPlayer("Odtheking", Classes.Archer, ResourceLocation("textures/entity/steve.png")),
        DungeonPlayer("CEzar", Classes.Tank, ResourceLocation("textures/entity/steve.png")),
        DungeonPlayer("Stiviaisd", Classes.Berserk, ResourceLocation("textures/entity/steve.png"))
    ).sortedBy { it.clazz.ordinal }.toMutableList()

    @SubscribeEvent
    fun onGuiLoad(event: GuiLoadedEvent) {
        if (event.name != "Spirit Leap") return
        val playerHeads = event.gui.inventory?.subList(11, 15)?.filter { it?.item is ItemSkull } ?: emptyList()

        leapTeammates =
            DungeonUtils.teammates
                .filter { playerHeads.any { head -> head.displayName.noControlCodes == it.name } }
                .sortedBy {
                    it.clazz.ordinal
                }.toMutableList()
    }

    @SubscribeEvent
    fun onDrawScreen(event: DrawGuiScreenEvent) {

        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap") return

        val sr = ScaledResolution(mc)
        leapTeammates.forEachIndexed { index, it ->
            GlStateManager.pushMatrix()
            GlStateManager.enableAlpha()
            GlStateManager.scale(6.0 / sr.scaleFactor,  6.0 / sr.scaleFactor, 1.0)
            GlStateManager.color(255f, 255f, 255f, 255f)
            GlStateManager.translate(50 + (index % 2 * 145f), if (index >= 2) 115f else 35f, 0f)
            mc.textureManager.bindTexture(mc.thePlayer.locationSkin/*it.locationSkin*/)

            Gui.drawRect(-15, -15, 90, 35, Color.DARK_GRAY.rgba)
            GlStateManager.color(255f, 255f, 255f, 255f)
            Gui.drawScaledCustomSizeModalRect(-10, -10, 8f, 8f, 8, 8, 40, 40, 64f, 64f)
            mc.fontRendererObj.drawString(it.name, 35, 5, it.clazz.color.rgba)

            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }
        event.isCanceled = true
    }
}