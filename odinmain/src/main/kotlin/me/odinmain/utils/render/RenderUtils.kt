package me.odinmain.utils.render

import gg.essential.universal.shader.BlendState
import gg.essential.universal.shader.UShader
import me.odinmain.OdinMain
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.*
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.*
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.util.glu.Cylinder
import org.lwjgl.util.glu.GLU
import org.lwjgl.util.vector.Vector3f
import java.awt.image.BufferedImage
import kotlin.math.*


object RenderUtils {

    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer
    private val beaconBeam = ResourceLocation("textures/entity/beacon_beam.png")
    private val renderManager: RenderManager = mc.renderManager

    /**
     * Gets the rendered x-coordinate of an entity based on its last tick and current tick positions.
     *
     * @receiver The entity for which to retrieve the rendered x-coordinate.
     * @return The rendered x-coordinate.
     */
    val Entity.renderX: Double
        get() = lastTickPosX + (posX - lastTickPosX) * partialTicks

    /**
     * Gets the rendered y-coordinate of an entity based on its last tick and current tick positions.
     *
     * @receiver The entity for which to retrieve the rendered y-coordinate.
     * @return The rendered y-coordinate.
     */
    val Entity.renderY: Double
        get() = lastTickPosY + (posY - lastTickPosY) * partialTicks

    /**
     * Gets the rendered z-coordinate of an entity based on its last tick and current tick positions.
     *
     * @receiver The entity for which to retrieve the rendered z-coordinate.
     * @return The rendered z-coordinate.
     */
    val Entity.renderZ: Double
        get() = lastTickPosZ + (posZ - lastTickPosZ) * partialTicks

    /**
     * Gets the rendered position of an entity as a `Vec3`.
     *
     * @receiver The entity for which to retrieve the rendered position.
     * @return The rendered position as a `Vec3`.
     */
    val Entity.renderVec: Vec3
        get() = Vec3(renderX, renderY, renderZ)

    private val viewerVec: Vec3
        get() = Vec3(renderManager.viewerPosX, renderManager.viewerPosY, renderManager.viewerPosZ)

    private fun blendFactor() {
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    }

    /**
     * Gets the rendered bounding box of an entity based on its last tick and current tick positions.
     *
     * @receiver The entity for which to retrieve the rendered bounding box.
     * @return The rendered bounding box as an `AxisAlignedBB`.
     */
    val Entity.renderBoundingBox: AxisAlignedBB
        get() = AxisAlignedBB(
            renderX - this.width / 2,
            renderY,
            renderZ - this.width / 2,
            renderX + this.width / 2,
            renderY + this.height,
            renderZ + this.width / 2
        )

    inline operator fun WorldRenderer.invoke(block: WorldRenderer.() -> Unit) {
        block.invoke(this)
    }

    private fun WorldRenderer.addVertex(x: Double, y: Double, z: Double, nx: Float, ny: Float, nz: Float) {
        pos(x, y, z).normal(nx, ny, nz).endVertex()
    }

