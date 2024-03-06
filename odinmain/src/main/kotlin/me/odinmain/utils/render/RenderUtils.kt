package me.odinmain.utils.render

import gg.essential.universal.shader.BlendState
import gg.essential.universal.shader.UShader
import me.odinmain.OdinMain
import me.odinmain.OdinMain.mc
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import org.lwjgl.util.glu.GLU
import java.awt.image.BufferedImage
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

object RenderUtils {

    val tessellator: Tessellator = Tessellator.getInstance()
    val worldRenderer = tessellator.worldRenderer
    private val beaconBeam = ResourceLocation("textures/entity/beacon_beam.png")
    private val renderManager: RenderManager = mc.renderManager

    /**
     * Gets the rendered x-coordinate of an entity based on its last tick and current tick positions.
     *
     * @receiver The entity for which to retrieve the rendered x-coordinate.
     * @return The rendered x-coordinate.
     */
    val Entity.renderX: Double
        get() = lastTickPosX + (posX - lastTickPosX) * RenderUtils.partialTicks

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
        get() = lastTickPosZ + (posZ - lastTickPosZ) * RenderUtils.partialTicks

    /**
     * Gets the rendered position of an entity as a `Vec3`.
     *
     * @receiver The entity for which to retrieve the rendered position.
     * @return The rendered position as a `Vec3`.
     */
    val Entity.renderVec: Vec3
        get() = Vec3(renderX, renderY, renderZ)

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

