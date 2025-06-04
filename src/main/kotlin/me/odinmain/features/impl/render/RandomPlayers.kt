package me.odinmain.features.impl.render

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.mojang.authlib.GameProfile
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.features.impl.render.ClickGUIModule.devSize
import me.odinmain.utils.downloadFile
import me.odinmain.utils.getDataFromServer
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.ui.Colors
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
import java.net.URL
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.createDirectories
import kotlin.math.cos
import kotlin.math.sin

object RandomPlayers {
    private var randoms: HashMap<String, RandomPlayer> = HashMap()
    val isRandom get() = randoms.containsKey(mc.session?.username)
    val isDev get() = randoms[mc.session?.username]?.isDev ?: false

    data class RandomPlayer(val scale: Triple<Float, Float, Float>, val wings: Boolean = false, val wingsColor: Color = Colors.WHITE, var capeLocation: ResourceLocation? = null, val customName: String, val isDev: Boolean)

    private val pattern = Regex("Decimal\\('(-?\\d+(?:\\.\\d+)?)'\\)")

    fun updateCustomProperties() {
        scope.launch {
            val data = getDataFromServer("https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/").replace(pattern) { match -> match.groupValues[1] }.ifEmpty { return@launch }
            JsonParser().parse(data)?.asJsonArray?.forEach {
                val jsonElement = it.asJsonObject
                val randomsName = jsonElement.get("DevName")?.asString ?: return@forEach
                val size = jsonElement.get("Size")?.asJsonArray?.let { sizeArray -> Triple(sizeArray[0].asFloat, sizeArray[1].asFloat, sizeArray[2].asFloat) } ?: return@forEach
                val wings = jsonElement.get("Wings")?.asBoolean == true
                val wingsColor = jsonElement.get("WingsColor")?.asJsonArray?.let { colorArray -> Color(colorArray[0].asInt, colorArray[1].asInt, colorArray[2].asInt) } ?: Colors.WHITE
                val customName = jsonElement.get("CustomName")?.asString?.replace("COLOR", "§") ?: ""
                val isDev = jsonElement.get("IsDev")?.asBoolean ?: false
                randoms[randomsName] = RandomPlayer(size, wings, Color(wingsColor.red, wingsColor.green, wingsColor.blue), null, customName, isDev)
            }
            modMessage("Dev players updated: ${randoms.size} players found.")
        }
    }

    init {
        updateCustomProperties()
    }

    @JvmStatic
    fun preRenderCallbackScaleHook(entityLivingBaseIn: AbstractClientPlayer) {
        if (!randoms.containsKey(entityLivingBaseIn.name)) return
        if (!devSize && entityLivingBaseIn.name == mc.thePlayer.name) return
        val random = randoms[entityLivingBaseIn.name] ?: return
        if (random.scale.second < 0) GlStateManager.translate(0f, random.scale.second * 2, 0f)
        GlStateManager.scale(random.scale.first, random.scale.second, random.scale.third)
    }

    @SubscribeEvent
    fun onRenderPlayer(event: RenderPlayerEvent.Post) {
        if (!randoms.containsKey(event.entity.name)) return
        if (!devSize && event.entity.name == mc.thePlayer.name) return
        val random = randoms[event.entity.name] ?: return
        if (!random.wings) return
        DragonWings.renderWings(event.entityPlayer, event.partialRenderTick, random)
    }

    fun replaceText(text: String?): String? {
        var replacedText = text
        for (random in randoms) {
            if (random.value.customName.isBlank()) continue
            replacedText = randoms[random.key]?.let { replacedText?.replace(random.key, it.customName) }
        }

        return replacedText
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

        fun renderWings(player: EntityPlayer, partialTicks: Float, random: RandomPlayer) {
            val rotation = this.interpolate(player.prevRenderYawOffset, player.renderYawOffset, partialTicks)

            GlStateManager.pushMatrix()
            val x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
            val y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
            val z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks
            if (random.scale.second < 0) GlStateManager.translate(0f, random.scale.second * -2, 0f)

            GlStateManager.translate(-mc.renderManager.viewerPosX + x, -mc.renderManager.viewerPosY + y, -mc.renderManager.viewerPosZ + z)
            GlStateManager.scale(-0.2, -0.2, 0.2)
            GlStateManager.scale(random.scale.first, random.scale.second, random.scale.third)
            GlStateManager.rotate(180 + rotation, 0f, 1f, 0f)
            GlStateManager.translate(0.0, -(1.25 / 0.2f), 0.0)
            GlStateManager.translate(0.0, 0.0, 0.25)

            if (player.isSneaking) {
                GlStateManager.rotate(45f, 1f, 0f, 0f)
                GlStateManager.translate(0.0, 1.0, -0.5)
            }

            GlStateManager.color(random.wingsColor.red.toFloat()/255, random.wingsColor.green.toFloat()/255, random.wingsColor.blue.toFloat()/255, 1f)
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

        capeData = fetchCapeData()
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

    private fun fetchCapeData(manifestUrl: String = "https://odtheking.github.io/Odin/capes/capes.json"): Map<String, List<String>> {
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

        return getCapeLocation(randoms[name], capeFile)
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

    private fun getCapeLocation(dev: RandomPlayer?, file: File): ResourceLocation? {
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