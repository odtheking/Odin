package me.odinmain.utils.render.world

import me.odinmain.OdinMain.mc
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.*
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import org.lwjgl.util.glu.GLU
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.*

object RenderUtils {

    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer
    private val renderManager: RenderManager = mc.renderManager

    var partialTicks = 0f

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        partialTicks = event.partialTicks
    }

    fun preDraw() {
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    }

    fun postDraw() {
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }

    /**
     * Gets the viewer's position as a triple of x, y, and z coordinates.
     *
     * @return A `Triple` representing the viewer's position.
     */
    val viewerPos: Triple<Double, Double, Double>
        get() {
            val viewer = mc.renderViewEntity
            return Triple(
                viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks,
                viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks,
                viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks
            )
        }

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


    fun Color.bindColor() {
        GlStateManager.resetColor()
        GlStateManager.color(r / 255f, g / 255f, b / 255f, a / 255f)
    }

    inline operator fun WorldRenderer.invoke(block: WorldRenderer.() -> Unit) {
        block.invoke(this)
    }

    fun drawCustomBox(aabb: AxisAlignedBB, color: Color, thickness: Float = 3f, phase: Boolean) {
        drawCustomBox(
            aabb.minX, aabb.maxX - aabb.minX,
            aabb.minY, aabb.maxY - aabb.minY,
            aabb.minZ, aabb.maxZ - aabb.minZ,
            color,
            thickness,
            phase
        )
    }

    fun drawBoxWithOutline(aabb: AxisAlignedBB, color: Color, phase: Boolean, thickness: Float = 3f) {
        drawCustomBox(
            aabb.minX, aabb.maxX - aabb.minX,
            aabb.minY, aabb.maxY - aabb.minY,
            aabb.minZ, aabb.maxZ - aabb.minZ,
            color,
            thickness,
            phase
        )
        drawFilledBox(
            aabb,
            color,
            phase
        )
    }


    /**
     * Draws a custom box in the 3D world space.
     *
     * @param x X-coordinate of the box.
     * @param y Y-coordinate of the box.
     * @param z Z-coordinate of the box.
     * @param scale The scale of the box.
     * @param color The color of the box (must be in the range of 0-255).
     * @param thickness The thickness of the lines forming the box. Default is 3f.
     * @param phase If `true`, disables depth testing for the box. Default is `false`.
     */
    fun drawCustomBox(x: Double, y: Double, z: Double, scale: Double, color: Color, thickness: Float = 3f, phase: Boolean = false) {
        drawCustomBox(x, scale, y, scale, z, scale, color, thickness, phase)
    }

    /**
     * Draws a custom box in the 3D world space using block coordinates.
     *
     * @param pos The block position of the box.
     * @param color The color of the box (must be in the range of 0-255).
     * @param thickness The thickness of the lines forming the box. Default is 3f.
     * @param phase If `true`, disables depth testing for the box. Default is `false`.
     */
    fun drawCustomBox(pos: BlockPos, color: Color, thickness: Float = 3f, phase: Boolean = false) {
        drawCustomBox(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 1.0, color, thickness, phase)
    }

    /**
     * Draws a custom box in the 3D world space.
     *
     * @param x X-coordinate of the box.
     * @param y Y-coordinate of the box.
     * @param z Z-coordinate of the box.
     * @param width The width of the box.
     * @param height The height of the box.
     * @param color The color of the box (must be in the range of 0-255).
     * @param thickness The thickness of the lines forming the box. Default is 3f.
     * @param phase If `true`, disables depth testing for the box. Default is `false`.
     */
    fun drawCustomBox(x: Double, y: Double, z: Double, width: Double, height: Double, color: Color, thickness: Float = 3f, phase: Boolean = false) {
        drawCustomBox(x, width, y, height, z, width, color, thickness, phase)
    }

    /**
     * Draws a custom box in the 3D world space.
     *
     * @param x X-coordinate of the box.
     * @param y Y-coordinate of the box.
     * @param z Z-coordinate of the box.
     * @param scale The scale of the box.
     * @param color The color of the box (must be in the range of 0-255).
     * @param thickness The thickness of the lines forming the box. Default is 3f.
     * @param phase If `true`, disables depth testing for the box. Default is `false`.
     */
    fun drawCustomBox(x: Number, y: Number, z: Number, scale: Number, color: Color, thickness: Number = 3f, phase: Boolean = false) {
        drawCustomBox(x.toDouble(), scale.toDouble(), y.toDouble(), scale.toDouble(), z.toDouble(), scale.toDouble(), color, thickness.toFloat(), phase)
    }

    /**
     * Draws a custom box in the 3D world space.
     *
     * @param x X-coordinate of the box.
     * @param xWidth The width of the box along the x-axis.
     * @param y Y-coordinate of the box.
     * @param yWidth The width of the box along the y-axis.
     * @param z Z-coordinate of the box.
     * @param zWidth The width of the box along the z-axis.
     * @param color The color of the box (must be in the range of 0-255).
     * @param thickness The thickness of the lines forming the box. Default is 3f.
     * @param phase If `true`, disables depth testing for the box. Default is `false`.
     */
    fun drawCustomBox(x: Double, xWidth: Double, y: Double, yWidth: Double, z: Double, zWidth: Double, color: Color, thickness: Float = 3f, phase: Boolean) {
        GlStateManager.pushMatrix()
        color.bindColor()
        GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        if (phase) GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        GL11.glLineWidth(thickness)

        val x1 = x + xWidth
        val y1 = y + yWidth
        val z1 = z + zWidth

        worldRenderer {
            begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
            pos(x1, y1, z1).endVertex()
            pos(x1, y1, z).endVertex()
            pos(x, y1, z).endVertex()
            pos(x, y1, z1).endVertex()
            pos(x1, y1, z1).endVertex()
            pos(x1, y, z1).endVertex()
            pos(x1, y, z).endVertex()
            pos(x, y, z).endVertex()
            pos(x, y, z1).endVertex()
            pos(x, y, z).endVertex()
            pos(x, y1, z).endVertex()
            pos(x, y, z).endVertex()
            pos(x1, y, z).endVertex()
            pos(x1, y1, z).endVertex()
            pos(x1, y, z).endVertex()
            pos(x1, y, z1).endVertex()
            pos(x, y, z1).endVertex()
            pos(x, y1, z1).endVertex()
            pos(x1, y1, z1).endVertex()
        }

        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
    }

    /**
     * Draws a filled box in the 3D world space using block coordinates.
     *
     * @param pos The block position of the box.
     * @param color The color of the box.
     * @param phase If `true`, disables depth testing for the box. Default is `false`.
     */
    fun drawFilledBox(pos: BlockPos, color: Color, phase: Boolean = false) {
        drawFilledBox(AxisAlignedBB(pos, pos.add(1, 1, 1)).expand(0.001, 0.001, 0.001), color, phase)
    }

    /**
     * Draws a filled box in the 3D world space.
     *
     * @param ab The `AxisAlignedBB` representing the box.
     * @param color The color of the box.
     * @param phase If `true`, disables depth testing for the box. Default is `false`.
     */
    fun drawFilledBox(ab: AxisAlignedBB, color: Color, phase: Boolean = false) {
        val (viewerX, viewerY, viewerZ) = viewerPos
        val aabb = AxisAlignedBB(
            ab.minX - viewerX,
            ab.minY - viewerY,
            ab.minZ - viewerZ,
            ab.maxX - viewerX,
            ab.maxY - viewerY,
            ab.maxZ - viewerZ
        )
        GlStateManager.pushMatrix()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        if (phase) GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        color.bindColor()
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

    fun drawStringInWorld(
        text: String,
        vec3: Vec3,
        color: Int = 0xffffffff.toInt(),
        renderBlackBox: Boolean = true,
        increase: Boolean = true,
        depthTest: Boolean = true,
        scale: Float = 1f,
        shadow: Boolean = true
    ) {
        var lScale = scale

        val renderPos = getRenderPos(vec3)

        if (increase) {
            val distance = sqrt(renderPos.xCoord * renderPos.xCoord + renderPos.yCoord * renderPos.yCoord + renderPos.zCoord * renderPos.zCoord)
            val multiplier = distance / 120f
            lScale *= 0.45f * multiplier.toFloat()
        }

        if (!depthTest) {
            GlStateManager.disableDepth()
            GlStateManager.depthMask(false)
        }

        val xMultiplier = if (mc.gameSettings.thirdPersonView == 2) -1 else 1

        GlStateManager.color(1f, 1f, 1f, 0.5f)
        GlStateManager.pushMatrix()
        GlStateManager.translate(renderPos.xCoord, renderPos.yCoord, renderPos.zCoord)
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewX * xMultiplier, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(-lScale, -lScale, lScale)
        GlStateManager.disableLighting()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)

        val textWidth = mc.fontRendererObj.getStringWidth(text)

        if (renderBlackBox) {
            val j = textWidth / 2
            GlStateManager.disableTexture2D()
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
            worldRenderer.pos(-j - 1.0, -1.0, .0).color(.0f, .0f, .0f, 0.25f).endVertex()
            worldRenderer.pos(-j - 1.0, 8.0, .0).color(.0f, .0f, .0f, 0.25f).endVertex()
            worldRenderer.pos(j + 1.0, 8.0, .0).color(.0f, .0f, .0f, 0.25f).endVertex()
            worldRenderer.pos(j + 1.0, -1.0, .0).color(.0f, .0f, .0f, 0.25f).endVertex()
            tessellator.draw()
            GlStateManager.enableTexture2D()
        }

        if (shadow) mc.fontRendererObj.drawStringWithShadow(text, -textWidth / 2f, 0f, color)
        else mc.fontRendererObj.drawString(text, -textWidth / 2, 0, color)

        if (!depthTest) {
            GlStateManager.enableDepth()
            GlStateManager.depthMask(true)
        }

        GlStateManager.resetColor()
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }


    fun renderCustomBeacon(title: String, pos: Vec3, color: Color, partialTicks: Float = this.partialTicks) {
        renderCustomBeacon(title, pos.xCoord, pos.yCoord, pos.zCoord, color, partialTicks)
    }

    fun renderCustomBeacon(title: String, x: Double, y: Double, z: Double, color: Color, partialTicks: Float, beacon: Boolean = true) {
        val distX = x - mc.renderManager.viewerPosX
        val distY = y - mc.renderManager.viewerPosY - mc.renderViewEntity.eyeHeight
        val distZ = z - mc.renderManager.viewerPosZ
        val dist = sqrt(distX * distX + distY * distY + distZ * distZ)

        drawCustomBox(floor(x), floor(y), floor(z), 1.0, color.withAlpha(1f), 3f, true)

        drawStringInWorld(
            "$title §r§f(§3${dist.toInt()}m§f)",
            Vec3(floor(x) + .5, floor(y) + 1.7 + dist / 30, floor(z) + .5),
            color.rgba,
            renderBlackBox = true,
            increase = false,
            depthTest = false,
            max(0.03, dist / 180.0).toFloat()
        )
        val a = min(1f, max(0f, dist.toFloat()) / 60f)
        if (beacon) renderBeaconBeam(floor(x), .0, floor(z), color, a, true, partialTicks)
    }

    fun WorldRenderer.color(color: Color) { // local function is used to simplify this.
        this.color(color.r / 255f, color.g / 255f, color.b / 255f, color.alpha).endVertex()
    }


    fun draw3DLine(pos1: Vec3, pos2: Vec3, color: Color, lineWidth: Int, depth: Boolean, partialTicks: Float) {
        val render: Entity = mc.renderViewEntity

        val realX: Double = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks
        val realY: Double = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks
        val realZ: Double = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks

        GlStateManager.pushMatrix()
        color.bindColor()
        GlStateManager.translate(-realX, -realY, -realZ)
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

        GlStateManager.translate(realX, realY, realZ)
        if (!depth) {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GlStateManager.depthMask(true)
        }

        postDraw()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
    }

    private val beaconBeam = ResourceLocation("textures/entity/beacon_beam.png")

    private fun renderBeaconBeam(x: Double, y: Double, z: Double, color: Color, a: Float, depthCheck: Boolean, partialTicks: Float) {
        val height = 300
        val bottomOffset = 0
        val topOffset = bottomOffset + height

        if (!depthCheck) GlStateManager.disableDepth()

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
        GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)

        fun WorldRenderer.color(alpha: Float = a) { // local function is used to simplify this.
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

            tessellator.draw()
            GlStateManager.disableBlend()
            GlStateManager.resetColor()
            GlStateManager.popMatrix()
            GlStateManager.disableCull()
            GlStateManager.pushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)

            val d12 = -1 + d1
            val d13 = height + d12

            begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
            pos(x + 0.2, y + topOffset, z + 0.2).tex(1.0, d13).color(.25f * a)
            pos(x + 0.2, y + bottomOffset, z + 0.2).tex(1.0, d12).color(.25f * a)
            pos(x + 0.8, y + bottomOffset, z + 0.2).tex(0.0, d12).color(.25f * a)
            pos(x + 0.8, y + topOffset, z + 0.2).tex(0.0, d13).color(.25f * a)
            pos(x + 0.8, y + topOffset, z + 0.8).tex(1.0, d13).color(.25f * a)
            pos(x + 0.8, y + bottomOffset, z + 0.8).tex(1.0, d12).color(.25f * a)
            pos(x + 0.2, y + bottomOffset, z + 0.8).tex(0.0, d12).color(.25f * a)
            pos(x + 0.2, y + topOffset, z + 0.8).tex(0.0, d13).color(.25f * a)
            pos(x + 0.8, y + topOffset, z + 0.2).tex(1.0, d13).color(.25f * a)
            pos(x + 0.8, y + bottomOffset, z + 0.2).tex(1.0, d12).color(.25f * a)
            pos(x + 0.8, y + bottomOffset, z + 0.8).tex(0.0, d12).color(.25f * a)
            pos(x + 0.8, y + topOffset, z + 0.8).tex(0.0, d13).color(.25f * a)
            pos(x + 0.2, y + topOffset, z + 0.8).tex(1.0, d13).color(.25f * a)
            pos(x + 0.2, y + bottomOffset, z + 0.8).tex(1.0, d12).color(.25f * a)
            pos(x + 0.2, y + bottomOffset, z + 0.2).tex(0.0, d12).color(.25f * a)
            pos(x + 0.2, y + topOffset, z + 0.2).tex(0.0, d13).color(.25f * a)

            endVertex()
        }
        tessellator.draw()
        GlStateManager.resetColor()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
        GlStateManager.enableLighting()
        GlStateManager.enableTexture2D()
        if (!depthCheck) GlStateManager.enableDepth()
    }


    private fun getRenderPos(vec: Vec3): Vec3 {
        val renderPosX = mc.renderManager.viewerPosX
        val renderPosY = mc.renderManager.viewerPosY
        val renderPosZ = mc.renderManager.viewerPosZ
        return Vec3(vec.xCoord - renderPosX, vec.yCoord - renderPosY, vec.zCoord - renderPosZ)
    }

    fun drawCylinder(
        pos: Vec3, baseRadius: Float, topRadius: Float, height: Float,
        slices: Int, stacks: Int, rot1: Float, rot2: Float, rot3: Float,
        color: Color, phase: Boolean = false, linemode: Boolean = false
    ) {
        drawCylinder(pos, baseRadius, topRadius, height, slices, stacks, rot1, rot2, rot3, color.r / 255f, color.g / 255f, color.b / 255f, color.alpha, linemode)
    }

    fun drawCylinder(
        pos: Vec3, baseRadius: Float, topRadius: Float, height: Float,
        slices: Int, stacks: Int, rot1: Float, rot2: Float, rot3: Float,
        r: Float, g: Float, b: Float, a: Float, phase: Boolean = false, linemode: Boolean = false
    ) {
        val renderPos = getRenderPos(pos)
        val x = renderPos.xCoord
        val y = renderPos.yCoord
        val z = renderPos.zCoord

        GlStateManager.pushMatrix()
        GL11.glLineWidth(2.0f)
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting() // important for some reason
        GlStateManager.blendFunc(770, 771)
        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()

        if (phase) GlStateManager.disableDepth()

        GlStateManager.color(r, g, b, a)
        GlStateManager.translate(x, y, z)
        GlStateManager.rotate(rot1, 1f, 0f, 0f)
        GlStateManager.rotate(rot2, 0f, 0f, 1f)
        GlStateManager.rotate(rot3, 0f, 1f, 0f)

        val cyl = Cylinder()
        cyl.drawStyle = GLU.GLU_LINE
        if (linemode) cyl.draw(baseRadius, topRadius, height, slices, stacks)
        else Cylinder().draw(baseRadius, topRadius, height, slices, stacks)

        GlStateManager.enableCull()
        GlStateManager.disableBlend()
        GlStateManager.enableLighting()
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.resetColor()
        if (phase) GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    fun loadImage(path: String): BufferedImage {
        val resource = this::class.java.getResource(path)
            ?: return BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB) // poor fix for debug mode
        return ImageIO.read(resource)
    }

    fun drawTexturedModalRect(x: Int, y: Int, width: Int, height: Int) {
        GlStateManager.enableTexture2D()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(0.0, 1.0).endVertex()
        worldRenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(1.0, 1.0).endVertex()
        worldRenderer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(1.0, 0.0).endVertex()
        worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(0.0, 0.0).endVertex()
        tessellator.draw()
    }

}