package me.odinmain.features.impl.render


import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.mojang.authlib.GameProfile
import kotlinx.coroutines.runBlocking
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.features.impl.render.ClickGUIModule.devSize
import me.odinmain.utils.downloadFile
import me.odinmain.utils.getDataFromServer
import me.odinmain.utils.render.Color
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.net.URL
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.createDirectories
import kotlin.math.cos
import kotlin.math.sin

object DevPlayers {
    private var devs: HashMap<String, DevPlayer> = HashMap()
    val isDev get() = devs.containsKey(mc.session?.username)

    data class DevPlayer(val xScale: Float = 1f, val yScale: Float = 1f, val zScale: Float = 1f,
                         val wings: Boolean = false, val wingsColor: Color = Color(255, 255, 255), var capeLocation: ResourceLocation? = null)
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

    @JvmStatic
    fun preRenderCallbackScaleHook(entityLivingBaseIn: AbstractClientPlayer) {
        if (!devs.containsKey(entityLivingBaseIn.name)) return
        if (!devSize && entityLivingBaseIn.name == mc.thePlayer.name) return
        val dev = devs[entityLivingBaseIn.name] ?: return
        if (dev.yScale < 0) GlStateManager.translate(0f, dev.yScale * 2, 0f)
        GlStateManager.scale(dev.xScale, dev.yScale, dev.zScale)
    }

    @SubscribeEvent
    fun onRenderPlayer(event: RenderPlayerEvent.Post) {
        if (!devs.containsKey(event.entity.name)) return
        if (!devSize && event.entity.name == mc.thePlayer.name) return
        val dev = devs[event.entity.name] ?: return
        if (!dev.wings) return
        DragonWings.renderWings(event.entityPlayer, event.partialRenderTick, dev)
    }

    object DragonWings : ModelBase() {

        private val dragonWingTextureLocation = ResourceLocation("textures/entity/enderdragon/dragon.png")
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
            if (dev.yScale < 0) GlStateManager.translate(0f, dev.yScale * -2, 0f)

            GlStateManager.translate(-mc.renderManager.viewerPosX + x, -mc.renderManager.viewerPosY + y, -mc.renderManager.viewerPosZ + z)
            GlStateManager.scale(-0.2, -0.2, 0.2)
            GlStateManager.scale(dev.xScale, dev.yScale, dev.zScale)
            GlStateManager.rotate(180 + rotation, 0f, 1f, 0f)
            GlStateManager.translate(0.0, -(1.25 / 0.2f), 0.0)
            GlStateManager.translate(0.0, 0.0, 0.25)

            if (player.isSneaking) {
                GlStateManager.rotate(45f, 1f, 0f, 0f)
                GlStateManager.translate(0.0, 1.0, -0.5)
            }

            GlStateManager.color(dev.wingsColor.r.toFloat()/255, dev.wingsColor.g.toFloat()/255, dev.wingsColor.b.toFloat()/255, 1f)
            mc.textureManager.bindTexture(dragonWingTextureLocation)

            for (j in 0..1) {
                GlStateManager.enableCull()
                GlStateManager.rotate(20f, 0f, 1f, 0f)
                val f11 = System.currentTimeMillis() % 1000 / 1000f * Math.PI.toFloat() * 2.0f
                wing.rotateAngleX = Math.toRadians(-80.0).toFloat() - cos(f11) * 0.2f
                wing.rotateAngleY = Math.toRadians(20.0).toFloat() + sin(f11) * 0.4f
                wing.rotateAngleZ = Math.toRadians(20.0).toFloat()
                wingTip.rotateAngleZ = -(sin((f11 + 2.0f)) + 0.5).toFloat() * 0.75f
                wing.render(0.0625f)
                GlStateManager.scale(-1.0f, 1.0f, 1.0f)
                if (j == 0) {
                    GlStateManager.rotate(20f, 0f, 1f, 0f)
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

    private lateinit var capeData: Map<String, List<String>>
    private val capeFolder = File(mc.mcDataDir, "config/odin/capes")
    private val capeUpdateCache = mutableMapOf<String, Boolean>()
    data class Capes(
        @SerializedName("capes")
        val capes: Map<String, List<String>>
    )

    fun preloadCapes() {
        if (!capeFolder.exists()) capeFolder.toPath().createDirectories()

        capeData = fetchCapeData("https://odtheking.github.io/Odin/capes/capes.json")
        capeData.forEach { (capeFileName, _) ->
            val capeFile = File(capeFolder, capeFileName)
            val capeUrl = "https://odtheking.github.io/Odin/capes/$capeFileName"

            synchronized(capeUpdateCache) {
                if (capeUpdateCache[capeFileName] != true) {
                    if (!capeFile.exists() || !isFileUpToDate(capeUrl, capeFile)) {
                        downloadFile(capeUrl, capeFile.path)
                    }
                    capeUpdateCache[capeFileName] = true
                }
            }
        }
    }

    private fun fetchCapeData(manifestUrl: String): Map<String, List<String>> {
        return try {
            val json = URL(manifestUrl).readText()
            val manifest = Gson().fromJson(json, Capes::class.java)
            manifest.capes
        } catch (e: IOException) {
            e.printStackTrace()
            emptyMap()
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    @JvmStatic
    fun hookGetLocationCape(gameProfile: GameProfile): ResourceLocation? {
        val name = gameProfile.name
        val nameEncoded = Base64.encode(name.toByteArray())

        val capeFileName = findCapeFileName(nameEncoded) ?: return null
        val capeFile = File(capeFolder, capeFileName)

        return getCapeLocation(devs[name], capeFile)
    }

    private fun findCapeFileName(encodedName: String): String? {
        return capeData.entries.find { (_, usernames) -> encodedName in usernames }?.key
    }

    private fun isFileUpToDate(url: String, file: File): Boolean {
        return try {
            val connection = URL(url).openConnection()
            connection.connect()
            val remoteLastModified = connection.getHeaderFieldDate("Last-Modified", 0L)
            val localLastModified = file.lastModified()
            localLastModified >= remoteLastModified
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun getCapeLocation(dev: DevPlayer?, file: File): ResourceLocation? {
        if (dev?.capeLocation == null && file.exists()) {
            try {
                val image: BufferedImage = ImageIO.read(file)
                val capeLocation = mc.textureManager.getDynamicTextureLocation("odincapes", DynamicTexture(image))
                dev?.capeLocation = capeLocation
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return dev?.capeLocation
    }
}