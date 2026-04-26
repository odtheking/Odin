package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonPlayer
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.leapTeammates
import com.odtheking.odin.utils.ui.HoverHandler
import com.odtheking.odin.utils.ui.widget.CustomGUIImpl
import net.minecraft.client.gui.components.PlayerFaceExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.PlayerHeadItem
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
    private val scale by NumberSetting("Render Scale", 1f, 0.1f, 2f, 0.1f, desc = "Scale of the leap menu.", unit = "x")
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

    private val EMPTY = DungeonPlayer("Empty", DungeonClass.Unknown, 0, null)
    private val leapedRegex = Regex("You have teleported to (\\w{1,16})!")

    const val BOX_WIDTH = 200
    const val BOX_HEIGHT = 75

    private fun currentLeapScreen(): AbstractContainerScreen<*>? {
        if (!enabled) return null
        val screen = mc.screen as? AbstractContainerScreen<*> ?: return null
        if (!screen.title.string.equalsOneOf("Spirit Leap", "Teleport to Player")) return null
        if (leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return null
        return screen
    }

    init {
        fun triggerMouseQuadrant(chest: AbstractContainerScreen<*>, mouseX: Int, mouseY: Int) {
            val quadrant = (if (mouseY >= chest.height / 2) 2 else 0) + (if (mouseX >= chest.width / 2) 1 else 0)
            chest.mouseTrigger(leapTeammates.getOrNull(quadrant) ?: EMPTY, quadrant)
        }

        CustomGUIImpl.register(
            CustomGUIImpl.HandlerSet(enabled = { currentLeapScreen() != null },
            click = fun ScreenEvent.MouseClick.(): Any {
                val chest = currentLeapScreen() ?: return false
                if (onRelease) return true
                triggerMouseQuadrant(chest, click.x().toInt(), click.y().toInt())
                return true
            },
            release = fun ScreenEvent.MouseRelease.(): Any {
                val chest = currentLeapScreen() ?: return false
                if (!onRelease) return true
                triggerMouseQuadrant(chest, click.x().toInt(), click.y().toInt())
                return true
            },
            key = fun ScreenEvent.KeyPress.(): Any {
                if (leapTeammates.isEmpty()) return false
                val keybindList = if (keybindType == 0) listOf(topLeftKeybind, topRightKeybind, bottomLeftKeybind, bottomRightKeybind)
                else listOf(archerKeybind, berserkerKeybind, healerKeybind, mageKeybind, tankKeybind)
                if (keybindList.none { it.value == input.key() }) return false

                val chest = currentLeapScreen() ?: return false

                val index = if (keybindType == 0) keybindList.indexOfFirst { it.value == input.key() }
                else DungeonClass.entries.find { clazz -> clazz.ordinal == keybindList.indexOfFirst { it.value == input.key() } }
                    ?.let { clazz -> leapTeammates.indexOfFirst { it.clazz == clazz } } ?: return false

                if (index < 0) return false

                val playerToLeap = leapTeammates.getOrNull(index) ?: return false
                if (playerToLeap == EMPTY) return false
                if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")
                leapTo(playerToLeap.name, chest)
                return true
            },
            render = fun ScreenEvent.Render.(): Any {
                val halfW = mc.window.guiScaledWidth / 2
                val halfH = mc.window.guiScaledHeight / 2

                repeat(4) { i ->
                    val player = leapTeammates.getOrNull(i) ?: return@repeat
                    if (player == EMPTY) return@repeat

                    val col = i % 2
                    val row = i / 2
                    val nearX = if (col == 0) halfW - 24 else halfW + 24
                    val nearY = if (row == 0) halfH - 24 else halfH + 24
                    val localX = if (col == 0) -BOX_WIDTH else 0
                    val localY = if (row == 0) -BOX_HEIGHT else 0

                    val hover = hoverHandler[i]
                    val hovered = (if (col == 0) mouseX < halfW else mouseX >= halfW) && (if (row == 0) mouseY < halfH else mouseY >= halfH)
                    if (hovered != hover.isHovered) {
                        hover.anim.start()
                        hover.isHovered = hovered
                    }

                    val grow = hover.anim.get(0f, 5f, !hover.isHovered)

                    guiGraphics.pose().pushMatrix()
                    guiGraphics.pose().translate(nearX.toFloat(), nearY.toFloat())
                    guiGraphics.pose().scale(
                        scale * (BOX_WIDTH + grow * 2f) / BOX_WIDTH,
                        scale * (BOX_HEIGHT + grow * 2f) / BOX_HEIGHT
                    )

                    guiGraphics.roundedFill(
                        localX, localY, localX + BOX_WIDTH, localY + BOX_HEIGHT,
                        (if (colorStyle) player.clazz.color else backgroundColor).rgba, 9
                    )

                    val face = (BOX_HEIGHT * 0.76).toInt()
                    (player.playerSkin ?: mc.player?.skin)?.let { PlayerFaceExtractor.extractRenderState(guiGraphics, it, localX + 9, localY + 9, face) }

                    guiGraphics.text(
                        if (!onlyClass) player.name else player.clazz.name,
                        localX + 15 + face,
                        localY + (BOX_HEIGHT / 2.5).toInt(),
                        if (!colorStyle) player.clazz.color else backgroundColor
                    )

                    if (!onlyClass || player.isDead) {
                        guiGraphics.text(
                            if (player.isDead) "DEAD" else player.clazz.name,
                            localX + 15 + face,
                            localY + (BOX_HEIGHT / 1.7).toInt(),
                            if (player.isDead) Colors.MINECRAFT_RED else Colors.WHITE
                        )
                    }

                    guiGraphics.pose().popMatrix()
                }
                return true
            })
        )

        on<ChatPacketEvent> {
            if (leapAnnounce && DungeonUtils.inDungeons)
                leapedRegex.find(value)?.groupValues?.get(1)?.let { sendCommand("pc Leaped to ${it}!") }
        }

        on<LevelEvent.Load> {
            indexCache.clear()
        }
    }

    fun AbstractContainerScreen<*>.mouseTrigger(player: DungeonPlayer, quadrant: Int) {
        if ((type.equalsOneOf(1,2,3)) && leapTeammates.size < quadrant) return

        if (player == EMPTY) return
        if (player.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(player.name, this)
    }

    private val indexCache = mutableMapOf<String, Int>()

    private fun leapTo(name: String, screenHandler: AbstractContainerScreen<*>) {
        val slots = screenHandler.menu.slots

        indexCache.putAll(buildMap {
            for (slot in slots.subList(11, 16)) {
                val stack = slot.item
                if (stack.item is PlayerHeadItem) put(stack.hoverName.string.substringAfter(' ').noControlCodes, slot.index)
            }
        })

        indexCache[name.noControlCodes]?.let { index ->
            mc.player?.clickSlot(screenHandler.menu.containerId, index)
            modMessage("Teleporting to $name.")
        }
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