package me.odinmain.features.impl.dungeon

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.utils.withAlpha
import io.github.moulberry.notenoughupdates.NEUApi
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.LeapHelper.leapHelperBossChatEvent
import me.odinmain.features.impl.dungeon.LeapHelper.worldLoad
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.dungeon.DungeonClass
import me.odinmain.utils.skyblock.dungeon.DungeonPlayer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.leapTeammates
import me.odinmain.utils.skyblock.getItemIndexInContainerChest
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.ui.Colors
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui."
) {
    val type by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("Odin Sorting", "A-Z Class (BetterMap)", "A-Z Name", "Custom sorting", "No Sorting"), description = "How to sort the leap menu.")
    private val onlyClass by BooleanSetting("Only Classes", false, description = "Renders classes instead of names.")
    private val colorStyle by BooleanSetting("Color Style", false, description = "Which color style to use.")
    private val backgroundColor by ColorSetting("Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.9f), allowAlpha = true, description = "Color of the background of the leap menu.")
    private val roundedRect by BooleanSetting("Rounded Rect", true, description = "Toggles the rounded rect for the gui.")
    private val useNumberKeys by BooleanSetting("Use Number Keys", false, description = "Use keyboard keys to leap to the player you want, going from left to right, top to bottom.")
    private val topLeftKeybind by KeybindSetting("Top Left", Keyboard.KEY_1, "Used to click on the first person in the leap menu.").withDependency { useNumberKeys }
    private val topRightKeybind by KeybindSetting("Top Right", Keyboard.KEY_2, "Used to click on the second person in the leap menu.").withDependency { useNumberKeys }
    private val bottomLeftKeybind by KeybindSetting("Bottom Left", Keyboard.KEY_3, "Used to click on the third person in the leap menu.").withDependency { useNumberKeys }
    private val bottomRightKeybind by KeybindSetting("Bottom right", Keyboard.KEY_4, "Used to click on the fourth person in the leap menu.").withDependency { useNumberKeys }
    private val size by NumberSetting("Scale Factor", 1.0f, 0.5f, 2.0f, 0.1f, description = "Scale factor for the leap menu.")
    private val leapHelperToggle by BooleanSetting("Leap Helper", false, description = "Highlights the leap helper player in the leap menu.")
    private val leapHelperColor by ColorSetting("Leap Helper Color", Color.WHITE, description = "Color of the Leap Helper highlight.").withDependency { leapHelperToggle }
    val delay by NumberSetting("Reset Leap Helper Delay", 30, 10.0, 120.0, 1.0, description = "Delay for clearing the leap helper highlight.").withDependency { leapHelperToggle }
    private val leapAnnounce by BooleanSetting("Leap Announce", false, description = "Announces when you leap to a player.")

    private val EMPTY = DungeonPlayer("Empty", DungeonClass.Unknown)
    private val keybindList = listOf(topLeftKeybind, topRightKeybind, bottomLeftKeybind, bottomRightKeybind)


//    fun leapMenu() = UI {
//        Grid(copies()).scope {
//            leapTeammates.forEachIndexed { index, it ->
//                if (it == EMPTY) return@forEachIndexed
//                val x = when (index) {
//                    0, 2 -> 16.percent
//                    else -> 6.percent
//                }
//
//                val y = when (index) {
//                    0, 1 -> 38.percent
//                    else -> 10.percent
//                }
//                val sizeX = Animatable(from = 80.percent, to = 83.percent) // this keeps the starting x and y while enlarging the width and height which isnt what we want
//                val sizeY = Animatable(from = 50.percent, to = 53.percent)
//                group(size(50.percent, 50.percent)) {
//                    val block = block(
//                        constraints = constrain(x, y, sizeX, sizeY),
//                        color = `gray 38`,
//                        radius = 12.radius()
//                    ) {
//
//                        image(
//                            it.skinImage,
//                            constraints = constrain(5.percent, 10.percent, 30.percent, 80.percent),
//                            12f.radius()
//                        )
//                        column(constraints = constrain(38.percent, 40.percent)) {
//                            text(it.name, size = 20.percent, color = it.clazz.color)
//                            divider(5.percent)
//                            text(if (it.isDead) "§cDEAD" else it.clazz.name, size = 10.percent, color = Color.WHITE)
//                        }
//                    }
//                    onClick { // make it possible to click any mouse button
//                        handleMouseClick(index)
//                        true
//                    }
//                    onMouseEnter {
//                        sizeX.animate(0.25.seconds, Animations.EaseInOutQuint)
//                        sizeY.animate(0.25.seconds, Animations.EaseInOutQuint)
//                        block.outline(color = Color.WHITE)
//                        redraw()
//                    }
//                    onMouseExit {
//                        sizeX.animate(0.25.seconds, Animations.EaseInOutQuint)
//                        sizeY.animate(0.25.seconds, Animations.EaseInOutQuint)
//                        block.outline(color = Color.TRANSPARENT)
//                        redraw()
//                    }
//                }
//            }
//        }
//    }

    @SubscribeEvent
    fun guiOpen(event: GuiOpenEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap" || leapTeammates.isEmpty() || leapTeammates.all { it == EMPTY }) return
        if (Loader.instance().activeModList.any { it.modId == "notenoughupdates" }) NEUApi.setInventoryButtonsToDisabled()
    }

    private fun handleMouseClick(quadrant: Int) {
        if ((type.equalsOneOf(1,2,3)) && leapTeammates.size < quadrant) return

        val playerToLeap = leapTeammates[quadrant - 1]
        if (playerToLeap == EMPTY) return
        if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")
        modMessage("Teleporting to ${playerToLeap.name}.")
        //leapTo(playerToLeap.name, containerChest)
    }

    @SubscribeEvent
    fun keyTyped(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        val gui = event.gui as? GuiChest ?: return
        if (
            gui.inventorySlots !is ContainerChest ||
            gui.inventorySlots.name != "Spirit Leap" ||
            keybindList.none { it.isDown() } ||
            leapTeammates.isEmpty() ||
            !useNumberKeys
        ) return

        val index = keybindList.indexOfFirst { it.isDown() }
        val playerToLeap = if (index + 1 > leapTeammates.size) return else leapTeammates[index]
        if (playerToLeap == EMPTY) return
        if (playerToLeap.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, gui.inventorySlots as ContainerChest)

        event.isCanceled = true
    }

    private fun leapTo(name: String, containerChest: ContainerChest) {
        val index = getItemIndexInContainerChest(containerChest, name, 11..16) ?: return modMessage("Cant find player $name. This shouldn't be possible! are you nicked?")
        modMessage("Teleporting to $name.")
        if (leapAnnounce) partyMessage("Leaped to $name!")
        mc.playerController.windowClick(containerChest.windowId, index, 2, 3, mc.thePlayer)
    }

    init {
        onMessage(Regex(".*")) { leapHelperBossChatEvent(it) }

        onWorldLoad { worldLoad() }
    }

   /*private val leapTeammates: MutableList<DungeonPlayer> = mutableListOf(
        DungeonPlayer("Stivais", DungeonClass.Healer),
        DungeonPlayer("Odtheking", DungeonClass.Archer),
        DungeonPlayer("freebonsai", DungeonClass.Mage),
        DungeonPlayer("Cezar", DungeonClass.Tank)
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