    private fun preDraw() {
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)
    }

    fun depth(depth: Boolean) {
        if (depth) GlStateManager.enableDepth() else GlStateManager.disableDepth()
        GlStateManager.depthMask(depth)
    }

    private fun resetDepth() {
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
    }

    private fun postDraw() {
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        Color.WHITE.bind()
    }

    fun Color.bind() {
        GlStateManager.resetColor()
        GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f)
    }

    private fun getRenderPos(vec: Vec3): Vec3 {
        val renderPosX = mc.renderManager.viewerPosX
        val renderPosY = mc.renderManager.viewerPosY
        val renderPosZ = mc.renderManager.viewerPosZ
        return Vec3(vec.xCoord - renderPosX, vec.yCoord - renderPosY, vec.zCoord - renderPosZ)
    }

    /**
     * Draws a filled Axis Aligned Bounding Box (AABB).
     *
     * @param aabb The bounding box to draw.
     * @param color The color to use for drawing.
     * @param depth Whether to enable depth testing.
     */
    fun drawFilledAABB(aabb: AxisAlignedBB, color: Color, depth: Boolean = false) {
        if (color.isTransparent) return

        GlStateManager.pushMatrix()
        preDraw()
        GlStateManager.disableCull()
        depth(depth)
        color.bind()
        addVertexesForFilledBox(aabb)
        tessellator.draw()
        if (!depth) resetDepth()
        postDraw()
        GlStateManager.enableCull()
        GlStateManager.popMatrix()
    }

    /**
     * Draws an outlined Axis Aligned Bounding Box (AABB).
     *
     * @param aabb The bounding box to draw.
     * @param color The color to use for drawing.
     * @param thickness The thickness of the outline.
     * @param depth Whether to enable depth testing.
     */
    fun drawOutlinedAABB(aabb: AxisAlignedBB, color: Color, thickness: Number = 3f, depth: Boolean = false, smoothLines: Boolean = true) {
        if (color.isTransparent) return
        GlStateManager.pushMatrix()
        preDraw()

        if (smoothLines) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        }

        GL11.glLineWidth(thickness.toFloat())
        depth(depth)
        color.bind()
        addVertexesForOutlinedBox(aabb)
        tessellator.draw()

        if (smoothLines) GL11.glDisable(GL11.GL_LINE_SMOOTH)

        if (!depth) resetDepth()
        postDraw()
        GL11.glLineWidth(2f)
        GlStateManager.popMatrix()
    }

    /**
     * Draws a beacon beam at the specified position.
     * @param vec3 The position at which to draw the beacon beam.
     * @param color The color of the beacon beam.
     * @param depth Whether to enable depth testing.
     */
    fun drawBeaconBeam(vec3: Vec3, color: Color, depth: Boolean = false, height: Int = 300) {
        if (color.isTransparent) return
        val bottomOffset = 0
        val topOffset = bottomOffset + height
        if (!depth) GlStateManager.disableDepth()

        mc.textureManager.bindTexture(beaconBeam)

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT.toFloat())
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT.toFloat())

        GlStateManager.disableLighting()
        GlStateManager.enableCull()
        GlStateManager.enableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

        val time: Double = mc.theWorld.worldTime.toDouble() + partialTicks
        val x = vec3.xCoord
        val y = vec3.yCoord
        val z = vec3.zCoord
        val d1 = MathHelper.func_181162_h(-time * 0.2 - floor(-time * 0.1))
        val d2 = time * 0.025 * -1.5
        val d4 = 0.5 + cos(d2 + 2.356194490192345) * 0.2
        val d5 = 0.5 + sin(d2 + 2.356194490192345) * 0.2
        val d6 = 0.5 + cos(d2 + (Math.PI / 4)) * 0.2
        val d7 = 0.5 + sin(d2 + (Math.PI / 4)) * 0.2
        val d8 = 0.5 + cos(d2 + 3.9269908169872414) * 0.2
        val d9 = 0.5 + sin(d2 + 3.9269908169872414) * 0.2
        val d10 = 0.5 + cos(d2 + 5.497787143782138) * 0.2
        val d11 = 0.5 + sin(d2 + 5.497787143782138) * 0.2
        val d14 = -1 + d1
        val d15 = height * 2.5 + d14

        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)

        fun WorldRenderer.color(alpha: Float = color.alpha) { // local function is used to simplify this.
            this.color(color.r / 255f, color.g / 255f, color.b / 255f, alpha).endVertex()
        }

        worldRenderer {
            begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)

            pos(x + d4, y + topOffset, z + d5).tex(1.0, d15).color()
            pos(x + d4, y + bottomOffset, z + d5).tex(1.0, d14).color()
            pos(x + d6, y + bottomOffset, z + d7).tex(0.0, d14).color()
            pos(x + d6, y + topOffset, z + d7).tex(0.0, d15).color()
            pos(x + d10, y + topOffset, z + d11).tex(1.0, d15).color()
            pos(x + d10, y + bottomOffset, z + d11).tex(1.0, d14).color()
            pos(x + d8, y + bottomOffset, z + d9).tex(0.0, d14).color()
            pos(x + d8, y + topOffset, z + d9).tex(0.0, d15).color()
            pos(x + d6, y + topOffset, z + d7).tex(1.0, d15).color()
            pos(x + d6, y + bottomOffset, z + d7).tex(1.0, d14).color()
            pos(x + d10, y + bottomOffset, z + d11).tex(0.0, d14).color()
            pos(x + d10, y + topOffset, z + d11).tex(0.0, d15).color()
            pos(x + d8, y + topOffset, z + d9).tex(1.0, d15).color()
            pos(x + d8, y + bottomOffset, z + d9).tex(1.0, d14).color()
            pos(x + d4, y + bottomOffset, z + d5).tex(0.0, d14).color()
            pos(x + d4, y + topOffset, z + d5).tex(0.0, d15).color()
        }
        tessellator.draw()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)

        val d12 = -1 + d1
        val d13 = height + d12
        val alpha = color.alpha
        worldRenderer {
            begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
            pos(x + 0.2, y + topOffset, z + 0.2).tex(1.0, d13).color(.25f * alpha)
            pos(x + 0.2, y + bottomOffset, z + 0.2).tex(1.0, d12).color(.25f * alpha)
            pos(x + 0.8, y + bottomOffset, z + 0.2).tex(0.0, d12).color(.25f * alpha)
            pos(x + 0.8, y + topOffset, z + 0.2).tex(0.0, d13).color(.25f * alpha)
            pos(x + 0.8, y + topOffset, z + 0.8).tex(1.0, d13).color(.25f * alpha)
            pos(x + 0.8, y + bottomOffset, z + 0.8).tex(1.0, d12).color(.25f * alpha)
            pos(x + 0.2, y + bottomOffset, z + 0.8).tex(0.0, d12).color(.25f * alpha)
            pos(x + 0.2, y + topOffset, z + 0.8).tex(0.0, d13).color(.25f * alpha)
            pos(x + 0.8, y + topOffset, z + 0.2).tex(1.0, d13).color(.25f * alpha)
            pos(x + 0.8, y + bottomOffset, z + 0.2).tex(1.0, d12).color(.25f * alpha)
            pos(x + 0.8, y + bottomOffset, z + 0.8).tex(0.0, d12).color(.25f * alpha)
            pos(x + 0.8, y + topOffset, z + 0.8).tex(0.0, d13).color(.25f * alpha)
            pos(x + 0.2, y + topOffset, z + 0.8).tex(1.0, d13).color(.25f * alpha)
            pos(x + 0.2, y + bottomOffset, z + 0.8).tex(1.0, d12).color(.25f * alpha)
            pos(x + 0.2, y + bottomOffset, z + 0.2).tex(0.0, d12).color(.25f * alpha)
            pos(x + 0.2, y + topOffset, z + 0.2).tex(0.0, d13).color(.25f * alpha)

            endVertex()
        }
        tessellator.draw()
        GlStateManager.resetColor()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
        GlStateManager.enableTexture2D()
        if (!depth) GlStateManager.enableDepth()
    }

    fun drawLines(vararg points: Vec3, color: Color, lineWidth: Float, depth: Boolean) {
        if (points.size < 2) return

        GlStateManager.pushMatrix()
        color.bind()
        preDraw()
        depth(depth)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(lineWidth)

        worldRenderer {
            begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
            for (point in points) {
                pos(point.xCoord, point.yCoord, point.zCoord).endVertex()
            }
        }
        tessellator.draw()

        if (!depth) resetDepth()
        postDraw()
        GlStateManager.popMatrix()
    }

    fun drawLine(color: Color, x1: Double, y1: Double, x2: Double, y2: Double, lineWidth: Float) {
        GlStateManager.pushMatrix()
        color.bind()
        preDraw()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(lineWidth)

        worldRenderer {
            begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
            pos(x1, y1, 0.0).endVertex()
            pos(x2, y2, 0.0).endVertex()
        }
        tessellator.draw()

        postDraw()
        GlStateManager.popMatrix()
    }

    /**
     * Draws text in the world at the specified position with the specified color and optional parameters.
     *
     * @param text            The text to be drawn.
     * @param vec3            The position to draw the text.
     * @param color           The color of the text.
     * @param depthTest       Indicates whether to draw with depth (default is true).
     * @param scale           The scale of the text (default is 0.03).
     * @param shadow          Indicates whether to render a shadow for the text (default is true).
     */
    fun drawStringInWorld(
        text: String,
        vec3: Vec3,
        color: Color = Color.WHITE.withAlpha(1f),
        depthTest: Boolean = true,
        scale: Float = 0.3f,
        shadow: Boolean = false
    ) {
        val renderPos = getRenderPos(vec3)

        if (!depthTest) {
            GlStateManager.disableDepth()
            GlStateManager.depthMask(false)
        }

        val xMultiplier = if (mc.gameSettings.thirdPersonView == 2) -1 else 1

        GlStateManager.pushMatrix()
        GlStateManager.translate(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord)
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewX * xMultiplier, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(-scale, -scale, scale)

        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        color.bind()

        val textWidth = mc.fontRendererObj.getStringWidth(text)
        mc.fontRendererObj.drawString("$text§r", -textWidth / 2f, 0f, color.rgba, shadow)

        if (!depthTest) {
            GlStateManager.enableDepth()
            GlStateManager.depthMask(true)
        }

        GlStateManager.disableBlend()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
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
     * @param lineMode    Indicates whether to draw the cylinder in line mode (default is false).
     */
    fun drawCylinder(
        pos: Vec3, baseRadius: Number, topRadius: Number, height: Number,
        slices: Number, stacks: Number, rot1: Number, rot2: Number, rot3: Number,
        color: Color, depth: Boolean = false, lineMode: Boolean = false
    ) {
        val renderPos = getRenderPos(pos)

        GlStateManager.pushMatrix()
        GL11.glLineWidth(2.0f)
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        blendFactor()
        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()

        if (depth) GlStateManager.disableDepth()

        color.bind()
        GlStateManager.translate(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord)
        GlStateManager.rotate(rot1.toFloat(), 1f, 0f, 0f)
        GlStateManager.rotate(rot2.toFloat(), 0f, 0f, 1f)
        GlStateManager.rotate(rot3.toFloat(), 0f, 1f, 0f)

        val cyl = Cylinder()
        cyl.drawStyle = GLU.GLU_LINE
        if (lineMode) cyl.draw(baseRadius.toFloat(), topRadius.toFloat(), height.toFloat(), slices.toInt(), stacks.toInt())
        else Cylinder().draw(baseRadius.toFloat(), topRadius.toFloat(), height.toFloat(), slices.toInt(), stacks.toInt())

        GlStateManager.enableCull()
        GlStateManager.disableBlend()
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        if (depth) GlStateManager.enableDepth()
        GlStateManager.resetColor()
        Color.WHITE.bind()
        GlStateManager.popMatrix()
    }

    /**
     * Draws a Texture modal rectangle at the specified position.
     * @param x The x-coordinate of the rectangle.
     * @param y The y-coordinate of the rectangle.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
    fun drawTexturedModalRect(
        x: Int, y: Int, width: Int, height: Int,
        u: Float = 0f, v: Float = 0f, uWidth: Int = 1, vHeight: Int = 1,
        tileWidth: Float = 1.0f, tileHeight: Float = 1.0f
    ) {
        val f = 1.0f / tileWidth
        val g = 1.0f / tileHeight
        Color.WHITE.bind()
        worldRenderer {
            begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
            pos(x.toDouble(), (y + height).toDouble(), 0.0).tex((u * f).toDouble(), ((v + vHeight.toFloat()) * g).toDouble()).endVertex()
            pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(((u + uWidth.toFloat()) * f).toDouble(), ((v + vHeight.toFloat()) * g).toDouble()).endVertex()
            pos((x + width).toDouble(), y.toDouble(), 0.0).tex(((u + uWidth.toFloat()) * f).toDouble(), (v * g).toDouble()).endVertex()
            pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * g).toDouble()).endVertex()
        }
        tessellator.draw()
        GlStateManager.resetColor()
    }


    fun draw2D(entity: Entity, lineWidth: Float, color: Color) {
        val mvMatrix = getMatrix(2982)
        val projectionMatrix = getMatrix(2983)
        val bb = entity.entityBoundingBox.offset(-entity.positionVector).offset(entity.renderVec).offset(-viewerVec)
        var box = BoxWithClass(Float.MAX_VALUE, Float.MAX_VALUE, -1f,  -1f)

        GL11.glPushAttrib(GL11.GL_S)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GlStateManager.pushMatrix()
        GL11.glLoadIdentity()
        GL11.glOrtho(0.0, mc.displayWidth.toDouble(), mc.displayHeight.toDouble(), .0, -1.0, 1.0)
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GlStateManager.pushMatrix()
        GL11.glLoadIdentity()
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        blendFactor()
        GlStateManager.enableTexture2D()
        GlStateManager.depthMask(true)
        GL11.glLineWidth(lineWidth)

        for (boxVertex in bb.corners) {
            val screenPos = worldToScreen(
                Vec3f(boxVertex.xCoord.toFloat(), boxVertex.yCoord.toFloat(), boxVertex.zCoord.toFloat()),
                mvMatrix, projectionMatrix, mc.displayWidth, mc.displayHeight
            ) ?: continue
            box = BoxWithClass(min(screenPos.x, box.x), min(screenPos.y, box.y), max(screenPos.x, box.w), max(screenPos.y, box.h))
        }

        if ((box.x > 0f && box.y > 0f && box.x <= mc.displayWidth && box.y <= mc.displayHeight) || (box.w > 0 && box.h > 0 && box.w <= mc.displayWidth && box.h <= mc.displayHeight))
            rectangleOutline(box.x, box.y, box.w - box.x, box.h - box.y, color, 1f, lineWidth)

        GlStateManager.disableBlend()
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GlStateManager.popMatrix()
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GlStateManager.popMatrix()
        GL11.glPopAttrib()
    }

    private fun getMatrix(matrix: Int): Matrix4f {
        val floatBuffer = BufferUtils.createFloatBuffer(16)
        GL11.glGetFloat(matrix, floatBuffer)
        return Matrix4f().load(floatBuffer) as Matrix4f
    }

    private fun worldToScreen(pointInWorld: Vec3f, view: Matrix4f, projection: Matrix4f, screenWidth: Int, screenHeight: Int): Vec2f? {
        val clipSpacePos = (Vec4f(pointInWorld.x, pointInWorld.y, pointInWorld.z, 1.0f) * view) * projection
        val ndcSpacePos = Vector3f(clipSpacePos.x / clipSpacePos.w, clipSpacePos.y / clipSpacePos.w, clipSpacePos.z / clipSpacePos.w)
        val screenX: Float = (ndcSpacePos.x + 1.0f) / 2.0f * screenWidth
        val screenY: Float = (1.0f - ndcSpacePos.y) / 2.0f * screenHeight
        if (ndcSpacePos.z < -1.0 || ndcSpacePos.z > 1.0) {
            return null
        }
        return Vec2f(screenX, screenY)
    }

    private val BUF_FLOAT_4 = BufferUtils.createFloatBuffer(4)
    var isRenderingOutlinedEntities = false
        private set

    fun enableOutlineMode() {
        isRenderingOutlinedEntities = true
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_CONSTANT)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_REPLACE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)
    }
    fun outlineColor(color: Color) {
        BUF_FLOAT_4.put(0, color.redFloat)
        BUF_FLOAT_4.put(1, color.greenFloat)
        BUF_FLOAT_4.put(2, color.blueFloat)
        BUF_FLOAT_4.put(3, color.alphaFloat)
        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, BUF_FLOAT_4)
    }

    fun disableOutlineMode() {
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)
        isRenderingOutlinedEntities = false
    }

    /**
     * Creates a shader from a vertex shader, fragment shader, and a blend state
     *
     * @param vertName The name of the vertex shader's file.
     * @param fragName The name of the fragment shader's file.
     * @param blendState The blend state for the shader
     */
    fun createLegacyShader(vertName: String, fragName: String, blendState: BlendState) =
        UShader.fromLegacyShader(readShader(vertName, "vsh"), readShader(fragName, "fsh"), blendState)

    /**
     * Reads a shader file as a text file, and returns the contents
     *
     * @param name The name of the shader file
     * @param ext The file extension of the shader file (usually fsh or vsh)
     *
     * @return The contents of the shader file at the given path.
     */
    private fun readShader(name: String, ext: String): String =
        OdinMain::class.java.getResource("/shaders/source/$name.$ext")?.readText() ?: ""

    /**
     * Loads a BufferedImage from a path to a resource in the project
     *
     * @param path The path to the image file
     *
     * @returns The BufferedImage of that resource path.
     */
    fun loadBufferedImage(path: String): BufferedImage =
        TextureUtil.readBufferedImage(OdinMain::class.java.getResourceAsStream(path))


    var partialTicks = 0f

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderWorld(event: RenderWorldLastEvent) {
        this.partialTicks = event.partialTicks
    }

    fun drawText(
        text: String,
        x: Float,
        y: Float,
        scale: Double = 1.0,
        color: Color = Color.WHITE,
        shadow: Boolean = true,
        center: Boolean = false
    ) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        translate(x, y, 0f)
        scale(scale, scale, scale)
        color.bind()
        var yOffset = y - mc.fontRendererObj.FONT_HEIGHT
        text.split("\n").forEach {
            yOffset += mc.fontRendererObj.FONT_HEIGHT
            val xOffset = if (center) mc.fontRendererObj.getStringWidth(it) / -2f else 0f
            mc.fontRendererObj.drawString(it, xOffset, 0f, color.rgba, shadow)
        }
        GlStateManager.resetColor()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun AxisAlignedBB.outlineBounds(): AxisAlignedBB =
        expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)


    fun drawBoxes(boxes: Collection<DungeonWaypoint>, glList: Int, disableDepth: Boolean = false): Int {
        var newGlList = glList
        GlStateManager.pushMatrix()
        preDraw()
        GlStateManager.disableCull()

        GL11.glLineWidth(3f)
        if (newGlList != -1) {
            GL11.glCallList(newGlList)
            postDraw()
            resetDepth()
            GlStateManager.enableCull()
            GlStateManager.resetColor()
            GlStateManager.popMatrix()
            return newGlList
        } else {
            newGlList = GL11.glGenLists(1)
            GL11.glNewList(newGlList, GL11.GL_COMPILE)
        }

        for (box in boxes) {
            if (!box.depth || disableDepth) GlStateManager.disableDepth()
            else GlStateManager.enableDepth()
            val aabb = box.aabb.offset(box.x, box.y, box.z)
            box.color.bind()

            if (box.filled) addVertexesForFilledBox(aabb)
            else addVertexesForOutlinedBox(aabb)
            tessellator.draw()
        }
        GL11.glEndList()
        postDraw()
        resetDepth()
        GlStateManager.enableCull()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
        return newGlList
    }

    private fun addVertexesForFilledBox(aabb: AxisAlignedBB) {
        val minX = aabb.minX
        val minY = aabb.minY
        val minZ = aabb.minZ
        val maxX = aabb.maxX
        val maxY = aabb.maxY
        val maxZ = aabb.maxZ

        worldRenderer {
            begin(7, DefaultVertexFormats.POSITION_NORMAL)

            // Front face
            addVertex(minX, maxY, minZ, 0f, 0f, -1f)
            addVertex(maxX, maxY, minZ, 0f, 0f, -1f)
            addVertex(maxX, minY, minZ, 0f, 0f, -1f)
            addVertex(minX, minY, minZ, 0f, 0f, -1f)

            // Back face
            addVertex(minX, minY, maxZ, 0f, 0f, 1f)
            addVertex(maxX, minY, maxZ, 0f, 0f, 1f)
            addVertex(maxX, maxY, maxZ, 0f, 0f, 1f)
            addVertex(minX, maxY, maxZ, 0f, 0f, 1f)

            // Bottom face
            addVertex(minX, minY, minZ, 0f, -1f, 0f)
            addVertex(maxX, minY, minZ, 0f, -1f, 0f)
            addVertex(maxX, minY, maxZ, 0f, -1f, 0f)
            addVertex(minX, minY, maxZ, 0f, -1f, 0f)

            // Top face
            addVertex(minX, maxY, maxZ, 0f, 1f, 0f)
            addVertex(maxX, maxY, maxZ, 0f, 1f, 0f)
            addVertex(maxX, maxY, minZ, 0f, 1f, 0f)
            addVertex(minX, maxY, minZ, 0f, 1f, 0f)

            // Left face
            addVertex(minX, minY, maxZ, -1f, 0f, 0f)
            addVertex(minX, maxY, maxZ, -1f, 0f, 0f)
            addVertex(minX, maxY, minZ, -1f, 0f, 0f)
            addVertex(minX, minY, minZ, -1f, 0f, 0f)

            // Right face
            addVertex(maxX, minY, minZ, 1f, 0f, 0f)
            addVertex(maxX, maxY, minZ, 1f, 0f, 0f)
            addVertex(maxX, maxY, maxZ, 1f, 0f, 0f)
            addVertex(maxX, minY, maxZ, 1f, 0f, 0f)
        }
    }

    private fun addVertexesForOutlinedBox(aabb: AxisAlignedBB) {
        val minX = aabb.minX
        val minY = aabb.minY
        val minZ = aabb.minZ
        val maxX = aabb.maxX
        val maxY = aabb.maxY
        val maxZ = aabb.maxZ

        worldRenderer {
            begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
            pos(minX, minY, minZ).endVertex()
            pos(minX, minY, maxZ).endVertex()
            pos(maxX, minY, maxZ).endVertex()
            pos(maxX, minY, minZ).endVertex()
            pos(minX, minY, minZ).endVertex()

            pos(minX, maxY, minZ).endVertex()
            pos(minX, maxY, maxZ).endVertex()
            pos(maxX, maxY, maxZ).endVertex()
            pos(maxX, maxY, minZ).endVertex()
            pos(minX, maxY, minZ).endVertex()

            pos(minX, maxY, maxZ).endVertex()
            pos(minX, minY, maxZ).endVertex()
            pos(maxX, minY, maxZ).endVertex()
            pos(maxX, maxY, maxZ).endVertex()
            pos(maxX, maxY, minZ).endVertex()
            pos(maxX, minY, minZ).endVertex()
        }
    }


}