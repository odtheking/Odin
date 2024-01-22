package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.rectangleOutline
import me.odinmain.utils.render.gui.roundedRectangle
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RoundedRectangleTesting : Module(
    name = "Rounded Rectangle",
    category = Category.RENDER
) {
    private val x: Int by NumberSetting("x", 200, 0, 1920)
    private val y: Int by NumberSetting("y", 200, 0, 1080)
    private val w: Int by NumberSetting("width", 200, 10, 500)
    private val h: Int by NumberSetting("height", 200, 10, 500)
    private val color: Color by ColorSetting("Color", Color.WHITE, true)
    private val borderColor: Color by ColorSetting("Border Color", Color.GRAY, true)
    private val shadowColor: Color by ColorSetting("Shadow Color", Color.DARK_GRAY, true)
    private val borderThickness: Float by NumberSetting("Border Thickness", 1f, 0f, 10f)
    private val topL: Float by NumberSetting("Top Left", 5f, 0f, 50f)
    private val topR: Float by NumberSetting("Top Right", 5f, 0f, 50f)
    private val botL: Float by NumberSetting("Bottom Left", 5f, 0f, 50f)
    private val botR: Float by NumberSetting("Bottom Right", 5f, 0f, 50f)
    private val edgeSoftness: Float by NumberSetting("Edge Softness", 3f, 0f, 50f)


    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        roundedRectangle(x, y, w, h, color, borderColor, Color.GRAY, borderThickness, topL, topR, botL, botR, edgeSoftness)
        rectangleOutline(x.toFloat() + 100, y.toFloat(), w.toFloat(), h.toFloat(), borderColor, topL, borderThickness)
    }
}