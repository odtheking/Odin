package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.font.OdinFont
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.addVec
import me.odinmain.utils.min
import me.odinmain.utils.render.RenderUtils.bind
import me.odinmain.utils.render.RenderUtils.drawBeaconBeam
import me.odinmain.utils.render.RenderUtils.postDraw
import me.odinmain.utils.render.RenderUtils.preDraw
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.RenderUtils.tessellator
import me.odinmain.utils.render.RenderUtils.worldRenderer
import me.odinmain.utils.runIn
import me.odinmain.utils.toAABB
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import kotlin.math.max

object Renderer {
    fun drawBox(
        aabb: AxisAlignedBB,
        color: Color,
        outlineWidth: Number = 3,
        outlineAlpha: Number = 1,
        fillAlpha: Number = 1,
        depth: Boolean = true
    ) {
        RenderUtils.drawOutlinedAABB(aabb, color.withAlpha(outlineAlpha.toFloat()), depth = depth)

        RenderUtils.drawFilledAABB(aabb, color.withAlpha(fillAlpha.toFloat()), depth = depth, outlineWidth = outlineWidth)
    }

    fun draw3DLine(pos1: Vec3, pos2: Vec3, color: Color, lineWidth: Int = 3, depth: Boolean = false) {
        val renderVec = mc.renderViewEntity.renderVec

        GlStateManager.pushMatrix()
        color.bind()
        translate(-renderVec.xCoord, -renderVec.yCoord, -renderVec.zCoord)
        preDraw()

        GL11.glLineWidth(lineWidth.toFloat())
        if (!depth) {
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GlStateManager.depthMask(false)
        }
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(pos1.xCoord, pos1.yCoord, pos1.zCoord).endVertex()
        worldRenderer.pos(pos2.xCoord, pos2.yCoord, pos2.zCoord).endVertex()

        tessellator.draw()

        translate(renderVec.xCoord, renderVec.yCoord, renderVec.zCoord)
        if (!depth) {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GlStateManager.depthMask(true)
        }

        postDraw()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
    }

    fun drawCustomBeacon(title: String, vec3: Vec3, color: Color, beacon: Boolean = true, increase: Boolean = true, noFade: Boolean = false, distance: Boolean = true) {
        val dist = vec3.distanceTo(mc.thePlayer.positionVector)
        drawBox(aabb = vec3.toAABB(), color = color, fillAlpha = 0f)

        RenderUtils.drawStringInWorld(
            if (distance) "$title §r§f(§3${dist.toInt()}m§f)" else title,
            vec3.addVec(0.5, 1.7 + dist / 30, 0.5),
            color = color,
            shadow = true,
            scale = if (increase) max(0.03, dist / 200.0).toFloat() else 0.06f
        )

        val alpha = if (noFade) 255f else min(1f, max(0f, dist.toFloat()) / 60f)
        if (beacon) drawBeaconBeam(vec3, color.withAlpha(alpha))
    }

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

    fun drawCylinder(
        pos: Vec3, baseRadius: Float, topRadius: Float, height: Float,
        slices: Int, stacks: Int, rot1: Float, rot2: Float, rot3: Float,
        color: Color, phase: Boolean = false, linemode: Boolean = false
    ) {
        drawCylinder(pos, baseRadius, topRadius, height, slices, stacks, rot1, rot2, rot3, color, linemode, phase)
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