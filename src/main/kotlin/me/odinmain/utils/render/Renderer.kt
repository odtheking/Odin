package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.utils.addVec
import me.odinmain.utils.fastEyeHeight
import me.odinmain.utils.render.RenderUtils.drawBeaconBeam
import me.odinmain.utils.render.RenderUtils.outlineBounds
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.toAABB
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.multiplyAlpha
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.max
import kotlin.math.min

object Renderer {

    const val DEFAULT_STYLE = "Outline"
    val styles = arrayListOf("Filled", "Outline", "Filled Outline")
    const val STYLE_DESCRIPTION = "How the box should be rendered."

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
        depth: Boolean = false,
        lineSmoothing: Boolean = true
    ) {
        if (outlineAlpha == 0f && fillAlpha == 0f) return
        RenderUtils.drawOutlinedAABB(aabb, color.withAlpha(outlineAlpha.toFloat()), thickness = outlineWidth, depth = depth, lineSmoothing)

        RenderUtils.drawFilledAABB(aabb, color.withAlpha(fillAlpha.toFloat()), depth = depth)
    }

    /**
     * Draws a block in the world with the specified position, color, and optional parameters.
     *
     * @param pos          The position of the block.
     * @param color        The color of the block.
     * @param outlineWidth The width of the outline (default is 3).
     * @param outlineAlpha The alpha value of the outline (default is 1).
     * @param fillAlpha    The alpha value of the fill (default is 1).
     * @param depth        Indicates whether to draw with depth (default is false).
     */
    fun drawBlock(
        pos: BlockPos,
        color: Color,
        outlineWidth: Number = 3,
        outlineAlpha: Number = 1,
        fillAlpha: Number = 1,
        depth: Boolean = false,
        lineSmoothing: Boolean = true,
        expand: Double = 0.0
    ) {
        val block = getBlockAt(pos)
        block.setBlockBoundsBasedOnState(mc.theWorld, pos)
        drawBox(block.getSelectedBoundingBox(mc.theWorld, pos).outlineBounds().expand(expand, expand, expand), color, outlineWidth, outlineAlpha, fillAlpha, depth, lineSmoothing)
    }

    fun drawStyledBlock(
        pos: BlockPos,
        color: Color,
        style: Int,
        width: Number = 3,
        depth: Boolean = false,
        lineSmoothing: Boolean = true,
        expand: Double = 0.0
    ) {
        when (style) {
            0 -> drawBlock(pos, color, width, 0, color.alphaFloat, depth, lineSmoothing, expand)
            1 -> drawBlock(pos, color, width, color.alphaFloat, 0, depth, lineSmoothing, expand)
            2 -> drawBlock(pos, color, width, color.alphaFloat, color.multiplyAlpha(.75f).alphaFloat, depth, lineSmoothing, expand)
        }
    }

    fun drawStyledBox(
        aabb: AxisAlignedBB,
        color: Color,
        style: Int,
        width: Number = 3,
        depth: Boolean = false
    ) {
        when (style) {
            0 -> drawBox(aabb, color, width, 0, color.alphaFloat, depth)
            1 -> drawBox(aabb, color, width, color.alphaFloat, 0, depth)
            2 -> drawBox(aabb, color, width, color.alphaFloat, color.multiplyAlpha(.75f).alphaFloat, depth)
        }
    }

    /**
     * Draws a 3D line between two specified points in the world.
     *
     * @param points    The points to draw the line between.
     * @param color     The color of the line.
     * @param lineWidth The width of the line (default is 3).
     * @param depth     Indicates whether to draw with depth (default is false).
     */
    fun draw3DLine(points: Collection<Vec3>, color: Color, lineWidth: Float = 3f, depth: Boolean = false) {
        RenderUtils.drawLines(points, color = color, lineWidth = lineWidth, depth = depth)
    }

    /**
     * Draws a 3D line between two specified points in the world.
     *
     * @param goal       The end point of the line.
     * @param color     The color of the line.
     * @param lineWidth The width of the line (default is 3).
     * @param depth     Indicates whether to draw with depth (default is false).
     */
    fun drawTracer(goal: Vec3, color: Color, lineWidth: Float = 3f, depth: Boolean = false) {
        RenderUtils.drawLines(listOf(mc.thePlayer.renderVec.addVec(y = fastEyeHeight()), goal), color, lineWidth, depth)
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
    fun drawCustomBeacon(title: String, vec3: Vec3, color: Color, beacon: Boolean = true, increase: Boolean = true, noFade: Boolean = false, distance: Boolean = true, style: Int = 1) {
        val dist = vec3.distanceTo(mc.thePlayer.positionVector)
        drawStyledBox(vec3.toAABB(), color, depth = false, style = style)

        RenderUtils.drawStringInWorld(
            if (distance) "$title §r§f(§3${dist.toInt()}m§f)" else title,
            vec3.addVec(0.5, 1.7 + dist / 30, 0.5),
            color = color, shadow = true,
            scale = if (increase) max(0.03, dist / 200.0).toFloat() else 0.06f,
            depthTest = false
        )

        val alpha = if (noFade) 1f else min(1f, max(0f, dist.toFloat()) / 60f)
        if (beacon) drawBeaconBeam(vec3, color.withAlpha(alpha), depth = true)
    }

    /**
     * Draws text in the world at the specified position with the specified color and optional parameters.
     *
     * @param text            The text to be drawn.
     * @param vec3            The position to draw the text.
     * @param color           The color of the text.
     * @param depth           Indicates whether to draw with depth (default is true).
     * @param scale           The scale of the text (default is 0.03).
     * @param shadow          Indicates whether to render a shadow for the text (default is true).
     */
    fun drawStringInWorld(
        text: String,
        vec3: Vec3,
        color: Color = Colors.WHITE,
        depth: Boolean = false,
        scale: Float = 0.03f,
        shadow: Boolean = true
    ) {
        RenderUtils.drawStringInWorld(text, vec3, color, depth, scale, shadow)
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
     * @param depth       Indicates whether to phase the cylinder (default is false).
     */
    fun drawCylinder(
        pos: Vec3, baseRadius: Number, topRadius: Number, height: Number,
        slices: Number, stacks: Number, rot1: Number, rot2: Number, rot3: Number,
        color: Color, depth: Boolean = false
    ) {
        RenderUtils.drawCylinder(pos, baseRadius, topRadius, height, slices, stacks, rot1, rot2, rot3, color, depth)
    }

    fun draw2DEntity(entity: Entity, color: Color, lineWidth: Float) {
       RenderUtils2D.draw2DESP(entity.entityBoundingBox, color, lineWidth)
    }

    private var displayTitle = ""
    private var titleTicks = 0
    private var displayColor = Colors.WHITE

    fun displayTitle(title: String, ticks: Int, color: Color = Colors.WHITE) {
        displayTitle = title
        titleTicks = ticks
        displayColor = color
    }

    private fun clearTitle() {
        displayTitle = ""
        titleTicks = 0
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || titleTicks <= 0) return
        mc.entityRenderer.setupOverlayRendering()
        val sr = ScaledResolution(mc)

        RenderUtils.drawText(
            text = displayTitle, x = sr.scaledWidth / 2f,
            y = sr.scaledHeight / 2.5f, scale = 4f,
            color = displayColor, center = true
        )
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        titleTicks--
    }

    @SubscribeEvent
    fun worldLoad(event: WorldEvent.Load) {
        clearTitle()
    }
}