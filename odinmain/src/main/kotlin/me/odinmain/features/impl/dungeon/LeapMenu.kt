package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.DrawGuiScreenEvent
import me.odinmain.events.impl.GuiClickEvent
import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.DungeonPlayer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.teammates
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.teammatesNoSelf
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemSkull
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui.",
    category = Category.DUNGEON
) {
    private val type: Int by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("A-Z Class (BetterMap)", "A-Z Name", "Odin Sorting"))
    private val colorStyle: Boolean by DualSetting("Color Style", "Gray", "Color", default = false, description = "What Click to use")
    private val leapHelperToggle: Boolean by BooleanSetting("Leap Helper", true)
    private val color: Color by ColorSetting("Leap Helper Color", default = Color.WHITE).withDependency { leapHelperToggle }
    private val delay: Int by NumberSetting("Delay", 30, 10.0, 120.0, 1.0)

    private val EMPTY = DungeonPlayer("Empty", Classes.Archer, ResourceLocation("textures/entity/steve.png"))
    private var leapTeammates = mutableListOf<DungeonPlayer>()
    private var leapHelper: String = ""


   /*private var teammates: List<DungeonPlayer> = listOf(
        DungeonPlayer("Bonzi", Classes.Mage, ResourceLocation("textures/entity/steve.png")),
        DungeonPlayer("OdthekingABCDFEGH", Classes.Archer, ResourceLocation("textures/entity/steve.png")),
        DungeonPlayer("CEzar", Classes.Tank, ResourceLocation("textures/entity/steve.png")),
        DungeonPlayer("Stiviaisd", Classes.Berserk, ResourceLocation("textures/entity/steve.png"))
    )*/

    @SubscribeEvent
    fun mouseClicked(event: GuiClickEvent) {
        if (event.gui !is GuiChest || event.gui.inventorySlots !is ContainerChest || (event.gui.inventorySlots as ContainerChest).name != "Spirit Leap")  return

        val quadrant = getQuadrant(event.x, event.y)

        if (leapTeammates.isEmpty()) return
        if ((type == 1 || type == 0) && leapTeammates.size < quadrant ) return

        val playerToLeap = leapTeammates[quadrant - 1]

        val index = event.gui.inventorySlots.inventorySlots.subList(11, 16)
            .indexOfFirst { it?.stack?.displayName?.noControlCodes == playerToLeap.name }
                .takeIf { it != -1 } ?: return
        modMessage("Teleporting to ${playerToLeap.name}.")
        if (playerToLeap.clazz == Classes.DEAD) return modMessage("This player is dead, can't leap.")
        mc.playerController.windowClick(event.gui.inventorySlots.windowId, 11 + index, 1, 2, mc.thePlayer)
        event.isCanceled = true
    }

    private fun fillPlayerList(players: List<DungeonPlayer>): Array<DungeonPlayer> {
        val sortedPlayers = players.sortedBy { it.clazz.prio }
        val result = Array(4) { EMPTY }
        val secondRound = mutableListOf<DungeonPlayer>()

        for (player in sortedPlayers) {
            if (result[player.clazz.defaultQuandrant] == EMPTY) result[player.clazz.defaultQuandrant] = player
            else secondRound.add(player)
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

        leapTeammates = when (type) {
            0 -> teammatesNoSelf.sortedWith(compareBy({ it.clazz.ordinal }, { it.name })).toMutableList()
            1 -> teammatesNoSelf.sortedBy { it.name }.toMutableList()
            else -> fillPlayerList(teammatesNoSelf).toMutableList()
        }
    }
    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        leapTeammates.clear()
    }

    @SubscribeEvent
    fun onDrawScreen(event: DrawGuiScreenEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap") return
        if (teammates.size <= 1) return

        val width = mc.displayWidth / 1920.0
        val height = mc.displayHeight / 1080.0
        val sr = ScaledResolution(mc)

        leapTeammates.forEachIndexed { index, it ->
            if (it == EMPTY) return@forEachIndexed
            GlStateManager.pushMatrix()
            GlStateManager.enableAlpha()
            GlStateManager.scale(width, height, 0.0)
            GlStateManager.scale(6.0 / sr.scaleFactor,  6.0 / sr.scaleFactor, 1.0)
            GlStateManager.color(255f, 255f, 255f, 255f)
            GlStateManager.translate(
                (30.0 + (index % 2 * 145.0)),
                (if (index >= 2) 120.0 else 40.0),
                0.0)
            mc.textureManager.bindTexture(it.locationSkin)
            if (it.name == leapHelper && leapHelperToggle) Gui.drawRect(-10, -20, 125, 40, color.rgba)
            Gui.drawRect(-5, -15, 120, 35, if (!colorStyle) Color.DARK_GRAY.rgba else it.clazz.color.rgba)

            GlStateManager.color(255f, 255f, 255f, 255f)
            Gui.drawScaledCustomSizeModalRect(0, -10, 8f, 8f, 8, 8, 40, 40, 64f, 64f)

            //if (it.name.length > 14) GlStateManager.scale(1.6 / sr.scaleFactor,  1.6 / sr.scaleFactor, 1.0)
            mc.fontRendererObj.drawString(it.name, if (it.name.length > 13) 52 else 42, 5, if (!colorStyle) it.clazz.color.rgba else Color.DARK_GRAY.rgba)

            mc.fontRendererObj.drawString(it.clazz.name, if (it.name.length > 13) 52 else 42, 16, Color.WHITE.rgba )

            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }
        event.isCanceled = true

    }
    private fun getQuadrant(mouseX: Int, mouseY: Int): Int {
        var guiSize = mc.gameSettings.guiScale * 2
        if (mc.gameSettings.guiScale == 0) guiSize = 10

        val screenY = mc.displayHeight / guiSize
        val screenX = mc.displayWidth / guiSize

        return when {
            mouseX >= screenX -> if (mouseY >= screenY) 4 else 2
            else -> if (mouseY >= screenY) 3 else 1
        }
    }

    private val keyRegex = Regex("(?:\\[(?:\\w+)] )?(\\w+) opened a (?:WITHER|Blood) door!")
    private val leapHelperClock = Clock(delay * 1000L)

    @SubscribeEvent
    fun onChat (event: ChatPacketEvent) {
        if(leapHelperClock.hasTimePassed()) leapHelper = ""
        leapHelper = keyRegex.find(event.message)?.groupValues?.get(1) ?: return
        leapHelperClock.update()
    }
}