package me.odinmain.features.impl.dungeon

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.constraints.impl.measurements.Undefined
import com.github.stivais.aurora.constraints.impl.positions.Center
import com.github.stivais.aurora.constraints.impl.size.AspectRatio
import com.github.stivais.aurora.constraints.impl.size.Copying
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.Layout.Companion.divider
import com.github.stivais.aurora.elements.impl.Block.Companion.outline
import com.github.stivais.aurora.elements.impl.Shadow
import com.github.stivais.aurora.elements.impl.layout.Grid
import com.github.stivais.aurora.utils.color
import com.github.stivais.aurora.utils.loop
import com.github.stivais.aurora.utils.withAlpha
import io.github.moulberry.notenoughupdates.NEUApi
import me.odinmain.features.Module
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonClass
import me.odinmain.utils.skyblock.dungeon.DungeonPlayer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonTeammatesNoSelf
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.renderer.NVGRenderer
import me.odinmain.utils.ui.screens.UIChest
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui."
) {
    private val type by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("Odin Sorting", "A-Z Class (BetterMap)", "A-Z Name", "Custom sorting", "No Sorting"), description = "How to sort the leap menu.")
    private val onlyClass by BooleanSetting("Only Classes", description = "Renders classes instead of names.")
    private val menuStyle by SelectorSetting("Menu Style", "Default", arrayListOf("Default", "Colored"), description = "The style of the leap menu.")
    private val backgroundAlpha by NumberSetting("Background Alpha", 0.85f, 0.1f, 1f, 0.1f, description = "Alpha for the background of the leap menu.")
    private val useNumberKeys by BooleanSetting("Use Number Keys", description = "Use keyboard keys to leap to the player you want, going from left to right, top to bottom.")
    private val topLeftKeybind by KeybindSetting("Top Left", Keyboard.KEY_1, "Used to click on the first person in the leap menu.").withDependency { useNumberKeys }
    private val topRightKeybind by KeybindSetting("Top Right", Keyboard.KEY_2, "Used to click on the second person in the leap menu.").withDependency { useNumberKeys }
    private val bottomLeftKeybind by KeybindSetting("Bottom Left", Keyboard.KEY_3, "Used to click on the third person in the leap menu.").withDependency { useNumberKeys }
    private val bottomRightKeybind by KeybindSetting("Bottom right", Keyboard.KEY_4, "Used to click on the fourth person in the leap menu.").withDependency { useNumberKeys }
    private val leapAnnounce by BooleanSetting("Leap Announce", description = "Announces when you leap to a player.")

    private val EMPTY = DungeonPlayer("Empty", DungeonClass.Unknown)

    private fun leapMenu(sortedTeammates: ArrayList<DungeonPlayer>) = Aurora(renderer = NVGRenderer) {
        Grid(copies()).scope {
            sortedTeammates.loop { teammate ->
                group(size(50.percent, 50.percent)) {
                    if (teammate == EMPTY) return@loop // ensures we don't render empty player

                    val block = block(
                        constraints = constrain(Center, Center, AspectRatio(2.5f), 60.percent),
                        color = if (menuStyle == 0) `gray 38`.withAlpha(backgroundAlpha) else color { teammate.clazz.color.withAlpha(backgroundAlpha).rgba },
                        radius = 16.radius()
                    ) {
                        Shadow(
                            copies(),
                            blur = 5f,
                            spread = 2f,
                            radii = 10.radius(),
                        ).add()
                        row(copies(), padding = 5.percent) {
                            divider(5.percent)
                            image(
                                teammate.skinImage,
                                constraints = constrain(y = Center, w = 30.percent, h = AspectRatio(1f)),
                                16f.radius()
                            )
                            column(constrain(Undefined, Center, 70.percent, Copying), padding = 5.percent) {
                                divider(40.percent)
                                if (!onlyClass) text(teammate.name, color = if (menuStyle == 0) color { teammate.clazz.color.rgba } else `gray 38`.withAlpha(backgroundAlpha) , size = 20.percent)
                                text(if (teammate.isDead) "DEAD" else teammate.clazz.name, color = if (teammate.isDead) Colors.MINECRAFT_RED else Colors.WHITE, size = 12.percent)
                            }
                        }
                        outline(`gray 38`, 2.px)
                    }
                    onClick(nonSpecific = true) {
                        if (teammate.isDead) return@onClick modMessage("§cThis player is dead, can't leap.").let { false }
                        leapTo(teammate.name)
                    }
                    onMouseEnter {
                        block.outline(Color.WHITE, 2.px)
                    }
                    onMouseExit {
                        block.outline(`gray 38`, 2.px)
                    }
                }
            }
        }

        onKeycodePressed { (code) -> // not good
            if (!useNumberKeys || sortedTeammates.isEmpty()) return@onKeycodePressed false

            val keybindIndex = listOf(topLeftKeybind, topRightKeybind, bottomLeftKeybind, bottomRightKeybind).indexOfFirst { it.key == code }.takeIf { it != -1 } ?: return@onKeycodePressed false
            if (keybindIndex >= sortedTeammates.size) return@onKeycodePressed false

            val playerToLeap = sortedTeammates[keybindIndex]
            if (playerToLeap == EMPTY || playerToLeap.isDead) return@onKeycodePressed modMessage("§cThis player is dead, can't leap.").let { false }

            leapTo(playerToLeap.name)
        }
    }

    @SubscribeEvent
    fun guiOpen(event: GuiOpenEvent) {
        (event.gui as? GuiChest)?.takeIf { it.inventorySlots?.name == "Spirit Leap" } ?: return
        if (Loader.instance().activeModList.any { it.modId == "notenoughupdates" }) NEUApi.setInventoryButtonsToDisabled()

        UIChest(leapMenu(ArrayList(getSortedLeapList()).ifEmpty { return })).open(false)
    }

    private fun leapTo(name: String): Boolean {
        val containerChest = mc.thePlayer?.openContainer as? ContainerChest ?: return modMessage("§cYou need to be in the leap menu to leap.").let { false }
        val index = getItemIndexInContainerChest(containerChest, name, 11..16) ?: return modMessage("§cCan't find player $name. This shouldn't be possible! are you nicked?").let { false }
        mc.playerController?.windowClick(containerChest.windowId, index, 2, 3, mc.thePlayer) ?: return false
        if (leapAnnounce) partyMessage("Leaped to $name!")
        modMessage("Teleporting to $name.")
        return true
    }

    /**
     * Sorts the list of players based on their default quadrant and class priority.
     * The function first tries to place each player in their default quadrant. If the quadrant is already occupied,
     * the player is added to a second round list. After all players have been processed, the function fills the remaining
     * empty quadrants with the players from the second round list.
     *
     * @param players The list of players to be sorted.
     * @return An array of sorted players.
     */
    private fun odinSorting(players: List<DungeonPlayer>): List<DungeonPlayer> {
        val secondRound = mutableListOf<DungeonPlayer>()
        val result = MutableList(4) { EMPTY }

        for (player in players.sortedBy { it.clazz.priority }) {
            if (result[player.clazz.defaultQuadrant] == EMPTY) result[player.clazz.defaultQuadrant] = player
            else secondRound.add(player)
        }

        if (secondRound.isEmpty()) return result

        result.forEachIndexed { index, _ ->
            if (result[index] != EMPTY) return@forEachIndexed
            result[index] = secondRound.removeAt(0)
            if (secondRound.isEmpty()) return result
        }
        return result
    }

    private val theGoats = mutableListOf(
        DungeonPlayer("Stivais", DungeonClass.Healer), DungeonPlayer("Odtheking", DungeonClass.Archer),
        DungeonPlayer("freebonsai", DungeonClass.Berserk), DungeonPlayer("Cezar", DungeonClass.Tank)
    )
    var customLeapOrder: List<String> = emptyList()

    private fun getSortedLeapList(): List<DungeonPlayer> {
        return if (LocationUtils.currentArea.isArea(Island.SinglePlayer)) odinSorting(theGoats)
        else when (type) {
            0 -> odinSorting(dungeonTeammatesNoSelf.sortedBy { it.clazz.priority })
            1 -> dungeonTeammatesNoSelf.sortedWith(compareBy({ it.clazz.ordinal }, { it.name }))
            2 -> dungeonTeammatesNoSelf.sortedBy { it.name }
            3 -> dungeonTeammatesNoSelf.sortedBy { customLeapOrder.indexOf(it.name.lowercase()).takeIf { it != -1 } ?: Int.MAX_VALUE }
            else -> dungeonTeammatesNoSelf
        }
    }
}