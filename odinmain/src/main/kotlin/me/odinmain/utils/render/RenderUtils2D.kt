package me.odinmain.utils.render

import me.odinmain.OdinMain.mc
import me.odinmain.utils.render.RenderUtils.renderVec
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
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

    private data class Box2D(val x: Double, val y: Double, val w: Double, val h: Double)

    private val modelViewMatrix: FloatBuffer = BufferUtils.createFloatBuffer(16)
    private val projectionMatrix: FloatBuffer = BufferUtils.createFloatBuffer(16)
    private val viewportDims: IntBuffer = BufferUtils.createIntBuffer(16)

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderWorld(event: RenderWorldLastEvent) {
        GlStateManager.pushMatrix()
        val renderPos = mc.thePlayer?.renderVec ?: return GlStateManager.popMatrix()
        GlStateManager.translate(-renderPos.xCoord, -renderPos.yCoord, -renderPos.zCoord)

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
        val vertices = getVertices(aabb)
        var x1 = Double.MAX_VALUE
        var x2 = Double.MIN_VALUE
        var y1 = Double.MAX_VALUE
        var y2 = Double.MIN_VALUE

        vertices.forEach { vertex ->
            worldToScreenPosition(vertex)?.let { vec ->
                x1 = x1.coerceAtMost(vec.xCoord)
                x2 = x2.coerceAtLeast(vec.xCoord)
                y1 = y1.coerceAtMost(vec.yCoord)
                y2 = y2.coerceAtLeast(vec.yCoord)
            }
        }
        return if (x1 != Double.MAX_VALUE) Box2D(x1, y1, x2, y2) else null
    }

    private fun getVertices(aabb: AxisAlignedBB): Array<Vec3> = arrayOf(
        Vec3(aabb.minX, aabb.minY, aabb.minZ),
        Vec3(aabb.minX, aabb.minY, aabb.maxZ),
        Vec3(aabb.maxX, aabb.minY, aabb.maxZ),
        Vec3(aabb.maxX, aabb.minY, aabb.minZ),
        Vec3(aabb.minX, aabb.maxY, aabb.minZ),
        Vec3(aabb.minX, aabb.maxY, aabb.maxZ),
        Vec3(aabb.maxX, aabb.maxY, aabb.maxZ),
        Vec3(aabb.maxX, aabb.maxY, aabb.minZ)
    )

    fun drawNameTag(vec3: Vec3, name: String) {
        worldToScreenPosition(vec3)?.let { pos ->
            mc.fontRendererObj.drawString(name, pos.xCoord.toFloat(), pos.yCoord.toFloat(), -1, true)
        }
    }

    fun draw2DESP(aabb: AxisAlignedBB, color: Color, thickness: Float) {
        calculateBoundingBox(aabb)?.let { box ->
            with(RenderUtils) {
                drawLine(color, box.x, box.y, box.x, box.h, thickness)
                drawLine(color, box.x, box.y, box.w, box.y, thickness)
                drawLine(color, box.w, box.h, box.w, box.y, thickness)
                drawLine(color, box.w, box.h, box.x, box.h, thickness)
            }
        }
    }

    fun draw3DESP(aabb: AxisAlignedBB, color: Color, thickness: Float) {
        val projected = getVertices(aabb).mapNotNull { worldToScreenPosition(it) }.takeIf { it.size == 8 } ?: return

        with(RenderUtils) {
            drawLine(color, projected[0].xCoord, projected[0].yCoord, projected[1].xCoord, projected[1].yCoord, thickness)
            drawLine(color, projected[0].xCoord, projected[0].yCoord, projected[4].xCoord, projected[4].yCoord, thickness)
            drawLine(color, projected[5].xCoord, projected[5].yCoord, projected[1].xCoord, projected[1].yCoord, thickness)
            drawLine(color, projected[5].xCoord, projected[5].yCoord, projected[4].xCoord, projected[4].yCoord, thickness)
            drawLine(color, projected[3].xCoord, projected[3].yCoord, projected[2].xCoord, projected[2].yCoord, thickness)
            drawLine(color, projected[3].xCoord, projected[3].yCoord, projected[7].xCoord, projected[7].yCoord, thickness)
            drawLine(color, projected[6].xCoord, projected[6].yCoord, projected[2].xCoord, projected[2].yCoord, thickness)
            drawLine(color, projected[6].xCoord, projected[6].yCoord, projected[7].xCoord, projected[7].yCoord, thickness)
            drawLine(color, projected[1].xCoord, projected[1].yCoord, projected[2].xCoord, projected[2].yCoord, thickness)
            drawLine(color, projected[0].xCoord, projected[0].yCoord, projected[3].xCoord, projected[3].yCoord, thickness)
            drawLine(color, projected[4].xCoord, projected[4].yCoord, projected[7].xCoord, projected[7].yCoord, thickness)
            drawLine(color, projected[5].xCoord, projected[5].yCoord, projected[6].xCoord, projected[6].yCoord, thickness)
        }
    }
}