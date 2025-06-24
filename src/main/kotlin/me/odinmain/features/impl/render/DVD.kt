package me.odinmain.features.impl.render

import com.github.stivais.aurora.color.Color
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import org.lwjgl.opengl.Display

object DVD : Module(
    name = "DVD",
    description = "No further explanation."
) {
    private val boxWidth by NumberSetting("Box Width", 50f, 0, 150, 1, description = "Width of the DVD box.")
    private val boxHeight by NumberSetting("Box Height", 50f, 0, 150, 1, description = "Height of the DVD box.")
    private val roundedCorners by BooleanSetting("Rounded Corners", true, description = "Whether the DVD box should have rounded corners.")

    private val speed by NumberSetting("Speed", 1, .1, 2, .1, description = "Speed of the DVD box.")
    private val text by StringSetting("Text", "ODVD", description = "Text to display on the DVD box.")
    private val textScale by NumberSetting("Text Scale", 1.5f, 0.1f, 2f, 0.1f, description = "Scale of the text.")

    override fun onEnable() {
        x = Display.getWidth() / 4f
        y = Display.getHeight() / 4f
        super.onEnable()
    }

    init {
        execute( { speed * 10L } ) {
            updateDVD()
        }
    }

    private var dx = 1
    private var dy = 1
    
    private var x = Display.getWidth() / 2f
    private var y = Display.getHeight() / 2f
    private var color = Color.HSB((Math.random() * 360).toFloat(), 1.0f, 0.5f)

    fun getDVDColor(): Color.HSB = Color.HSB((Math.random() * 360).toFloat(), 1.0f, 0.5f)


    // THIS SHOULD/WILL BE THE ONLY "HUD" TO NOT USE HUD SYSTEM (and directly renders).
    // I am not redoing it just so it can support this single joke module.

//    @SubscribeEvent
//    fun onRenderOverlay(event: RenderOverlayNoCaching) {
//        NVGRenderer.beginFrame(Display.getWidth().toFloat(), Display.getHeight().toFloat())
//        NVGRenderer.rect(x, y, boxWidth, boxHeight, color.rgba, (if (roundedCorners) 12f else 0f).radius())
//        NVGRenderer.endFrame()
//    }

    private fun updateDVD() {
        x += dx
        y += dy

        // Get screen dimensions
        val screenWidth = Display.getWidth()
        val screenHeight = Display.getHeight()

        // Check collision with screen edges
        if (x <= 0) {
            x = 0f // Reset x position to prevent going out of bounds
            color = getDVDColor()
            dx = -dx // Reverse horizontal direction
        } else if (x + boxWidth >= screenWidth) {
            x = screenWidth - boxWidth // Adjust x position to prevent going out of bounds
            color = getDVDColor()
            dx = -dx // Reverse horizontal direction
        }

        if (y <= 0) {
            y = 0f // Reset y position to prevent going out of bounds
            color = getDVDColor()
            dy = -dy // Reverse vertical direction
        } else if (y + boxHeight >= screenHeight) {
            y = screenHeight - boxHeight // Adjust y position to prevent going out of bounds
            color = getDVDColor()
            dy = -dy // Reverse vertical direction
        }

        // Check collision with corners
        if ((x <= 0 || x + boxWidth >= screenWidth) && (y <= 0 || y + boxHeight >= screenHeight)) {
            modMessage("$text hit a corner!")
            PlayerUtils.playLoudSound("note.pling", 100f, 1f)
        }
    }
}