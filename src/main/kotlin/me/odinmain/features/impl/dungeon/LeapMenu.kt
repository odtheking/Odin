package me.odinmain.features.impl.dungeon

import io.github.moulberry.notenoughupdates.NEUApi
import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.KeybindSetting
import me.odinmain.clickgui.settings.impl.SelectorSetting
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Module
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color.Companion.withAlpha
import me.odinmain.utils.render.Colors
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.utils.skyblock.dungeon.DungeonClass
import me.odinmain.utils.skyblock.dungeon.DungeonListener.leapTeammates
import me.odinmain.utils.skyblock.dungeon.DungeonPlayer
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.skyblock.unformattedName
import me.odinmain.utils.ui.HoverHandler
import me.odinmain.utils.ui.getQuadrant
import me.odinmain.utils.ui.rendering.NVGRenderer
import me.odinmain.utils.ui.rendering.NVGRenderer.createFaceImage
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.Display

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui."
) {
    val type by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("Odin Sorting", "A-Z Class (BetterMap)", "A-Z Name", "Custom sorting", "No Sorting"), desc = "How to sort the leap menu. /od leaporder to configure custom sorting.")
    private val onlyClass by BooleanSetting("Only Classes", false, desc = "Renders classes instead of names.")
    private val colorStyle by BooleanSetting("Color Style", false, desc = "Which color style to use.")
    private val backgroundColor by ColorSetting("Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.75f), true, desc = "Color of the background of the leap menu.").withDependency { !colorStyle }
    private val useNumberKeys by BooleanSetting("Use Number Keys", false, desc = "Use keyboard keys to leap to the player you want, going from left to right, top to bottom.")
    private val topLeftKeybind by KeybindSetting("Top Left", Keyboard.KEY_1, "Used to click on the first person in the leap menu.").withDependency { useNumberKeys }
    private val topRightKeybind by KeybindSetting("Top Right", Keyboard.KEY_2, "Used to click on the second person in the leap menu.").withDependency { useNumberKeys }
    private val bottomLeftKeybind by KeybindSetting("Bottom Left", Keyboard.KEY_3, "Used to click on the third person in the leap menu.").withDependency { useNumberKeys }
    private val bottomRightKeybind by KeybindSetting("Bottom right", Keyboard.KEY_4, "Used to click on the fourth person in the leap menu.").withDependency { useNumberKeys }
    private val leapAnnounce by BooleanSetting("Leap Announce", false, desc = "Announces when you leap to a player.")
    private val hoverHandler = List(4) { HoverHandler(200L) }

    private val EMPTY = DungeonPlayer("Empty", DungeonClass.Unknown, 0, ResourceLocation("textures/entity/steve.png"))
    private val keybindList = listOf(topLeftKeybind, topRightKeybind, bottomLeftKeybind, bottomRightKeybind)
    private val imageCacheMap = mutableMapOf<String, Int>()

    @SubscribeEvent
    fun onDrawScreen(event: GuiEvent.DrawGuiBackground) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || !chest.name.equalsOneOf("Spirit Leap", "Teleport to Player") || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return

        val halfWidth = Display.getWidth() / 2f
        val halfHeight = Display.getHeight() / 2f

        hoverHandler[0].handle(0f, 0f, halfWidth, halfHeight)
        hoverHandler[1].handle(halfWidth, 0f, halfWidth, halfHeight)
        hoverHandler[2].handle(0f, halfHeight, halfWidth, halfHeight)
        hoverHandler[3].handle(halfWidth, halfHeight, halfWidth, halfHeight)

        NVGRenderer.beginFrame(Display.getWidth().toFloat(), Display.getHeight().toFloat())
        NVGRenderer.translate(halfWidth, halfHeight)
        val boxWidth = 800f
        val boxHeight = 300f
        leapTeammates.forEachIndexed { index, player ->
            if (player == EMPTY) return@forEachIndexed

            val x = when (index) {
                0, 2 -> -((1920f - (boxWidth * 2f)) / 6f + boxWidth)
                else -> ((1920f - (boxWidth * 2f)) / 6f)
            }
            val y = when (index) {
                0, 1 -> -((1080f - (boxHeight * 2f)) / 8f + boxHeight)
                else -> ((1080f - (boxHeight * 2f)) / 8f)
            }

            val color = if (colorStyle) player.clazz.color else backgroundColor
            val expandValue = hoverHandler[index].anim.get(0f, 5f, !hoverHandler[index].hasStarted)

            NVGRenderer.rect(x - expandValue ,y - expandValue, boxWidth + expandValue * 2, boxHeight + expandValue * 2, color.rgba, 12f)
            imageCacheMap.getOrPut(player.locationSkin.resourcePath) {
                createFaceImage(mc.textureManager.getTexture(player.locationSkin)?.glTextureId ?: 0, 64, 64)
            }.let { glTextureId ->
                NVGRenderer.drawSubImage(glTextureId, 64, 64, 8, 8, 8, 8, x + 30f, y + 30f, 240f, 240f, 9f)
            }

            NVGRenderer.text(player.name, x + 275f, y + 155f, 45f, if (!colorStyle) player.clazz.color.rgba else backgroundColor.rgba, NVGRenderer.defaultFont)
            if (!onlyClass || player.isDead) NVGRenderer.text(if (player.isDead) "§cDEAD" else player.clazz.name, x + 275f, y + 210f, 30f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
        NVGRenderer.endFrame()
        event.isCanceled = true
    }

    @SubscribeEvent
    fun guiOpen(event: GuiOpenEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || !chest.name.equalsOneOf("Spirit Leap", "Teleport to Player") || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return
        if (Loader.instance().activeModList.any { it.modId == "notenoughupdates" }) NEUApi.setInventoryButtonsToDisabled()
    }

    @SubscribeEvent
    fun mouseClicked(event: GuiEvent.MouseClick) {
        val gui = (event.gui as? GuiChest)?.inventorySlots as? ContainerChest ?: return
        if (!gui.name.equalsOneOf("Spirit Leap", "Teleport to Player") || leapTeammates.isEmpty())  return

        val quadrant = getQuadrant()
        if ((type.equalsOneOf(1,2,3)) && leapTeammates.size < quadrant) return

        val playerToLeap = leapTeammates[quadrant - 1]
        if (playerToLeap == EMPTY) return
        if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, gui)

        event.isCanceled = true
    }

    @SubscribeEvent
    fun keyTyped(event: GuiEvent.KeyPress) {
        val gui = (event.gui as? GuiChest)?.inventorySlots as? ContainerChest ?: return
        if (!useNumberKeys || !gui.name.equalsOneOf("Spirit Leap", "Teleport to Player") || keybindList.none { it.key == event.key } || leapTeammates.isEmpty()) return

        val index = keybindList.indexOfFirst { it.key == event.key }
        val playerToLeap = if (index + 1 > leapTeammates.size) return else leapTeammates[index]
        if (playerToLeap == EMPTY) return
        if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, gui)

        event.isCanceled = true
    }

    private fun leapTo(name: String, containerChest: ContainerChest) {
        val index = containerChest.inventorySlots.subList(11, 16).firstOrNull {
            it.stack?.unformattedName?.noControlCodes?.substringAfter(' ').equals(name.noControlCodes, ignoreCase = true)
        }?.slotIndex ?: return modMessage("Can't find player $name. This shouldn't be possible! are you nicked?")
        modMessage("Teleporting to $name.")
        windowClick(index, ClickType.Middle)
    }

    init {
        onMessage(Regex("You have teleported to (\\w{1,16})!")) {
            if (leapAnnounce) partyMessage("Leaped to ${it.groupValues[1]}!")
        }
    }

    /*private val leapTeammates: MutableList<DungeonPlayer> = mutableListOf(
        DungeonPlayer("Stiviaisd", DungeonClass.Healer, 50),
        DungeonPlayer("Odtheking", DungeonClass.Archer, 50),
        DungeonPlayer("Bonzi", DungeonClass.Mage, 47),
        DungeonPlayer("Cezar", DungeonClass.Tank, 38)
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
