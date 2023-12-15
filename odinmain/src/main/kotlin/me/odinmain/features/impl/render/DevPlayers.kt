package me.odinmain.features.impl.render


import kotlinx.coroutines.launch
import me.odinmain.OdinMain
import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.render.ClickGUIModule.devSize
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.fetchURLData
import me.odinmain.utils.render.Color
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin


object DevPlayers {

    data class Dev(val xScale: Float = 1f, val yScale: Float = 1f, val zScale: Float = 1f,
                   val wings: Boolean = false, val wingsColor: Color = Color(255, 255, 255)
    )

    val devs = HashMap<String, Dev>()

    private fun updateDevs() {
        val webhook: String = fetchURLData("https://pastebin.com/raw/9Lq8hKTQ")

        val keyValuePairs = webhook.split("?")
        for (keyValuePair in keyValuePairs) {
            val parts = keyValuePair.split(" to ")

            if (parts.size == 2) {

                val key = parts[0].trim(' ', '"')
                val valueString = parts[1].trim()

                val regex = Regex("""Dev\((\d+\.*\d*), (\d+\.*\d*), (\d+\.*\d*)\), (\w+), Color\((\d+), (\d+), (\d+)\)\)""")
                val match = regex.find(valueString) ?: return

                val (x, y, z, wings, wingRed, wingGreen, wingBlue) = match.destructured
                val dev = Dev(x.toFloat(), y.toFloat(), z.toFloat(), wings.toBoolean(), Color(wingRed.toInt(), wingGreen.toInt(), wingBlue.toInt()))

                devs[key] = dev

            }
        }
    }

    init {
        Executor(delay = 15000) {
            OdinMain.scope.launch { updateDevs() }
        }.register()
    }

    fun preRenderCallbackScaleHook(entityLivingBaseIn: AbstractClientPlayer ) {
        if (!devs.containsKey(entityLivingBaseIn.name)) return
        if (!devSize && entityLivingBaseIn.name == mc.thePlayer.name) return
        val dev = devs[entityLivingBaseIn.name]
        if (dev != null) { GlStateManager.scale(dev.xScale, dev.yScale, dev.zScale) }
    }

    private val dragonWingTextureLocation = ResourceLocation("textures/entity/enderdragon/dragon.png")

    object DragonWings : ModelBase() {

        private val wing: ModelRenderer
        private val wingTip: ModelRenderer

        init {
            textureWidth = 256
            textureHeight = 256
            setTextureOffset("wing.skin", -56, 88)
            setTextureOffset("wingtip.skin", -56, 144)
            setTextureOffset("wing.bone", 112, 88)
            setTextureOffset("wingtip.bone", 112, 136)

            wing = ModelRenderer(this, "wing")
            wing.setRotationPoint(-12.0f, 5.0f, 2.0f)
            wing.addBox("bone", -56.0f, -4.0f, -4.0f, 56, 8, 8)
            wing.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56)
            wingTip = ModelRenderer(this, "wingtip")
            wingTip.setRotationPoint(-56.0f, 0.0f, 0.0f)
            wingTip.addBox("bone", -56.0f, -2.0f, -2.0f, 56, 4, 4)
            wingTip.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56)
            wing.addChild(wingTip)
        }

        fun renderWings(player: EntityPlayer, partialTicks: Float, dev: Dev) {
            val rotation = this.interpolate(player.prevRenderYawOffset, player.renderYawOffset, partialTicks)

            GlStateManager.pushMatrix()
            GlStateManager.scale(-0.2 * dev.xScale, -0.2 * dev.yScale, 0.2 * dev.zScale)
            GlStateManager.rotate(180 + rotation, 0f, 1f, 0f)
            GlStateManager.translate(0.0, -(1.25 / 0.2f), 0.0)
            GlStateManager.translate(0.0, 0.0, 0.1 / 0.2 / dev.zScale)

            if (player.isSneaking) {
                GlStateManager.translate(0.0, (0.125 / 1.0) * dev.yScale, 0.0)
            }

            GlStateManager.color(dev.wingsColor.r.toFloat()/255, dev.wingsColor.g.toFloat()/255, dev.wingsColor.b.toFloat()/255, 1f)
            mc.textureManager.bindTexture(dragonWingTextureLocation)

            for (j in 0..1) {
                GlStateManager.enableCull()
                val f11 = System.currentTimeMillis() % 1000 / 1000f * Math.PI.toFloat() * 2.0f
                wing.rotateAngleX = Math.toRadians(-80.0).toFloat() - cos(f11.toDouble()).toFloat() * 0.2f
                wing.rotateAngleY = Math.toRadians(20.0).toFloat() + sin(f11.toDouble()).toFloat() * 0.4f
                wing.rotateAngleZ = Math.toRadians(20.0).toFloat()
                wingTip.rotateAngleZ = -(sin((f11 + 2.0f).toDouble()) + 0.5).toFloat() * 0.75f
                wing.render(0.0625f)
                GlStateManager.scale(-1.0f, 1.0f, 1.0f)
                if (j == 0) {
                    GlStateManager.cullFace(1028)
                }
            }

            GlStateManager.cullFace(1029)
            GlStateManager.disableCull()
            GlStateManager.color(1f, 1f, 1f, 1f)
            GlStateManager.popMatrix()
            GlStateManager.disableAlpha()

        }

        private fun interpolate(yaw1: Float, yaw2: Float, percent: Float): Float {
            var f = (yaw1 + (yaw2 - yaw1) * percent) % 360
            if (f < 0) {
                f += 360f
            }
            return f
        }

    }

    @SubscribeEvent
    fun onRenderPlayer(event: RenderPlayerEvent.Post) {
        if (!devs.containsKey(event.entity.name)) return
        if (!devSize && event.entity.name == mc.thePlayer.name) return
        val dev = devs[event.entity.name]
        if (dev != null) DragonWings.renderWings(event.entityPlayer, event.partialRenderTick, dev)
    }

}