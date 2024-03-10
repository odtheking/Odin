package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.font.OdinFont
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.addVec
import me.odinmain.utils.min
import me.odinmain.utils.render.RenderUtils.drawBeaconBeam
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toAABB
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.Display
import kotlin.math.max

object Renderer {

    /**
     * Draws a box in the world with the specified axis-aligned bounding box (AABB), color, and optional parameters.
     *
     * @param aabb         The axis-aligned bounding box defining the box.
     * @param color        The color of the box.
     * @param outlineWidth The width of the outline (default is 3).
     * @param outlineAlpha The alpha value of the outline (default is 1).
     * @param fillAlpha    The alpha value of the fill (default is 1).
     * @param depth        Indicates whether to draw with depth (default is true).
     */
    fun drawBox(
        aabb: AxisAlignedBB,
        color: Color,
        outlineWidth: Number = 3,
        outlineAlpha: Number = 1,
        fillAlpha: Number = 1,
        depth: Boolean = false
    ) {
        RenderUtils.drawOutlinedAABB(aabb, color.withAlpha(outlineAlpha.toFloat()), depth = depth)

        RenderUtils.drawFilledAABB(aabb, color.withAlpha(fillAlpha.toFloat()), depth = depth, outlineWidth = outlineWidth)
    }

    /**
     * Draws a 3D line between two specified points in the world.
     *
     * @param pos1      The starting position of the line.
     * @param pos2      The ending position of the line.
     * @param color     The color of the line.
     * @param lineWidth The width of the line (default is 3).
     * @param depth     Indicates whether to draw with depth (default is false).
     */
    fun draw3DLine(pos1: Vec3, pos2: Vec3, color: Color, lineWidth: Int = 3, depth: Boolean = false) {
        RenderUtils.draw3DLine(pos1, pos2, color, lineWidth, depth)
    }

    /**
     * Draws a custom beacon with specified title, position, color, and optional parameters.
     *
     * @param title    The title of the beacon.
     * @param vec3     The position of the beacon.
     * @param color    The color of the beacon.
     * @param beacon   Indicates whether to draw the beacon (default is true).
     * @param increase Indicates whether to increase the scale based on distance (default is true).
     * @param noFade   Indicates whether the beacon should not fade based on distance (default is false).
     * @param distance Indicates whether to display the distance in the title (default is true).
     */
    fun drawCustomBeacon(title: String, vec3: Vec3, color: Color, beacon: Boolean = true, increase: Boolean = true, noFade: Boolean = false, distance: Boolean = true) {
        val dist = vec3.distanceTo(mc.thePlayer.positionVector)
        drawBox(aabb = vec3.toAABB(), color = color, fillAlpha = 0f, depth = false)

        RenderUtils.drawStringInWorld(
            if (distance) "$title §r§f(§3${dist.toInt()}m§f)" else title,
            vec3.addVec(0.5, 1.7 + dist / 30, 0.5),
            color = color,
            shadow = true,
            scale = if (increase) max(0.03, dist / 200.0).toFloat() else 0.06f,
            depthTest = false
        )

        val alpha = if (noFade) 1f else min(1f, max(0f, dist.toFloat()) / 60f)
        modMessage("Alpha: $alpha, noFade: $noFade")
        if (beacon) drawBeaconBeam(vec3, color.withAlpha(alpha), depth = false)
    }

    /**
     * Draws text in the world at the specified position with the specified color and optional parameters.
     *
     * @param text            The text to be drawn.
     * @param vec3            The position to draw the text.
     * @param color           The color of the text.
     * @param renderBlackBox  Indicates whether to render a black box behind the text (default is false).
     * @param depth           Indicates whether to draw with depth (default is true).
     * @param scale           The scale of the text (default is 0.03).
     * @param shadow          Indicates whether to render a shadow for the text (default is true).
     */
    fun drawStringInWorld(
        text: String,
        vec3: Vec3,
        color: Color,
        renderBlackBox: Boolean = false,
        depth: Boolean = true,
        scale: Float = 0.03f,
        shadow: Boolean = true
        ) {
        RenderUtils.drawStringInWorld(text, vec3, color, renderBlackBox, depth, scale, shadow)
    }

    /**
     * Draws a cylinder in the world with the specified parameters.
     *
     * @param pos         The position of the cylinder.
     * @param baseRadius  The radius of the base of the cylinder.
     * @param topRadius   The radius of the top of the cylinder.
     * @param height      The height of the cylinder.
     * @param slices      The number of slices for the cylinder.
     * @param stacks      The number of stacks for the cylinder.
     * @param rot1        Rotation parameter.
     * @param rot2        Rotation parameter.
     * @param rot3        Rotation parameter.
     * @param color       The color of the cylinder.
     * @param phase       Indicates whether to phase the cylinder (default is false).
     * @param linemode    Indicates whether to draw the cylinder in line mode (default is false).
     */
    fun drawCylinder(
        pos: Vec3, baseRadius: Float, topRadius: Float, height: Float,
        slices: Int, stacks: Int, rot1: Float, rot2: Float, rot3: Float,
        color: Color, phase: Boolean = false, linemode: Boolean = false
    ) {
        RenderUtils.drawCylinder(pos, baseRadius, topRadius, height, slices, stacks, rot1, rot2, rot3, color, linemode, phase)
    }

    private var displayTitle = ""
    private var titleTicks = 0
    private var displayColor = Color.WHITE

    fun displayTitle(title: String, ticks: Int, color: Color = Color.WHITE) {
        displayTitle = title
        titleTicks = ticks
        displayColor = color

        runIn(ticks) {
            clearTitle()
        }
    }

    private fun clearTitle() {
        displayTitle = ""
        titleTicks = 0
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        mc.entityRenderer.setupOverlayRendering()

        scale(1f / scaleFactor, 1f / scaleFactor, 1f)

        text(text = displayTitle, x = (Display.getWidth() / 2f) - (OdinFont.getTextWidth(displayTitle, 50f) /2f), y = Display.getHeight() / 2f, color = displayColor, size = 50f, shadow = true)
        scale(scaleFactor, scaleFactor, 1f)
    }

    @SubscribeEvent
    fun worldLoad(event: WorldEvent.Load) {
        clearTitle()
    }
}