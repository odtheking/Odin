package me.odinmain.features.impl.render

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.features.Module
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils.partialTicks
import net.minecraft.client.model.ModelDragon
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin

object PetDragon : Module(
    name = "Pet Dragon",
    description = "Renders your own pet dragon."
) {

    private val size by NumberSetting("Size", 1f, 0.1f, 2f, 0.01f, desc = "")
    private val height by NumberSetting("Height", 1f, -10f, 10f, 0.01f, desc = "")
    private val distance by NumberSetting("Distance", 2f, 0f, 10f, 0.01f, desc = "")
    private val direction by NumberSetting("Direction", 0f, 0f, 360f, increment = 1f, desc = "")
    private val animationSpeed by NumberSetting("Animation Speed", 1f, 0.1f, 2f, 0.01f, desc = "")
    private val instantRotation by BooleanSetting("Instant Rotation", desc = "")
    private val onlyF5 by BooleanSetting("Only F5", desc = "")
    private val color by ColorSetting("Color", default = Colors.WHITE, desc = "")

    var animTime = 0f
    var prevAnimTime = 0f

    var posX = 0.0
    var posY = 0.0
    var posZ = 0.0
    var rotationYaw = 0f

    private val ringBuffer = Array(64) { DoubleArray(3) }
    private var bufferIndex = -1

    fun getMovementOffsets(p_70974_1_: Int, p_70974_2_: Float): DoubleArray {
        var p_70974_2_ = p_70974_2_
        p_70974_2_ = 1.0f - p_70974_2_
        val i: Int = bufferIndex - p_70974_1_ * 1 and 0x3F
        val j: Int = bufferIndex - p_70974_1_ * 1 - 1 and 0x3F
        val adouble = DoubleArray(3)
        var d0 = this.ringBuffer[i][0]
        var d1 = MathHelper.wrapAngleTo180_double(this.ringBuffer[j][0] - d0)
        adouble[0] = d0 + d1 * p_70974_2_.toDouble()
        d0 = this.ringBuffer[i][1]
        d1 = this.ringBuffer[j][1] - d0
        adouble[1] = d0 + d1 * p_70974_2_.toDouble()
        adouble[2] = this.ringBuffer[i][2] + (this.ringBuffer[j][2] - this.ringBuffer[i][2]) * p_70974_2_.toDouble()
        return adouble
    }

    class PetDragonModel() : ModelDragon(0.0f) {

        override fun render(
            entityIn: Entity?,
            limbSwing: Float,
            limbSwingAmount: Float,
            ageInTicks: Float,
            netHeadYaw: Float,
            headPitch: Float,
            scale: Float
        ) {
            GlStateManager.pushMatrix()
            //GlStateManager.shadeModel(7424)
            //RenderHelper.enableStandardItemLighting()
            //mc.entityRenderer.enableLightmap()
            GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alphaFloat)
            GL11.glFrontFace(GL11.GL_CW)

            var u: Float
            GlStateManager.pushMatrix()
            val k = prevAnimTime + (animTime - prevAnimTime) * partialTicks
            this.jaw.rotateAngleX = (sin((k * Math.PI.toFloat() * 2.0f).toDouble()) + 1.0).toFloat() * 0.2f
            var l = (sin((k * Math.PI.toFloat() * 2.0f - 1.0f).toDouble()) + 1.0).toFloat()
            l = (l * l * 1.0f + l * 2.0f) * 0.05f
            GlStateManager.translate(0.0f, l - 2.0f, -3.0f)
            GlStateManager.rotate(l * 2.0f, 1.0f, 0.0f, 0.0f)
            var m: Float
            var n = 0.0f
            val o = 1.5f
            var ds = getMovementOffsets(6, partialTicks)
            val p: Float = this.updateRotations(
                getMovementOffsets(
                    5,
                    partialTicks
                )[0] - getMovementOffsets(10, partialTicks)[0]
            )
            val q: Float =
                this.updateRotations(getMovementOffsets(5, partialTicks)[0] + (p / 2.0f).toDouble())
            var r = k * Math.PI.toFloat() * 2.0f
            m = 20.0f
            var s = -12.0f
            for (t in 0..4) {
                val es = getMovementOffsets(5 - t, partialTicks)
                u = cos((t.toFloat() * 0.45f + r).toDouble()).toFloat() * 0.15f
                this.spine.rotateAngleY = this.updateRotations(es[0] - ds[0]) * Math.PI.toFloat() / 180.0f * o
                this.spine.rotateAngleX = u + (es[1] - ds[1]).toFloat() * Math.PI.toFloat() / 180.0f * o * 5.0f
                this.spine.rotateAngleZ = -this.updateRotations(es[0] - q.toDouble()) * Math.PI.toFloat() / 180.0f * o
                this.spine.rotationPointY = m
                this.spine.rotationPointZ = s
                this.spine.rotationPointX = n
                m = (m.toDouble() + sin(this.spine.rotateAngleX.toDouble()) * 10.0).toFloat()
                s =
                    (s.toDouble() - cos(this.spine.rotateAngleY.toDouble()) * cos(this.spine.rotateAngleX.toDouble()) * 10.0).toFloat()
                n =
                    (n.toDouble() - sin(this.spine.rotateAngleY.toDouble()) * cos(this.spine.rotateAngleX.toDouble()) * 10.0).toFloat()
                this.spine.render(scale)
            }
            this.head.rotationPointY = m
            this.head.rotationPointZ = s
            this.head.rotationPointX = n
            var fs = getMovementOffsets(0, partialTicks)
            this.head.rotateAngleY = this.updateRotations(fs[0] - ds[0]) * Math.PI.toFloat() / 180.0f * 1.0f
            this.head.rotateAngleZ = -this.updateRotations(fs[0] - q.toDouble()) * Math.PI.toFloat() / 180.0f * 1.0f
            this.head.render(scale)
            GlStateManager.pushMatrix()
            GlStateManager.translate(0.0f, 1.0f, 0.0f)
            GlStateManager.rotate(-p * o * 1.0f, 0.0f, 0.0f, 1.0f)
            GlStateManager.translate(0.0f, -1.0f, 0.0f)
            this.body.rotateAngleZ = 0.0f
            this.body.render(scale)
            for (v in 0..1) {
                GlStateManager.enableCull()
                u = k * Math.PI.toFloat() * 2.0f
                this.wing.rotateAngleX = 0.125f - cos(u.toDouble()).toFloat() * 0.2f
                this.wing.rotateAngleY = 0.25f
                this.wing.rotateAngleZ = (sin(u.toDouble()) + 0.125).toFloat() * 0.8f
                this.wingTip.rotateAngleZ = -((sin((u + 2.0f).toDouble()) + 0.5).toFloat()) * 0.75f
                this.rearLeg.rotateAngleX = 1.0f + l * 0.1f
                this.rearLegTip.rotateAngleX = 0.5f + l * 0.1f
                this.rearFoot.rotateAngleX = 0.75f + l * 0.1f
                this.frontLeg.rotateAngleX = 1.3f + l * 0.1f
                this.frontLegTip.rotateAngleX = -0.5f - l * 0.1f
                this.frontFoot.rotateAngleX = 0.75f + l * 0.1f
                this.wing.render(scale)
                this.frontLeg.render(scale)
                this.rearLeg.render(scale)
                GlStateManager.scale(-1.0f, 1.0f, 1.0f)
                if (v != 0) continue
                GlStateManager.cullFace(1028)
            }
            GlStateManager.popMatrix()
            GlStateManager.cullFace(1029)
            GlStateManager.disableCull()
            var w = -(sin((k * Math.PI.toFloat() * 2.0f).toDouble()).toFloat()) * 0.0f
            r = k * Math.PI.toFloat() * 2.0f
            m = 10.0f
            s = 60.0f
            n = 0.0f
            ds = getMovementOffsets(11, partialTicks)
            for (x in 0..11) {
                fs = getMovementOffsets(12 + x, partialTicks)
                w = (w.toDouble() + sin((x.toFloat() * 0.45f + r).toDouble()) * 0.05).toFloat()
                this.spine.rotateAngleY =
                    (this.updateRotations(fs[0] - ds[0]) * o + 180.0f) * Math.PI.toFloat() / 180.0f
                this.spine.rotateAngleX = w + (fs[1] - ds[1]).toFloat() * Math.PI.toFloat() / 180.0f * o * 5.0f
                this.spine.rotateAngleZ = this.updateRotations(fs[0] - q.toDouble()) * Math.PI.toFloat() / 180.0f * o
                this.spine.rotationPointY = m
                this.spine.rotationPointZ = s
                this.spine.rotationPointX = n
                m = (m.toDouble() + sin(this.spine.rotateAngleX.toDouble()) * 10.0).toFloat()
                s =
                    (s.toDouble() - cos(this.spine.rotateAngleY.toDouble()) * cos(this.spine.rotateAngleX.toDouble()) * 10.0).toFloat()
                n =
                    (n.toDouble() - sin(this.spine.rotateAngleY.toDouble()) * cos(this.spine.rotateAngleX.toDouble()) * 10.0).toFloat()
                this.spine.render(scale)
            }
            GlStateManager.popMatrix()

            GL11.glFrontFace(GL11.GL_CCW)
            //mc.entityRenderer.disableLightmap()
            GlStateManager.popMatrix()
        }

        private fun updateRotations(angle: Double): Float {
            var f = angle.toFloat()
            while (f >= 180.0f) f -= 360.0f
            while (f < -180.0f) f += 360.0f
            return f
        }
    }

    val dragonModel = PetDragonModel()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        prevAnimTime = animTime
        animTime += 0.1f * animationSpeed

        if (instantRotation) return

        this.rotationYaw = mc.thePlayer?.rotationYaw ?: return

        this.rotationYaw = -MathHelper.wrapAngleTo180_float(this.rotationYaw)
        if (this.bufferIndex < 0) {
            for (i in this.ringBuffer.indices) {
                this.ringBuffer[i][0] = this.rotationYaw.toDouble()
                this.ringBuffer[i][1] = this.posY
            }
        }
        if (++bufferIndex == this.ringBuffer.size) {
            bufferIndex = 0
        }
        this.ringBuffer[bufferIndex][0] = this.rotationYaw.toDouble()
        this.ringBuffer[bufferIndex][1] = this.posY
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (onlyF5 && mc.gameSettings.thirdPersonView == 0) return
        val player = mc.thePlayer ?: return

        val playerYawRadians = Math.toRadians(player.rotationYaw.toDouble() + direction)
        posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX + distance * cos(playerYawRadians)
        posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY + height
        posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ + distance * sin(playerYawRadians)

        GlStateManager.pushMatrix()
        GlStateManager.translate(posX, posY, posZ)

        GlStateManager.rotate(-mc.thePlayer.rotationYaw, 0.0f, 1.0f, 0.0f)
        GlStateManager.scale(-0.1f * size, -0.1f * size, -0.1f * size)

        mc.textureManager.bindTexture(ResourceLocation("textures/entity/enderdragon/dragon.png"))
        dragonModel.render(null, 0f, 0f, 0f, 0f, 0f, 0.0625f)

        GlStateManager.popMatrix()
    }
}