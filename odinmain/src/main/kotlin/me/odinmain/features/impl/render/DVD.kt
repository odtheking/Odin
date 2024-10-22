package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.*
import me.odinmain.utils.render.RenderUtils.bind
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
    private val boxWidth by NumberSetting("Box Width", 50, 0, 150, 1, description = "Width of the DVD box.")
    private val boxHeight by NumberSetting("Box Height", 50, 0, 150, 1, description = "Height of the DVD box.")
    private val roundedCorners by BooleanSetting("Rounded Corners", true, description = "Whether the DVD box should have rounded corners.")

    private val text by StringSetting("Text", "ODVD", description = "Text to display on the DVD box.")
    private val textScale by NumberSetting("Text Scale", 1.5f, 0.1f, 2f, 0.1f, description = "Scale of the text.")

    override fun onEnable() {
        x = Display.getWidth() / 4
        y = Display.getHeight() / 4
        super.onEnable()
    }

    private var dx = 1
    private var dy = 1
    
    private var x = Display.getWidth() / 2
    private var y = Display.getHeight() / 2
    private var color = Color.WHITE

    private fun getDVDColor() {
        val hue = (Math.random() * 360).toFloat()

        val javaColor = getHSBColor(hue, 1.0f, 0.5f)
        color = Color(javaColor.red, javaColor.green, javaColor.blue)
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        updateDVD()
        roundedRectangle(x, y, boxWidth, boxHeight, color, if (roundedCorners) 12f else 0f)
        mcText(text, x + boxWidth / 2, y + boxHeight / 2 - getMCTextHeight() * textScale / 2 , textScale, color, true)
        Color.WHITE.bind()
    }

    private fun updateDVD() {
        x += dx
        y += dy
        val sr = ScaledResolution(mc)

        // Get screen dimensions
        val screenWidth = sr.scaledWidth
        val screenHeight = sr.scaledHeight

        // Check collision with screen edges
        if (x <= 0) {
            x = 0 // Reset x position to prevent going out of bounds
            getDVDColor()
            dx = -dx // Reverse horizontal direction
        } else if (x + boxWidth >= screenWidth) {
            x = screenWidth - boxWidth // Adjust x position to prevent going out of bounds
            getDVDColor()
            dx = -dx // Reverse horizontal direction
        }

        if (y <= 0) {
            y = 0 // Reset y position to prevent going out of bounds
            getDVDColor()
            dy = -dy // Reverse vertical direction
        } else if (y + boxHeight >= screenHeight) {
            y = screenHeight - boxHeight // Adjust y position to prevent going out of bounds
            getDVDColor()
            dy = -dy // Reverse vertical direction
        }

        // Check collision with corners
        if ((x <= 0 || x + boxWidth >= screenWidth) && (y <= 0 || y + boxHeight >= screenHeight)) {
            modMessage("$text hit a corner!")
            PlayerUtils.playLoudSound("note.pling", 100f, 1f)
        }
    }
}