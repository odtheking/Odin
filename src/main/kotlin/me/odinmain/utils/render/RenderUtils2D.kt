package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.utils.*
import me.odinmain.utils.render.RenderUtils.bind
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.RenderUtils.tessellator
import me.odinmain.utils.render.RenderUtils.worldRenderer
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Project
import java.nio.FloatBuffer
import java.nio.IntBuffer

object RenderUtils2D {

    data class Box2D(val x: Double, val y: Double, val w: Double, val h: Double)

    private val modelViewMatrix: FloatBuffer = BufferUtils.createFloatBuffer(16)
    private val projectionMatrix: FloatBuffer = BufferUtils.createFloatBuffer(16)
    private val viewportDims: IntBuffer = BufferUtils.createIntBuffer(16)

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderWorld(event: RenderWorldLastEvent) {
        val (x, y, z) = mc.thePlayer?.renderVec ?: return
        GlStateManager.pushMatrix()
        GlStateManager.translate(-x, -y, -z)

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix)
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix)

        GlStateManager.popMatrix()
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewportDims)
    }

    /**
     * Projects a 3D point to 2D screen coordinates.
     *
     * @param vec3 The 3D point to be projected.
     * @return The 2D screen coordinates as a Vec3, or null if projection fails.
     */
    private fun worldToScreenPosition(vec3: Vec3): Vec3? {
        val coords = BufferUtils.createFloatBuffer(3)
        val success = Project.gluProject(
            vec3.xCoord.toFloat(), vec3.yCoord.toFloat(), vec3.zCoord.toFloat(),
            modelViewMatrix, projectionMatrix, viewportDims, coords
        )

        return success.takeIf { it && coords[2] in 0.0..1.0 }?.run {
            val sr = ScaledResolution(mc)
            Vec3(coords[0] / sr.scaleFactor.toDouble(), (sr.scaledHeight - (coords[1] / sr.scaleFactor)).toDouble(), coords[2].toDouble())
        }
    }

    private fun calculateBoundingBox(aabb: AxisAlignedBB): Box2D? {
        var x1 = Double.MAX_VALUE
        var x2 = Double.MIN_VALUE
        var y1 = Double.MAX_VALUE
        var y2 = Double.MIN_VALUE

        aabb.corners.forEach { vertex ->
            worldToScreenPosition(vertex)?.let { vec ->
                x1 = x1.coerceAtMost(vec.xCoord)
                x2 = x2.coerceAtLeast(vec.xCoord)
                y1 = y1.coerceAtMost(vec.yCoord)
                y2 = y2.coerceAtLeast(vec.yCoord)
            }
        }
        return if (x1 != Double.MAX_VALUE) Box2D(x1, y1, x2, y2) else null
    }

    fun drawBackgroundNameTag(
        text: String,
        entity: Entity,
        padding: Number,
        backgroundColor: Color = Colors.MINECRAFT_GRAY.withAlpha(0.5f),
        accentColor: Color = Colors.MINECRAFT_BLUE,
        textColor: Color = Colors.WHITE,
        scale: Float = 1f,
        shadow: Boolean = false
    ) {
        worldToScreenPosition(entity.renderVec.addVec(y = 0.5 + entity.height))?.let {
            val width = getMCTextWidth(text) + padding.toDouble()
            val height = getMCTextHeight() + padding.toDouble()
            GlStateManager.pushMatrix()
            GlStateManager.translate(it.xCoord, it.yCoord, 0.0)
            GlStateManager.scale(scale, scale, scale)
            roundedRectangle(-width / 2, -height / 2, width, height * 0.9, backgroundColor)
            roundedRectangle(-width / 2, -height / 2 + height * 0.9, width, height * 0.1, accentColor)
            RenderUtils.drawText(text, 0f, -4.5f, 1f, textColor, shadow = shadow, center = true)
            GlStateManager.popMatrix()
        }
    }


    fun drawNameTag(vec3: Vec3, name: String) {
        worldToScreenPosition(vec3)?.let { pos ->
            mc.fontRendererObj.drawString(name, pos.xCoord.toFloat(), pos.yCoord.toFloat(), -1, true) // can use nvg in the future
        }
    }

    fun draw2DESP(aabb: AxisAlignedBB, color: Color, thickness: Float) {
        calculateBoundingBox(aabb)?.let { box ->
            drawBox(box, color, thickness)
        }
    }

    fun drawBox(box: Box2D, color: Color, lineWidth: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(lineWidth)
        color.bind()

        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)

        // Draw the four lines of the box
        worldRenderer.pos(box.x, box.y, 0.0).endVertex()  // Top-left to top-right
        worldRenderer.pos(box.w, box.y, 0.0).endVertex()

        worldRenderer.pos(box.w, box.y, 0.0).endVertex()  // Top-right to bottom-right
        worldRenderer.pos(box.w, box.h, 0.0).endVertex()

        worldRenderer.pos(box.w, box.h, 0.0).endVertex()  // Bottom-right to bottom-left
        worldRenderer.pos(box.x, box.h, 0.0).endVertex()

        worldRenderer.pos(box.x, box.h, 0.0).endVertex()  // Bottom-left to top-left
        worldRenderer.pos(box.x, box.y, 0.0).endVertex()

        tessellator.draw()

        Colors.WHITE.bind()
        GL11.glLineWidth(1f)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.popMatrix()
    }
}