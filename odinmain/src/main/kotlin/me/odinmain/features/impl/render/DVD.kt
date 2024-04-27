package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getMCTextHeight
import me.odinmain.utils.render.mcText
import me.odinmain.utils.render.roundedRectangle
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.Display
import java.awt.Color.getHSBColor

object DVD : Module(
    name = "DVD",
    category = Category.RENDER,
    description = "No further explanation."
) {
    private val boxWidth: Int by NumberSetting("Box Width", 50, 0, 150, 1, description = "Width of the DVD box.")
    private val boxHeight: Int by NumberSetting("Box Height", 50, 0, 150, 1, description = "Height of the DVD box.")

    private val text: String by StringSetting("Text", "DVD", description = "Text to display on the DVD box.")
    private val textScale: Float by NumberSetting("Text Scale", 1.5f, 0.1f, 2f, 0.1f, description = "Scale of the text.")

    private val speed: Long by NumberSetting("Speed", 10, 1, 20, 1, description = "Speed of the DVD box.")

    private var x = Display.getWidth() / 2
    private var y = Display.getHeight() / 2
    private var dx = 1
    private var dy = 1
    var color = Color.WHITE

    private fun getDVDColor() {
        val hue = (Math.random() * 360).toFloat()

        val javaColor = getHSBColor(hue, 1.0f, 0.5f)
        color = Color(javaColor.red, javaColor.green, javaColor.blue)
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent) {
        roundedRectangle(x, y, boxWidth, boxHeight, color, 9f)
        mcText(text, x + boxWidth / 2, y + boxHeight / 2 - getMCTextHeight() * textScale / 2 , textScale, color, true)
    }

    init {
        execute({ speed }) {
            x += dx
            y += dy
            val sr = ScaledResolution(mc)

            // Get screen dimensions
            val screenWidth = sr.scaledWidth
            val screenHeight = sr.scaledHeight

            // Check collision with screen edges
            if (x <= 0 || x + boxWidth >= screenWidth) {
                getDVDColor()
                dx = -dx // Reverse horizontal direction
            }
            if (y <= 0 || y + boxHeight >= screenHeight) {
                getDVDColor()
                dy = -dy // Reverse vertical direction
            }

            if ((x <= 0 || x + boxWidth >= screenWidth) && (y <= 0 || y + boxHeight >= screenHeight)) {
                modMessage("DVD hit a corner!")
                PlayerUtils.playLoudSound("note.pling", 100f, 1f)
            }
        }
    }
}