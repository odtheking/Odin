package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.DrawGuiScreenEvent
import me.odinmain.events.impl.GuiClickEvent
import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.PlayerUtils
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
import org.lwjgl.input.Mouse

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui.",
    category = Category.DUNGEON
) {
    private val type: Boolean by DualSetting("Sorting", "Class", "Name", default = true)

    private var leapTeammates1 : Array<DungeonPlayer?>? = mutableListOf(
        DungeonPlayer("Stiviaisd", Classes.Archer, ResourceLocation("textures/entity/steve.png")),


        ).toTypedArray()

    private val leapTeammates =
        leapTeammates1?.let { fillPlayerList(it) }

    @SubscribeEvent
    fun mouseClicked(event: GuiClickEvent) {
        if (event.button != 0 || event.gui !is GuiChest || event.gui.inventorySlots !is ContainerChest || (event.gui.inventorySlots as ContainerChest).name != "Spirit Leap")  return

        val quadrant = getQuadrant(event.x, event.y)

        val playerToLeap = leapTeammates?.get(quadrant - 1)

        val index = event.gui.inventorySlots.inventorySlots.subList(11, 15)
            .indexOfFirst { it?.stack?.displayName?.noControlCodes == playerToLeap?.name }
                .takeIf { it != -1 } ?: return
        modMessage("Teleporting to ${playerToLeap?.name} (${playerToLeap?.clazz?.name}) in index $index.")

        mc.playerController.windowClick(event.gui.inventorySlots.windowId, 11 + index, 1, 2, mc.thePlayer)
    }

    private fun fillPlayerList(players: Array<DungeonPlayer?>): Array<DungeonPlayer?> {
        val sortedPlayers = players.sortedBy { it?.clazz?.prio }
        val result = arrayOfNulls<DungeonPlayer?>(4)
        val secondRound = mutableListOf<DungeonPlayer>()

        for (player in sortedPlayers) {
            if (result[player?.clazz?.defaultQuandrant!!] == null) {
                result[player.clazz.defaultQuandrant] = player
                println("Added $player to result.")
            } else {
                secondRound.add(player)
                println("Added $player to second round.")
                println("Second round: ${secondRound.joinToString(", ") { it.name }}")
            }
        }

        println("${secondRound.size} players in the second round.")

        if (secondRound.isEmpty()) return result

        result.forEachIndexed { index, player ->
            if (result[index] == null) {
                println(index)
                result[index] = secondRound.removeAt(0)
                if (secondRound.isEmpty()) return result
                println("Added $player to result.")
            }
        }
        println("Result: ${result.joinToString(", ") { it?.name ?: "null" }}")
        return result
    }


    @SubscribeEvent
    fun onGuiLoad(event: GuiLoadedEvent) {
        if (event.name != "Spirit Leap") return

        val playerHeads = event.gui.inventory?.subList(11, 15)?.filter { it?.item is ItemSkull } ?: emptyList()

        if (type) {
           // leapTeammates =
            //    DungeonUtils.teammates
              //      .filter { playerHeads.any { head -> head.displayName.noControlCodes == it.name } }
                 //   .sortedBy {
                  //      it.clazz.ordinal
                 //   }.toMutableList()
        } else {
            //leapTeammates = leapTeammates1?.let { fillPlayerList(it) }
        }

    }

    @SubscribeEvent
    fun onDrawScreen(event: DrawGuiScreenEvent) {

        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap") return

        val sr = ScaledResolution(mc)
        leapTeammates?.forEachIndexed { index, it ->
            if (it == null) return@forEachIndexed
            GlStateManager.pushMatrix()
            GlStateManager.enableAlpha()
            GlStateManager.scale(6.0 / sr.scaleFactor,  6.0 / sr.scaleFactor, 1.0)
            GlStateManager.color(255f, 255f, 255f, 255f)
            GlStateManager.translate(50 + (index % 2 * 145f), if (index >= 2) 115f else 35f, 0f)
            mc.textureManager.bindTexture(mc.thePlayer.locationSkin/*it.locationSkin*/)

            Gui.drawRect(-15, -15, 90, 35, Color.DARK_GRAY.rgba)

            GlStateManager.color(255f, 255f, 255f, 255f)
            Gui.drawScaledCustomSizeModalRect(-10, -10, 8f, 8f, 8, 8, 40, 40, 64f, 64f)
            if (it != null) {
                mc.fontRendererObj.drawString(it.name, 35, 5, it.clazz.color.rgba)
            }

            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }
        event.isCanceled = true

    }
    private fun getQuadrant(mouseX: Int, mouseY: Int): Int {
        val screenWidth = mc.displayWidth / 2
        val screenHeight = mc.displayHeight / 2

        val centerX = screenWidth / 2
        val centerY = screenHeight / 2

        return when {
            (mouseX < centerX && mouseY < centerY) -> 1
            (mouseX >= centerX && mouseY < centerY) -> 2
            (mouseX < centerX && mouseY >= centerY) -> 3
            else -> 4
        }
    }





}