package me.odinmain.features.impl.render

import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.clickgui.settings.impl.StringSetting
import me.odinmain.features.Module
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.ui.getTextWidth
import net.minecraft.client.gui.Gui
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color.getHSBColor

object DVD : Module(
    name = "DVD",
    description = "No further explanation."
) {
    private val boxWidth by NumberSetting("Box Width", 50, 0, 150, 1, desc = "Width of the DVD box.")
    private val boxHeight by NumberSetting("Box Height", 50, 0, 150, 1, desc = "Height of the DVD box.")

    private val speed by NumberSetting("Speed", 1f, 1, 3, .1, desc = "Speed of the DVD box.")
    private val text by StringSetting("Text", "ODVD", desc = "Text to display on the DVD box.")

    private var lastUpdateTime = System.nanoTime()
    private var color = Colors.WHITE.copy()
    private var x = 10
    private var y = 10
    private var dx = 1
    private var dy = 1

    override fun onEnable() {
        x = mc.displayWidth / 4
        y = mc.displayHeight / 4
        lastUpdateTime = System.nanoTime()
        super.onEnable()
    }

    private fun randomDVDColor(): Color {
        val javaColor = getHSBColor((Math.random() * 360).toFloat(), 1.0f, 0.5f)
        return Color(javaColor.red, javaColor.green, javaColor.blue)
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        updatePosition()
        Gui.drawRect(x, y, x + boxWidth, y + boxHeight, color.rgba)
        RenderUtils.drawText(text, x + boxWidth / 2f - getTextWidth(text) / 2f, y + boxHeight / 2f - 5, color, true)
    }

    private fun updatePosition() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0
        lastUpdateTime = currentTime

        val movement = speed * deltaTime * 200
        x += (dx * movement.toFloat()).toInt()
        y += (dy * movement.toFloat()).toInt()

        val screenWidth = mc.displayWidth / 2
        val screenHeight = mc.displayHeight / 2

        if (x <= 0) {
            x = 0
            dx = -dx
            color = randomDVDColor()
        } else if (x + boxWidth >= screenWidth) {
            x = (screenWidth - boxWidth)
            dx = -dx
            color = randomDVDColor()
        }

        if (y <= 0) {
            y = 0
            dy = -dy
            color = randomDVDColor()
        } else if (y + boxHeight >= screenHeight) {
            y = (screenHeight - boxHeight)
            dy = -dy
            color = randomDVDColor()
        }

        if ((x <= 0 || x + boxWidth >= screenWidth) && (y <= 0 || y + boxHeight >= screenHeight)) PlayerUtils.alert("$text has hit a corner!")
    }
}