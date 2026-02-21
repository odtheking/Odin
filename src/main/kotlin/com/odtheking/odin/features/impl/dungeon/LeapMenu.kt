package com.odtheking.odin.features.impl.dungeon

import com.mojang.blaze3d.opengl.GlTexture
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonPlayer
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.leapTeammates
import com.odtheking.odin.utils.ui.HoverHandler
import com.odtheking.odin.utils.ui.getQuadrant
import com.odtheking.odin.utils.ui.rendering.NVGPIPRenderer
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui.",
    key = GLFW.GLFW_KEY_UNKNOWN
) {
    val type by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("Odin Sorting", "A-Z Class", "A-Z Name", "Custom sorting", "No Sorting"), desc = "How to sort the leap menu. /od leaporder to configure custom sorting.")
    private val onRelease by BooleanSetting("On Key Release", false, desc = "Whether to trigger the leap on key release instead of key press.")
    private val onlyClass by BooleanSetting("Only Classes", false, desc = "Renders classes instead of names.")
    private val colorStyle by BooleanSetting("Color Style", false, desc = "Which color style to use.")
    private val backgroundColor by ColorSetting("Background Color", Colors.gray38.withAlpha(0.75f), true, desc = "Color of the background of the leap menu.").withDependency { !colorStyle }
    private val scale by NumberSetting("Scale", 0.5f, 0.1f, 2f, 0.1f, desc = "Scale of the leap menu.", unit = "x")
    val keybindType by SelectorSetting("Mode", "Normal", arrayListOf("Corners", "Class"), desc = "How the keybinds should function.")

    private val topLeftKeybind by KeybindSetting("Top Left", GLFW.GLFW_KEY_UNKNOWN, "Used to click on the first person in the leap menu.").withDependency { keybindType == 0 }
    private val topRightKeybind by KeybindSetting("Top Right", GLFW.GLFW_KEY_UNKNOWN, "Used to click on the second person in the leap menu.").withDependency { keybindType == 0 }
    private val bottomLeftKeybind by KeybindSetting("Bottom Left", GLFW.GLFW_KEY_UNKNOWN, "Used to click on the third person in the leap menu.").withDependency { keybindType == 0 }
    private val bottomRightKeybind by KeybindSetting("Bottom Right", GLFW.GLFW_KEY_UNKNOWN, "Used to click on the fourth person in the leap menu.").withDependency { keybindType == 0 }

    private val archerKeybind by KeybindSetting("Archer", GLFW.GLFW_KEY_UNKNOWN, "Used to leap to the Archer in the leap menu.").withDependency { keybindType == 1 }
    private val berserkerKeybind by KeybindSetting("Berserker", GLFW.GLFW_KEY_UNKNOWN, "Used to leap to the Berserker in the leap menu.").withDependency { keybindType == 1 }
    private val healerKeybind by KeybindSetting("Healer", GLFW.GLFW_KEY_UNKNOWN, "Used to leap to the Healer in the leap menu.").withDependency { keybindType == 1 }
    private val mageKeybind by KeybindSetting("Mage", GLFW.GLFW_KEY_UNKNOWN, "Used to leap to the Mage in the leap menu.").withDependency { keybindType == 1 }
    private val tankKeybind by KeybindSetting("Tank", GLFW.GLFW_KEY_UNKNOWN, "Used to leap to the Tank in the leap menu.").withDependency { keybindType == 1 }

    private val leapAnnounce by BooleanSetting("Leap Announce", false, desc = "Announces when you leap to a player.")
    private val hoverHandler = List(4) { HoverHandler(200L) }

    private val EMPTY = DungeonPlayer("Empty", DungeonClass.Unknown, 0, ResourceLocation.withDefaultNamespace("textures/entity/steve.png"))
    private val leapedRegex = Regex("You have teleported to (\\w{1,16})!")
    private val imageCacheMap = mutableMapOf<String, Int>()
    const val BOX_WIDTH = 800f
    const val BOX_HEIGHT = 300f

    init {
        on<GuiEvent.Draw> {
            val chest = (screen as? AbstractContainerScreen<*>) ?: return@on
            if (chest.title?.string?.equalsOneOf("Spirit Leap", "Teleport to Player") == false || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return@on

            val halfWidth = mc.window.screenWidth / 2f
            val halfHeight = mc.window.screenHeight / 2f

            hoverHandler[0].handle(0f, 0f, halfWidth, halfHeight)
            hoverHandler[1].handle(halfWidth, 0f, halfWidth, halfHeight)
            hoverHandler[2].handle(0f, halfHeight, halfWidth, halfHeight)
            hoverHandler[3].handle(halfWidth, halfHeight, halfWidth, halfHeight)

            NVGPIPRenderer.draw(guiGraphics, 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight()) {
                NVGRenderer.scale(scale, scale)
                NVGRenderer.translate(halfWidth / scale, halfHeight / scale)
                leapTeammates.forEachIndexed { index, player ->
                    if (player == EMPTY) return@forEachIndexed

                    val x = when (index) {
                        0, 2 -> -((mc.window.screenWidth - (BOX_WIDTH * 2f)) / 6f + BOX_WIDTH)
                        else -> ((mc.window.screenWidth - (BOX_WIDTH * 2f)) / 6f)
                    }
                    val y = when (index) {
                        0, 1 -> -((mc.window.screenHeight - (BOX_HEIGHT * 2f)) / 8f + BOX_HEIGHT)
                        else -> ((mc.window.screenHeight - (BOX_HEIGHT * 2f)) / 8f)
                    }

                    val expandValue = hoverHandler[index].anim.get(0f, 15f, !hoverHandler[index].isHovered)
                    NVGRenderer.rect(x - expandValue ,y - expandValue, BOX_WIDTH + expandValue * 2, BOX_HEIGHT + expandValue * 2, (if (colorStyle) player.clazz.color else backgroundColor).rgba, 12f)
                    val locationSkin = player.locationSkin ?: mc.player?.skin?.body?.id() ?: return@forEachIndexed
                    imageCacheMap.getOrPut(locationSkin.path) {
                        NVGRenderer.createNVGImage((mc.textureManager?.getTexture(locationSkin)?.texture as? GlTexture)?.glId() ?: 0, 64, 64)
                    }.let { glTextureId ->
                        NVGRenderer.image(glTextureId, 64, 64, 8, 8, 8, 8, x + 30f, y + 30f, 240f, 240f, 9f)
                    }

                    NVGRenderer.textShadow(if (!onlyClass) player.name else player.clazz.name, x + 275f, y + 110f, 50f, if (!colorStyle) player.clazz.color.rgba else backgroundColor.rgba, NVGRenderer.defaultFont)
                    if (!onlyClass || player.isDead) NVGRenderer.textShadow(if (player.isDead) "DEAD" else player.clazz.name, x + 275f, y + 180f, 40f, if (player.isDead) Colors.MINECRAFT_RED.rgba else Colors.WHITE.rgba, NVGRenderer.defaultFont)
                }
            }
            cancel()
        }

        on<GuiEvent.DrawBackground> {
            val chest = (screen as? AbstractContainerScreen<*>) ?: return@on
            if (chest.title?.string?.equalsOneOf("Spirit Leap", "Teleport to Player") == false || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return@on
            cancel()
        }

        on<GuiEvent.MouseClick> {
            if (!onRelease) mouseTrigger()
        }

        on<GuiEvent.MouseRelease> {
            if (onRelease) mouseTrigger()
        }

        on<GuiEvent.KeyPress> {
            val chest = (screen as? AbstractContainerScreen<*>) ?: return@on
            val keybindList =
                if(keybindType == 0) listOf(topLeftKeybind, topRightKeybind, bottomLeftKeybind, bottomRightKeybind)
                else listOf(archerKeybind, berserkerKeybind, healerKeybind, mageKeybind, tankKeybind)
            if (chest.title?.string?.equalsOneOf("Spirit Leap", "Teleport to Player") == false || keybindList.none { it.value == input.key() } || leapTeammates.isEmpty()) return@on

            val index = if(keybindType == 0) keybindList.indexOfFirst { it.value == input.key() }
            else DungeonClass.entries.find { clazz -> clazz.ordinal == keybindList.indexOfFirst { it.value == input.key() } }?.let { clazz -> leapTeammates.indexOfFirst { it.clazz == clazz } } ?: return@on
            if (index == -1) return@on
            val playerToLeap = leapTeammates[index]
            if (playerToLeap == EMPTY) return@on
            if (playerToLeap.isDead) return@on modMessage("This player is dead, can't leap.")

            leapTo(playerToLeap.name, chest)
            cancel()
        }

        on<ChatPacketEvent> {
            if (!leapAnnounce || !DungeonUtils.inDungeons) return@on
            leapedRegex.find(value)?.groupValues?.get(1)?.let { sendCommand("pc Leaped to ${it}!") }
        }
    }

    fun GuiEvent.mouseTrigger() {
        val chest = (screen as? AbstractContainerScreen<*>) ?: return
        if (chest.title?.string?.equalsOneOf("Spirit Leap", "Teleport to Player") == false || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return

        val quadrant = getQuadrant()
        if ((type.equalsOneOf(1,2,3)) && leapTeammates.size < quadrant) return

        val playerToLeap = leapTeammates[quadrant - 1]
        if (playerToLeap == EMPTY) return
        if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, chest)
        cancel()
    }

    private fun leapTo(name: String, screenHandler: AbstractContainerScreen<*>) {
        val index = screenHandler.menu.slots.subList(11, 16).firstOrNull {
            it.item?.hoverName?.string?.substringAfter(' ').equals(name.noControlCodes, ignoreCase = true)
        }?.index ?: return
        mc.player?.clickSlot(screenHandler.menu.containerId, index)
        modMessage("Teleporting to $name.")
    }

    /*private val leapTeammates: MutableList<DungeonPlayer> = mutableListOf(
        DungeonPlayer("Stiviaisd", DungeonClass.Healer, 50, null),
        DungeonPlayer("Odtheking", DungeonClass.Archer, 50, null),
        DungeonPlayer("Bonzi", DungeonClass.Mage, 47, null),
        DungeonPlayer("Cezar", DungeonClass.Tank, 38, null)
    )*/

    /**
     * Sorts the list of players based on their default quadrant and class priority.
     * The function first tries to place each player in their default quadrant. If the quadrant is already occupied,
     * the player is added to a second round list. After all players have been processed, the function fills the remaining
     * empty quadrants with the players from the second round list.
     *
     * @param players The list of players to be sorted.
     * @return An array of sorted players.
     */
    fun odinSorting(players: List<DungeonPlayer>): Array<DungeonPlayer> {
        val result = Array(4) { EMPTY }
        val secondRound = mutableListOf<DungeonPlayer>()

        for (player in players.sortedBy { it.clazz.priority }) {
            when {
                result[player.clazz.defaultQuadrant] == EMPTY -> result[player.clazz.defaultQuadrant] = player
                else -> secondRound.add(player)
            }
        }

        if (secondRound.isEmpty()) return result

        result.forEachIndexed { index, _ ->
            when {
                result[index] == EMPTY -> {
                    result[index] = secondRound.removeAt(0)
                    if (secondRound.isEmpty()) return result
                }
            }
        }
        return result
    }
}