    fun preDraw() {
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)
    }

    fun postDraw() {
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
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

    fun drawFilledAABB(aabb: AxisAlignedBB, color: Color, depth: Boolean = false, outlineWidth: Number = 3) {
        if (color.isTransparent) return
        GlStateManager.pushMatrix()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        if (depth) GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        GL11.glLineWidth(outlineWidth.toFloat())
        color.bind()

        worldRenderer {
            begin(7, DefaultVertexFormats.POSITION_NORMAL)
            pos(aabb.minX, aabb.maxY, aabb.minZ).normal(0f, 0f, -1f).endVertex()
            pos(aabb.maxX, aabb.maxY, aabb.minZ).normal(0f, 0f, -1f).endVertex()
            pos(aabb.maxX, aabb.minY, aabb.minZ).normal(0f, 0f, -1f).endVertex()
            pos(aabb.minX, aabb.minY, aabb.minZ).normal(0f, 0f, -1f).endVertex()
            pos(aabb.minX, aabb.minY, aabb.maxZ).normal(0f, 0f, 1f).endVertex()
            pos(aabb.maxX, aabb.minY, aabb.maxZ).normal(0f, 0f, 1f).endVertex()
            pos(aabb.maxX, aabb.maxY, aabb.maxZ).normal(0f, 0f, 1f).endVertex()
            pos(aabb.minX, aabb.maxY, aabb.maxZ).normal(0f, 0f, 1f).endVertex()
            pos(aabb.minX, aabb.minY, aabb.minZ).normal(0f, -1f, 0f).endVertex()
            pos(aabb.maxX, aabb.minY, aabb.minZ).normal(0f, -1f, 0f).endVertex()
            pos(aabb.maxX, aabb.minY, aabb.maxZ).normal(0f, -1f, 0f).endVertex()
            pos(aabb.minX, aabb.minY, aabb.maxZ).normal(0f, -1f, 0f).endVertex()
            pos(aabb.minX, aabb.maxY, aabb.maxZ).normal(0f, 1f, 0f).endVertex()
            pos(aabb.maxX, aabb.maxY, aabb.maxZ).normal(0f, 1f, 0f).endVertex()
            pos(aabb.maxX, aabb.maxY, aabb.minZ).normal(0f, 1f, 0f).endVertex()
            pos(aabb.minX, aabb.maxY, aabb.minZ).normal(0f, 1f, 0f).endVertex()
            pos(aabb.minX, aabb.minY, aabb.maxZ).normal(-1f, 0f, 0f).endVertex()
            pos(aabb.minX, aabb.maxY, aabb.maxZ).normal(-1f, 0f, 0f).endVertex()
            pos(aabb.minX, aabb.maxY, aabb.minZ).normal(-1f, 0f, 0f).endVertex()
            pos(aabb.minX, aabb.minY, aabb.minZ).normal(-1f, 0f, 0f).endVertex()
            pos(aabb.maxX, aabb.minY, aabb.minZ).normal(1f, 0f, 0f).endVertex()
            pos(aabb.maxX, aabb.maxY, aabb.minZ).normal(1f, 0f, 0f).endVertex()
            pos(aabb.maxX, aabb.maxY, aabb.maxZ).normal(1f, 0f, 0f).endVertex()
            pos(aabb.maxX, aabb.minY, aabb.maxZ).normal(1f, 0f, 0f).endVertex()
        }
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
    }


    fun drawOutlinedAABB(aabb: AxisAlignedBB, color: Color, thickness: Number = 3f, depth: Boolean = false) {
        if (color.isTransparent) return
        GlStateManager.pushMatrix()
        color.bind()
        GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        if (depth) GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        GL11.glLineWidth(thickness.toFloat())

        worldRenderer {
            begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
            pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
            pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
            pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
            pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
            pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()

            pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
            pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
            pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
            pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
            pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()

            pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
            pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
            pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
            pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
            pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
            pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        }

        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
    }

    fun drawBeaconBeam(vec3: Vec3, color: Color) {
        if (color.isTransparent) return
        val height = 300
        val bottomOffset = 0
        val topOffset = bottomOffset + height

        mc.textureManager.bindTexture(beaconBeam)

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT.toFloat())
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT.toFloat())

        GlStateManager.disableLighting()
        GlStateManager.enableCull()
        GlStateManager.enableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

        val time: Double = mc.theWorld.worldTime.toDouble() + RenderUtils.partialTicks
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
    }

    fun drawStringInWorld(
        text: String,
        vec3: Vec3,
        color: Color = Color.WHITE.withAlpha(1f),
        renderBlackBox: Boolean = false,
        depthTest: Boolean = true,
        scale: Float = 0.3f,
        shadow: Boolean = true
    ) {
        val renderPos = getRenderPos(vec3)

        if (!depthTest) {
            GlStateManager.disableDepth()
            GlStateManager.depthMask(false)
        }

        val xMultiplier = if (mc.gameSettings.thirdPersonView == 2) -1 else 1

        color.bind()
        GlStateManager.pushMatrix()
        translate(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord)
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewX * xMultiplier, 1.0f, 0.0f, 0.0f)
        scale(-scale, -scale, scale)
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)

        val textWidth = mc.fontRendererObj.getStringWidth(text)

        if (renderBlackBox) {
            val j = textWidth / 2
            GlStateManager.disableTexture2D()
            worldRenderer {
                begin(7, DefaultVertexFormats.POSITION_COLOR)
                worldRenderer.pos(-j - 1.0, -1.0, .0).color(.0f, .0f, .0f, 0.25f).endVertex()
                worldRenderer.pos(-j - 1.0, 8.0, .0).color(.0f, .0f, .0f, 0.25f).endVertex()
                worldRenderer.pos(j + 1.0, 8.0, .0).color(.0f, .0f, .0f, 0.25f).endVertex()
                worldRenderer.pos(j + 1.0, -1.0, .0).color(.0f, .0f, .0f, 0.25f).endVertex()
            }
            tessellator.draw()
            GlStateManager.enableTexture2D()
        }

        if (shadow) mc.fontRendererObj.drawStringWithShadow(text, -textWidth / 2f, 0f, color.rgba)
        else mc.fontRendererObj.drawString(text, -textWidth / 2, 0, color.rgba)

        if (!depthTest) {
            GlStateManager.enableDepth()
            GlStateManager.depthMask(true)
        }

        GlStateManager.resetColor()
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    fun drawCylinder(
        pos: Vec3, baseRadius: Number, topRadius: Number, height: Number,
        slices: Number, stacks: Number, rot1: Number, rot2: Number, rot3: Number,
        color: Color, phase: Boolean = false, linemode: Boolean = false
    ) {
        val renderPos = getRenderPos(pos)
        val x = renderPos.xCoord
        val y = renderPos.yCoord
        val z = renderPos.zCoord

        GlStateManager.pushMatrix()
        GL11.glLineWidth(2.0f)
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.blendFunc(770, 771)
        GlStateManager.disableTexture2D()

        if (phase) GlStateManager.disableDepth()

        color.bind()
        GlStateManager.translate(x, y, z)
        GlStateManager.rotate(rot1.toFloat(), 1f, 0f, 0f)
        GlStateManager.rotate(rot2.toFloat(), 0f, 0f, 1f)
        GlStateManager.rotate(rot3.toFloat(), 0f, 1f, 0f)

        val cyl = Cylinder()
        cyl.drawStyle = GLU.GLU_LINE
        if (linemode) cyl.draw(baseRadius.toFloat(), topRadius.toFloat(), height.toFloat(), slices.toInt(), stacks.toInt())
        else Cylinder().draw(baseRadius.toFloat(), topRadius.toFloat(), height.toFloat(), slices.toInt(), stacks.toInt())

        GlStateManager.enableCull()
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.resetColor()
        if (phase) GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    fun drawTexturedModalRect(x: Int, y: Int, width: Int, height: Int) {
        worldRenderer {
            begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
            worldRenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(0.0, 1.0).endVertex()
            worldRenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(1.0, 1.0).endVertex()
            worldRenderer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(1.0, 0.0).endVertex()
            worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(0.0, 0.0).endVertex()
        }
        tessellator.draw()
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
        OdinMain::class.java.getResource("/shaders/$name.$ext")?.readText() ?: ""

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

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        partialTicks = event.partialTicks
    }
}