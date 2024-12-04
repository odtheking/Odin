package me.odinmain.features.impl.render


import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.features.impl.render.ClickGUIModule.devSize
import me.odinmain.utils.getDataFromServer
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.translate
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.lang.reflect.Type
import kotlin.math.cos
import kotlin.math.sin

object DevPlayers {
    private var devs: HashMap<String, DevPlayer> = HashMap()
    val isDev get() = devs.containsKey(mc.session?.username)

    data class DevPlayer(val xScale: Float = 1f, val yScale: Float = 1f, val zScale: Float = 1f,
                         val wings: Boolean = false, val wingsColor: Color = Color(255, 255, 255))
    data class DevData(val devName: String, val wingsColor: Triple<Int, Int, Int>, val size: Triple<Float, Float, Float>, val wings: Boolean)

    @Suppress("UNCHECKED_CAST")
    class DevDeserializer : JsonDeserializer<DevData> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): DevData {
            val jsonObject = json?.asJsonObject
            val devName = jsonObject?.get("DevName")?.asString
            val wingsColorJsonArray = jsonObject?.get("WingsColor")?.asJsonArray
            val wingsColorTriple = Triple(
                wingsColorJsonArray?.get(0)?.asInt ?: 0,
                wingsColorJsonArray?.get(1)?.asInt ?: 0,
                wingsColorJsonArray?.get(2)?.asInt ?: 0
            )
            val sizeJsonArray = jsonObject?.get("Size")?.asJsonArray
            val sizeTriple = Triple(
                sizeJsonArray?.get(0)?.asFloat ?: 0,
                sizeJsonArray?.get(1)?.asFloat ?: 0,
                sizeJsonArray?.get(2)?.asFloat ?: 0
            )
            val wings = jsonObject?.get("Wings")?.asBoolean == true

            return DevData(devName ?: "", wingsColorTriple, sizeTriple as Triple<Float, Float, Float>, wings)
        }
    }

    private fun convertDecimalToNumber(s: String): String {
        val pattern = Regex("""Decimal\('(-?\d+(?:\.\d+)?)'\)""")

        return s.replace(pattern) { match -> match.groupValues[1] }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun updateDevs(): HashMap<String, DevPlayer> {
        runBlocking(scope.coroutineContext) {
            val data = convertDecimalToNumber(getDataFromServer("https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/")).ifEmpty { return@runBlocking }
            val gson = GsonBuilder().registerTypeAdapter(DevData::class.java, DevDeserializer())?.create() ?: return@runBlocking
            gson.fromJson(data, Array<DevData>::class.java).forEach {
                devs[it.devName] = DevPlayer(it.size.first, it.size.second, it.size.third, it.wings, Color(it.wingsColor.first, it.wingsColor.second, it.wingsColor.third))
            }
        }
        return devs
    }

    init {
        updateDevs()
    }

    fun preRenderCallbackScaleHook(entityLivingBaseIn: AbstractClientPlayer) {
        if (!devs.containsKey(entityLivingBaseIn.name)) return
        if (!devSize && entityLivingBaseIn.name == mc.thePlayer.name) return
        val dev = devs[entityLivingBaseIn.name] ?: return
        GlStateManager.scale(dev.xScale, dev.yScale, dev.zScale)
        if (dev.yScale < 0) GlStateManager.translate(0f, dev.yScale * -2, 0f)
    }

    @SubscribeEvent
    fun onRenderPlayer(event: RenderPlayerEvent.Post) {
        if (!devs.containsKey(event.entity.name)) return
        if (!devSize && event.entity.name == mc.thePlayer.name) return
        val dev = devs[event.entity.name] ?: return
        if (!dev.wings) return
        DragonWings.renderWings(event.entityPlayer, event.partialRenderTick, dev)
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

        fun renderWings(player: EntityPlayer, partialTicks: Float, dev: DevPlayer) {
            val rotation = this.interpolate(player.prevRenderYawOffset, player.renderYawOffset, partialTicks)

            GlStateManager.pushMatrix()
            val x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
            val y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
            val z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks
            if (dev.yScale < 0) translate(0f, dev.yScale * -2, 0f)

            GlStateManager.translate(-mc.renderManager.viewerPosX + x, -mc.renderManager.viewerPosY + y, -mc.renderManager.viewerPosZ + z)
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
                wing.rotateAngleX = Math.toRadians(-80.0).toFloat() - cos(f11) * 0.2f
                wing.rotateAngleY = Math.toRadians(20.0).toFloat() + sin(f11) * 0.4f
                wing.rotateAngleZ = Math.toRadians(20.0).toFloat()
                wingTip.rotateAngleZ = -(sin((f11 + 2.0f)) + 0.5).toFloat() * 0.75f
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
        }

        private fun interpolate(yaw1: Float, yaw2: Float, percent: Float): Float {
            var f = (yaw1 + (yaw2 - yaw1) * percent) % 360
            if (f < 0) {
                f += 360f
            }
            return f
        }
    }
}