package me.odinmain.utils.render

import gg.essential.universal.shader.BlendState
import gg.essential.universal.shader.UShader
import me.odinmain.OdinMain
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.util.glu.Cylinder
import java.awt.image.BufferedImage
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin


object RenderUtils {

    val tessellator: Tessellator = Tessellator.getInstance()
    val worldRenderer: WorldRenderer = tessellator.worldRenderer
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

    private fun blendFactor() = GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

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
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)
    }

    private fun postDraw() {
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }

    fun depth(depth: Boolean) {
        if (depth) GlStateManager.enableDepth() else GlStateManager.disableDepth()
        GlStateManager.depthMask(depth)
    }

    private fun resetDepth() {
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
    }

    fun Color.bind() {
        GlStateManager.resetColor()
        GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f)
    }

    private fun getRenderPos(vec: Vec3): Vec3 {
        return Vec3(vec.xCoord - renderManager.viewerPosX, vec.yCoord - renderManager.viewerPosY, vec.zCoord - renderManager.viewerPosZ)
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
        GlStateManager.disableCull()
        preDraw()
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
        GL11.glLineWidth(2f)
        postDraw()
        GlStateManager.popMatrix()
    }

    /**
     * Draws a beacon beam at the specified position.
     * @param vec3 The position at which to draw the beacon beam.
     * @param color The color of the beacon beam.
     * @param depth Whether to enable depth testing.
     */
    fun drawBeaconBeam(vec3: Vec3, color: Color, depth: Boolean = true, height: Int = 300) {
        if (color.isTransparent) return
        val bottomOffset = 0
        val topOffset = bottomOffset + height
        depth(depth)

        mc.textureManager.bindTexture(beaconBeam)

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT.toFloat())
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT.toFloat())

        GlStateManager.pushMatrix()

        GlStateManager.enableCull()
        GlStateManager.enableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)

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
        GlStateManager.disableCull()
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
        if (!depth) resetDepth()
        GlStateManager.enableCull()
        GlStateManager.popMatrix()
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
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(2f)
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
        if (text.isBlank()) return
        val renderPos = getRenderPos(vec3)

        GlStateManager.pushMatrix()

        val xMultiplier = if (mc.gameSettings.thirdPersonView == 2) -1 else 1
        GlStateManager.translate(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord)
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewX * xMultiplier, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(-scale, -scale, scale)

        GlStateManager.enableBlend()
        blendFactor()
        GlStateManager.disableLighting()
        depth(depthTest)

        val textWidth = mc.fontRendererObj.getStringWidth(text)
        mc.fontRendererObj.drawString("$text§r", -textWidth / 2f, 0f, color.rgba, shadow)

        if (!depthTest) resetDepth()
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
     * @param depth       Indicates whether to phase the cylinder (default is false)
     */
    fun drawCylinder(
        pos: Vec3, baseRadius: Number, topRadius: Number, height: Number,
        slices: Number, stacks: Number, rot1: Number, rot2: Number, rot3: Number,
        color: Color, depth: Boolean = false
    ) {
        val renderPos = getRenderPos(pos)

        GlStateManager.pushMatrix()
        GL11.glLineWidth(2.0f)

        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.depthMask(false)
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        blendFactor()

        if (depth) GlStateManager.disableDepth()

        color.bind()
        GlStateManager.translate(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord)
        GlStateManager.rotate(rot1.toFloat(), 1f, 0f, 0f)
        GlStateManager.rotate(rot2.toFloat(), 0f, 0f, 1f)
        GlStateManager.rotate(rot3.toFloat(), 0f, 1f, 0f)

        Cylinder().draw(baseRadius.toFloat(), topRadius.toFloat(), height.toFloat(), slices.toInt(), stacks.toInt())

        GlStateManager.enableCull()
        GlStateManager.disableBlend()
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        if (depth) GlStateManager.enableDepth()
        GlStateManager.resetColor()
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

    fun outlineColor(color: Color) {
        BUF_FLOAT_4.put(0, color.redFloat)
        BUF_FLOAT_4.put(1, color.greenFloat)
        BUF_FLOAT_4.put(2, color.blueFloat)
        BUF_FLOAT_4.put(3, color.alphaFloat)
        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, BUF_FLOAT_4)
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
        var yOffset = y - mc.fontRendererObj.FONT_HEIGHT
        text.split("\n").forEach {
            yOffset += mc.fontRendererObj.FONT_HEIGHT
            val xOffset = if (center) mc.fontRendererObj.getStringWidth(it) / -2f else 0f
            mc.fontRendererObj.drawString("${it}§r", xOffset, 0f, color.rgba, shadow)
        }
        GlStateManager.resetColor()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun AxisAlignedBB.outlineBounds(): AxisAlignedBB =
        expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)


    fun drawBoxes(boxes: Collection<DungeonWaypoint>, glList: Int, disableDepth: Boolean = false): Int {
        if (boxes.isEmpty()) return -1
        var newGlList = glList

        if (newGlList == -1) {
            newGlList = GL11.glGenLists(1)
            GL11.glNewList(newGlList, GL11.GL_COMPILE)

            GL11.glDisable(GL11.GL_CULL_FACE)
            GL11.glLineWidth(3f)

            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_ALPHA_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            for (box in boxes) {
                if (box.clicked) continue
                val depth = box.depth && !disableDepth
                if (depth) GL11.glEnable(GL11.GL_DEPTH_TEST)
                else GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glDepthMask(depth)

                box.aabb.offset(box.x, box.y, box.z).let {
                    box.color.bind()

                    if (box.filled) addVertexesForFilledBox(it)
                    else addVertexesForOutlinedBox(it)

                    tessellator.draw()
                }
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDepthMask(true)

            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_ALPHA_TEST)

            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            GL11.glLineWidth(2f)
            GL11.glEnable(GL11.GL_CULL_FACE)

            GL11.glEndList()
        }

        GL11.glPushMatrix()
        GL11.glTranslated(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)

        GL11.glCallList(newGlList)
        GL11.glPopMatrix()

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

    fun drawMinecraftLabel(entityIn: Entity, str: String, x: Double, y: Double, z: Double, scale: Double, depth: Boolean = true) {
        GlStateManager.pushMatrix()
        depth(depth)
        GlStateManager.translate(x + 0.0f, y + entityIn.height + 0.5f, z)
        GL11.glNormal3f(0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(-this.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(this.renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(-scale, -scale, scale)
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        mc.fontRendererObj.drawString(str, -mc.fontRendererObj.getStringWidth(str) / 2f, 0f, -1, true)
        GlStateManager.disableBlend()
        GlStateManager.enableLighting()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        if (!depth) resetDepth()
        GlStateManager.popMatrix()
    }
}