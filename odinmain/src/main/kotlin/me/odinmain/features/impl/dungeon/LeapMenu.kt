package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.DrawGuiScreenEvent
import me.odinmain.events.impl.GuiClickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.EMPTY
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.leapTeammates
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.teammatesNoSelf
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui.",
    category = Category.DUNGEON
) {
    val type: Int by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("A-Z Class (BetterMap)", "A-Z Name", "Odin Sorting"))
    private val colorStyle: Boolean by DualSetting("Color Style", "Gray", "Color", default = false, description = "What Click to use")
    private val leapHelperToggle: Boolean by BooleanSetting("Leap Helper", true)
    private val color: Color by ColorSetting("Leap Helper Color", default = Color.WHITE).withDependency { leapHelperToggle }
    private val delay: Int by NumberSetting("Delay", 30, 10.0, 120.0, 1.0)

    private var leapHelper: String = ""

    @SubscribeEvent
    fun onDrawScreen(event: DrawGuiScreenEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap" || teammatesNoSelf.isEmpty()) return

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

            mc.fontRendererObj.drawString(it.name, if (it.name.length > 13) 52 else 42, 5, if (!colorStyle) it.clazz.color.rgba else Color.DARK_GRAY.rgba)
            mc.fontRendererObj.drawString(it.clazz.name, if (it.name.length > 13) 52 else 42, 16, Color.WHITE.rgba )

            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }
        event.isCanceled = true
    }

    @SubscribeEvent
    fun mouseClicked(event: GuiClickEvent) {
        if (event.gui !is GuiChest || event.gui.inventorySlots !is ContainerChest || (event.gui.inventorySlots as ContainerChest).name != "Spirit Leap")  return

        val quadrant = getQuadrant(event.x, event.y)

        if (leapTeammates.isEmpty()) return
        if ((type == 1 || type == 0) && leapTeammates.size < quadrant) return

        val playerToLeap = leapTeammates[quadrant - 1]

        if (playerToLeap.clazz == Classes.DEAD) return modMessage("This player is dead, can't leap.")

        val index = event.gui.inventorySlots.inventorySlots.subList(11, 16)
            .indexOfFirst { it?.stack?.displayName?.noControlCodes == playerToLeap.name }
                .takeIf { it != -1 } ?: return
        modMessage("Teleporting to ${playerToLeap.name}.")

        mc.playerController.windowClick(event.gui.inventorySlots.windowId, 11 + index, 1, 2, mc.thePlayer)
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