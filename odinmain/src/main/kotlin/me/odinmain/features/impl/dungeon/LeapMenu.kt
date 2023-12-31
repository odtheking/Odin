package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.DrawGuiScreenEvent
import me.odinmain.events.impl.GuiClickEvent
import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.DungeonPlayer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.teammates
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
    private val type: Int by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("A-Z Class", "A-Z Name", "Odin Sorting"))
    private val colorStyle: Boolean by DualSetting("Color Style", "Gray", "Color", default = true, description = "What Click to use")


    private val EMPTY = DungeonPlayer("Empty", Classes.Archer, ResourceLocation("textures/entity/steve.png"))
    private var leapTeammates = mutableListOf<DungeonPlayer>()


   /* private var teammates: List<DungeonPlayer> = listOf(
        DungeonPlayer("Bonzi", Classes.Mage, ResourceLocation("textures/entity/steve.png")),
        DungeonPlayer("OdthekingABCDFEGH", Classes.Archer, ResourceLocation("textures/entity/steve.png")),
        DungeonPlayer("CEzar", Classes.Tank, ResourceLocation("textures/entity/steve.png")),
        DungeonPlayer("Stiviaisd", Classes.Berserk, ResourceLocation("textures/entity/steve.png"))
    )*/

    @SubscribeEvent
    fun mouseClicked(event: GuiClickEvent) {
        if (event.button != 0 || event.gui !is GuiChest || event.gui.inventorySlots !is ContainerChest || (event.gui.inventorySlots as ContainerChest).name != "Spirit Leap")  return

        val quadrant = getQuadrant(event.x, event.y)

        if (leapTeammates.isEmpty()) return
        if ((type == 1 || type == 0) && leapTeammates.size < quadrant ) return

        val playerToLeap = leapTeammates[quadrant - 1]

        val index = event.gui.inventorySlots.inventorySlots.subList(11, 16)
            .indexOfFirst { it?.stack?.displayName?.noControlCodes == playerToLeap.name }
                .takeIf { it != -1 } ?: return
        modMessage("Teleporting to ${playerToLeap.name}.")

        mc.playerController.windowClick(event.gui.inventorySlots.windowId, 11 + index, 1, 2, mc.thePlayer)
        event.isCanceled = true
    }

    private fun fillPlayerList(players: List<DungeonPlayer>): Array<DungeonPlayer> {
        val sortedPlayers = players.sortedBy { it.clazz.prio }
        val result = Array(4) { EMPTY }
        val secondRound = mutableListOf<DungeonPlayer>()

        for (player in sortedPlayers) {
            if (result[player.clazz.defaultQuandrant] == EMPTY) {
                result[player.clazz.defaultQuandrant] = player
            } else {
                secondRound.add(player)
            }
        }

        if (secondRound.isEmpty()) return result

        result.forEachIndexed { index, _ ->
            if (result[index] == EMPTY) {
                result[index] = secondRound.removeAt(0)
                if (secondRound.isEmpty()) return result
            }
        }
        return result
    }


    @SubscribeEvent
    fun onGuiLoad(event: GuiLoadedEvent) {
        if (event.name != "Spirit Leap") return

        val playerHeads = event.gui.inventory?.subList(11, 16)?.filter { it?.item is ItemSkull } ?: emptyList()
        teammates = teammates.filter { playerHeads.any { head -> head.displayName.noControlCodes == it.name } }

        leapTeammates = when (type) {
            0 -> teammates.sortedBy { it.clazz.ordinal }.toMutableList()
            1 -> teammates.sortedBy { it.name }.toMutableList()
            else -> fillPlayerList(teammates).toMutableList()
        }
    }

    @SubscribeEvent
    fun onDrawScreen(event: DrawGuiScreenEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap") return
        if (leapTeammates.isEmpty()) {
            mc.currentScreen = null
            return
        }

        val sr = ScaledResolution(mc)
        leapTeammates.forEachIndexed { index, it ->
            if (it == EMPTY) return@forEachIndexed
            GlStateManager.pushMatrix()
            GlStateManager.enableAlpha()
            GlStateManager.scale(6.0 / sr.scaleFactor,  6.0 / sr.scaleFactor, 1.0)
            GlStateManager.color(255f, 255f, 255f, 255f)
            GlStateManager.translate(30 + (index % 2 * 145f), if (index >= 2) 120f else 40f, 0f)
            mc.textureManager.bindTexture(ResourceLocation("textures/entity/steve.png"))

            Gui.drawRect(-5, -15, 120, 35, if (!colorStyle) Color.DARK_GRAY.rgba else it.clazz.color.rgba)

            GlStateManager.color(255f, 255f, 255f, 255f)
            Gui.drawScaledCustomSizeModalRect(0, -10, 8f, 8f, 8, 8, 40, 40, 64f, 64f)

            if (it.name.length > 14) GlStateManager.scale(1.6 / sr.scaleFactor,  1.6 / sr.scaleFactor, 1.0)
            mc.fontRendererObj.drawString(it.name, if (it.name.length > 13) 52 else 42, 5, if (!colorStyle) it.clazz.color.rgba else Color.DARK_GRAY.rgba)
            GlStateManager.scale(1.5 / sr.scaleFactor,  1.5 / sr.scaleFactor, 1.0)
            mc.fontRendererObj.drawString(it.clazz.name, if (it.name.length > 13) 70 else 56, 20, Color.WHITE.rgba )

            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }
        event.isCanceled = true

    }
    private fun getQuadrant(mouseX: Int, mouseY: Int): Int {
        val screenY = mc.displayHeight / 4

        return when {
            mouseX >= mc.displayWidth / 4 -> if (mouseY >= screenY) 4 else 2
            else -> if (mouseY >= screenY) 3 else 1
        }
    }
}