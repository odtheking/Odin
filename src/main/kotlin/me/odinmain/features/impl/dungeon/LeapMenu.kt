package me.odinmain.features.impl.dungeon

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.Layout.Companion.divider
import com.github.stivais.aurora.elements.impl.Block.Companion.outline
import com.github.stivais.aurora.elements.impl.layout.Grid
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
import me.odinmain.utils.ui.renderer.NVGRenderer
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import me.odinmain.features.impl.render.ClickGUI.`gray 38`

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

//    fun leapMenu() = Aurora(renderer = NVGRenderer) {
//        Grid(copies()).scope {
//            leapTeammates.forEachIndexed { index, it ->
//                if (it == EMPTY) return@forEachIndexed
//                group {
//                    val block = block(
//                        constraints = constrain(0.px, 0.px, 50.percent, 50.percent),
//                        color = `gray 38`,
//                        radius = 12.radius()
//                    ) {
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
//                    onClick(nonSpecific = true) {
//                        if (type != 0 && leapTeammates.size < index) return@onClick
//
//                        val playerToLeap = leapTeammates[index - 1]
//                        if (playerToLeap == EMPTY) return@onClick
//                        if (playerToLeap.isDead) return@onClick modMessage("This player is dead, can't leap.")
//                        modMessage("Teleporting to ${playerToLeap.name}.")
//                        leapTo(playerToLeap.name)
//                        true
//                    }
//                    onKeycodePressed { (code) ->
//                        if (!useNumberKeys || leapTeammates.isEmpty()) return@onKeycodePressed false
//
//                        val keybindIndex = listOf(topLeftKeybind, topRightKeybind, bottomLeftKeybind, bottomRightKeybind).indexOfFirst { it.key == code }.takeIf { it != -1 } ?: return@onKeycodePressed false
//                        if (keybindIndex >= leapTeammates.size) return@onKeycodePressed false
//
//                        val playerToLeap = leapTeammates[keybindIndex]
//                        if (playerToLeap == EMPTY || playerToLeap.isDead) {
//                            modMessage("This player is dead, can't leap.")
//                            return@onKeycodePressed false
//                        }
//
//                        leapTo(playerToLeap.name)
//                        true
//                    }
//                    onMouseEnter {
//                        block.outline(color = Color.WHITE, thickness = 2.px)
//                        redraw()
//                    }
//                    onMouseExit {
//                        block.outline(color = Color.TRANSPARENT, thickness = 0.px)
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

    private fun leapTo(name: String) {
        val containerChest = mc.thePlayer?.openContainer as? ContainerChest ?: return modMessage("You need to be in the leap menu to leap.")
        val index = getItemIndexInContainerChest(containerChest, name, 11..16) ?: return modMessage("Can't find player $name. This shouldn't be possible! are you nicked?")
        modMessage("Teleporting to $name.")
        if (leapAnnounce) partyMessage("Leaped to $name!")
        mc.playerController.windowClick(containerChest.windowId, index, 2, 3, mc.thePlayer)
    }

    init {
        onMessage(Regex(".*")) { leapHelperBossChatEvent(it) }

        onWorldLoad { worldLoad() }
    }

   private val leapTeammates: MutableList<DungeonPlayer> = mutableListOf(
        DungeonPlayer("Stivais", DungeonClass.Healer),
        DungeonPlayer("Odtheking", DungeonClass.Archer),
        DungeonPlayer("freebonsai", DungeonClass.Mage),
        DungeonPlayer("Cezar", DungeonClass.Tank)
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