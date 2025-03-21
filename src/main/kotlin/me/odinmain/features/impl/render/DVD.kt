package me.odinmain.features.impl.render

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.dsl.radius
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.regularFont
import me.odinmain.utils.ui.renderer.NVGRenderer
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.Display

object DVD : Module(
    name = "DVD",
    description = "No further explanation."
) {
    private val boxWidth by NumberSetting("Box Width", 50f, 10f, 150f, 1f, description = "Width of the DVD box.")
    private val boxHeight by NumberSetting("Box Height", 50f, 10f, 150f, 1f, description = "Height of the DVD box.")
    private val roundedCorners by NumberSetting("Rounded Corners", 12f, 0f, 50f, 1f, description = "Radius of the rounded corners.")
    private val speed by NumberSetting("Speed", 1.0f, 0.1f, 5.0f, 0.1f, description = "Speed of the DVD box.")
    private val text by StringSetting("Text", "ODVD", description = "Text to display on the DVD box.")
    private val textScale by NumberSetting("Text Scale", 16f, 10f, 64f, 1f, description = "Scale of the text.")

    private var lastUpdateTime = System.nanoTime()
    private var color = randomDVDColor()
    private var x = 0f
    private var y = 0f
    private var dx = 1
    private var dy = 1
    
    override fun onEnable() {
        x = Display.getWidth() / 4f
        y = Display.getHeight() / 4f
        lastUpdateTime = System.nanoTime()
        super.onEnable()
    }

    private fun randomDVDColor(): Color.HSB =
        Color.HSB((Math.random() * 360f).toFloat(), 1.0f, 0.8f)

    private fun updatePosition() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0
        lastUpdateTime = currentTime

        val movement = speed * deltaTime * 200
        x += dx * movement.toFloat()
        y += dy * movement.toFloat()

        val screenWidth = Display.getWidth()
        val screenHeight = Display.getHeight()

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

    @SubscribeEvent
    fun onRenderOverlay(event: RenderWorldLastEvent) {
        updatePosition()

        NVGRenderer.beginFrame(Display.getWidth().toFloat(), Display.getHeight().toFloat())
        NVGRenderer.rect(x, y, boxWidth, boxHeight, color.rgba, roundedCorners.radius())
        NVGRenderer.text(text, (x + boxWidth / 2) - NVGRenderer.textWidth(text, textScale, regularFont) / 2, (y + boxHeight / 2) - textScale / 2, textScale, Colors.WHITE.rgba, regularFont)
        NVGRenderer.endFrame()
    }
}
