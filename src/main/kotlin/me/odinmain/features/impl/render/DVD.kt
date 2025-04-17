package me.odinmain.features.impl.render

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.getMCTextHeight
import me.odinmain.utils.render.roundedRectangle
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.ui.Colors
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.Display
import java.awt.Color.getHSBColor

object DVD : Module(
    name = "DVD",
    desc = "No further explanation."
) {
    private val boxWidth by NumberSetting("Box Width", 50f, 0, 150, 1, desc = "Width of the DVD box.")
    private val boxHeight by NumberSetting("Box Height", 50f, 0, 150, 1, desc = "Height of the DVD box.")
    private val roundedCorners by BooleanSetting("Rounded Corners", true, desc = "Whether the DVD box should have rounded corners.")

    private val speed by NumberSetting("Speed", 1, .1, 2, .1, desc = "Speed of the DVD box.")
    private val text by StringSetting("Text", "ODVD", desc = "Text to display on the DVD box.")
    private val textScale by NumberSetting("Text Scale", 1.5f, 0.1, 2, 0.1, desc = "Scale of the text.")

    private var lastUpdateTime = System.nanoTime()
    private var color = Colors.WHITE.copy()
    private var x = 10f
    private var y = 10f
    private var dx = 1
    private var dy = 1

    override fun onEnable() {
        x = Display.getWidth() / 4f
        y = Display.getHeight() / 4f
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
        roundedRectangle(x, y, boxWidth, boxHeight, color, if (roundedCorners) 12f else 0f)
        RenderUtils.drawText(text, x + boxWidth / 2, y + boxHeight / 2f - getMCTextHeight() * textScale / 2f , textScale, color, true, center = true)
    }

    private fun updatePosition() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0
        lastUpdateTime = currentTime

        val movement = speed * deltaTime * 200
        x += dx * movement.toFloat()
        y += dy * movement.toFloat()

        val screenWidth = Display.getWidth() / 2
        val screenHeight = Display.getHeight() / 2

        if (x <= 0) {
            x = 0f
            dx = -dx
            color = randomDVDColor()
        } else if (x + boxWidth >= screenWidth) {
            x = (screenWidth - boxWidth)
            dx = -dx
            color = randomDVDColor()
        }

        if (y <= 0) {
            y = 0f
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