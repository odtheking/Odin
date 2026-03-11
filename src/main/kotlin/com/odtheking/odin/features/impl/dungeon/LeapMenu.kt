package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.ScreenEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonPlayer
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.ui.HoverHandler
import com.odtheking.odin.utils.ui.widget.CustomGUIImpl
import com.odtheking.odin.utils.ui.widget.simpleWidget
import net.minecraft.client.gui.components.PlayerFaceRenderer
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
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
    private val scale by NumberSetting("Scale", 1f, 0.1f, 2f, 0.1f, desc = "Scale of the leap menu.", unit = "x")
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

    init {
        on<ScreenEvent.Open> {
            if (screen.title?.string?.equalsOneOf("Spirit Leap", "Teleport to Player") == false || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return@on
            val chest = (screen as? AbstractContainerScreen<*>) ?: return@on

            val halfW = mc.window.guiScaledWidth / 2
            val halfH = mc.window.guiScaledHeight / 2
            val halfScreenW = mc.window.screenWidth / 2f
            val halfScreenH = mc.window.screenHeight / 2f
            val scaledPad = (24 * scale).toInt().coerceAtLeast(4)
            val cardWidth = (BOX_WIDTH * scale).toInt().coerceAtLeast(80)
            val cardHeight = (BOX_HEIGHT * scale).toInt().coerceAtLeast(30)

            repeat(4) { i ->
                val player = leapTeammates.getOrNull(i) ?: EMPTY
                val row = i / 2
                val col = i % 2
                val quadrantX = col * halfW
                val quadrantY = row * halfH
                val hoverX = if (col == 0) 0f else halfScreenW
                val hoverY = if (row == 0) 0f else halfScreenH
                val cardHover = hoverHandler[i]

                CustomGUIImpl.register(screen, simpleWidget(quadrantX, quadrantY, halfW, halfH) {
                    onClick { _, _ ->
                        if (!onRelease) {
                            chest.mouseTrigger(player, i)
                            true
                        } else false
                    }
                    onMouseRelease { _ -> if (onRelease) chest.mouseTrigger(player, i) }
                    onKeyPress { keyEvent ->
                        val keybindList =
                            if(keybindType == 0) listOf(topLeftKeybind, topRightKeybind, bottomLeftKeybind, bottomRightKeybind)
                            else listOf(archerKeybind, berserkerKeybind, healerKeybind, mageKeybind, tankKeybind)
                        if (keybindList.none { it.value == keyEvent.key() } || leapTeammates.isEmpty()) return@onKeyPress false

                        val index = if (keybindType == 0) keybindList.indexOfFirst { it.value == keyEvent.key() }
                        else DungeonClass.entries.find { clazz -> clazz.ordinal == keybindList.indexOfFirst { it.value == keyEvent.key() } }?.let { clazz -> leapTeammates.indexOfFirst { it.clazz == clazz } } ?: return@onKeyPress false
                        if (index == -1) return@onKeyPress false
                        val playerToLeap = leapTeammates[index]
                        if (playerToLeap == EMPTY) return@onKeyPress false
                        if (playerToLeap.isDead) {
                            modMessage("This player is dead, can't leap.")
                            return@onKeyPress false
                        }

                        leapTo(playerToLeap.name, chest)
                        true
                    }
                })

                val cardWidget = simpleWidget(
                    x = if (col == 0) quadrantX + halfW - scaledPad - cardWidth else quadrantX + scaledPad,
                    y = if (row == 0) quadrantY + halfH - scaledPad - cardHeight else quadrantY + scaledPad,
                    cardWidth, cardHeight
                ) {
                    onRender { x, y, w, h ->
                        if (player == EMPTY) return@onRender
                        cardHover.handle(hoverX, hoverY, halfScreenW, halfScreenH)

                        val expand = cardHover.anim.get(0f, 5f, !cardHover.isHovered)
                        val xScale = (w + expand * 2f) / w.toFloat()
                        val yScale = (h + expand * 2f) / h.toFloat()
                        val centerX = x + w / 2f
                        val centerY = y + h / 2f

                        pose().pushMatrix()
                        pose().translate(centerX, centerY)
                        pose().scale(xScale, yScale)
                        pose().translate(-centerX, -centerY)
                        roundedFill(x, y, x + w, y + h, (if (colorStyle) player.clazz.color else backgroundColor).rgba, 9)
                        pose().popMatrix()

                        val face = (h * 0.76).toInt()
                        (player.playerSkin ?: mc.player?.skin)?.let { PlayerFaceRenderer.draw(this, it, x + 9, y + 9, face) }

                        text(
                            if (!onlyClass) player.name else player.clazz.name,
                            x + 15 + face, (y + h / 2.5).toInt(),
                            if (!colorStyle) player.clazz.color else backgroundColor
                        )

                        if (!onlyClass || player.isDead) {
                            text(
                                if (player.isDead) "DEAD" else player.clazz.name,
                                x + 15 + face, (y + h / 1.7).toInt(),
                                if (player.isDead) Colors.MINECRAFT_RED else Colors.WHITE
                            )
                        }
                    }
                }
                CustomGUIImpl.register(screen, cardWidget)
            }
        }

        on<ChatPacketEvent> {
            if (leapAnnounce && DungeonUtils.inDungeons)
                leapedRegex.find(value)?.groupValues?.get(1)?.let { sendCommand("pc Leaped to ${it}!") }
        }
    }

    fun AbstractContainerScreen<*>.mouseTrigger(player: DungeonPlayer, quadrant: Int) {
        if ((type.equalsOneOf(1,2,3)) && leapTeammates.size < quadrant) return

        if (player == EMPTY) return
        if (player.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(player.name, this)
    }

    private fun leapTo(name: String, screenHandler: AbstractContainerScreen<*>) {
        val index = screenHandler.menu.slots.subList(11, 16).firstOrNull {
            it.item?.hoverName?.string?.substringAfter(' ').equals(name.noControlCodes, ignoreCase = true)
        }?.index ?: return
        mc.player?.clickSlot(screenHandler.menu.containerId, index)
        modMessage("Teleporting to $name.")
    }

    private val leapTeammates: MutableList<DungeonPlayer> = mutableListOf(
        DungeonPlayer("Stiviaisd", DungeonClass.Healer, 50, null),
        DungeonPlayer("Odtheking", DungeonClass.Archer, 50, null),
        DungeonPlayer("Bonzi", DungeonClass.Mage, 47, null),
        DungeonPlayer("Cezar", DungeonClass.Tank, 38, null)
    )

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