package com.odtheking.odin.features.impl.render

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mojang.authlib.GameProfile
import com.mojang.blaze3d.vertex.PoseStack
import com.odtheking.odin.OdinMod
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import com.odtheking.odin.utils.network.WebUtils.postData
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
import net.minecraft.client.renderer.entity.state.AvatarRenderState

object PlayerSize : Module(
    name = "Player Size",
    description = "Changes the size of the player."
) {
    private val devSize by BooleanSetting("Dev Size", true, desc = "Toggles client side dev size for your own player.").withDependency { isRandom }
    private val devSizeX by NumberSetting("Size X", 1f, -1, 3f, 0.1, desc = "X scale of the dev size.")
    private val devSizeY by NumberSetting("Size Y", 1f, -1, 3f, 0.1, desc = "Y scale of the dev size.")
    private val devSizeZ by NumberSetting("Size Z", 1f, -1, 3f, 0.1, desc = "Z scale of the dev size.")
    private val devWings by BooleanSetting("Wings", false, desc = "Toggles dragon wings.").withDependency { isRandom }
    private val devWingsColor by ColorSetting("Wings Color", Colors.WHITE, desc = "Color of the dev wings.").withDependency { devWings && isRandom }
    private var showHidden by DropdownSetting("Show Hidden").withDependency { isRandom }
    private val passcode by StringSetting("Passcode", "odin", desc = "Passcode for dev features.").withDependency { showHidden && isRandom }

    const val DEV_SERVER = "https://api.odtheking.com/devs/"

    private val sendDevData by ActionSetting("Send Dev Data", desc = "Sends dev data to the server.") {
        showHidden = false
        fun valid(v: Float) = (v in 0.8f..1.6f) || (v in -1.0f..-0.8f)
        if (!valid(devSizeX) || !valid(devSizeY) || !valid(devSizeZ)) {
            modMessage("Global values must be between 0.8..1.6 or -1..-0.8")
            return@ActionSetting
        }
        OdinMod.scope.launch {
            val body = buildDevBody(
                mc.user.name,
                devWingsColor, devSizeX, devSizeY,
                devSizeZ, devWings, " ", passcode
            )

            modMessage(postData(DEV_SERVER, body).getOrNull())
            updateCustomProperties()
        }
    }.withDependency { isRandom }


    var randoms: HashMap<String, RandomPlayer> = HashMap()
    val isRandom get() = randoms.containsKey(mc.user.name)

    data class RandomPlayer(
        @SerializedName("CustomName")   val customName: String?,
        @SerializedName("DevName")      val name: String,
        @SerializedName("IsDev")        val isDev: Boolean?,
        @SerializedName("WingsColor")   val wingsColor: List<Int>,
        @SerializedName("Size")         val scale: List<Float>,
        @SerializedName("Wings")        val wings: Boolean
    )

    @JvmStatic
    fun preRenderCallbackScaleHook(entityRenderer: AvatarRenderState, matrix: PoseStack) {
        val gameProfile = entityRenderer.getData(GAME_PROFILE_KEY) ?: return
        if (enabled && gameProfile.name == mc.player?.gameProfile?.name && !randoms.containsKey(gameProfile.name)) {
            if (devSizeY < 0) matrix.translate(0f, devSizeY * 2, 0f)
            matrix.scale(devSizeX, devSizeY, devSizeZ)
        }
        if (!randoms.containsKey(gameProfile.name)) return
        if (!devSize && gameProfile.name == mc.player?.gameProfile?.name) return
        val random = randoms[gameProfile.name] ?: return
        if (random.scale[1] < 0) matrix.translate(0f, random.scale[1] * 2, 1f)
        matrix.scale(random.scale[0], random.scale[1], random.scale[2])
    }

    suspend fun updateCustomProperties(): String {
        val response = fetchJson<Array<RandomPlayer>>("https://api.odtheking.com/devs/").getOrNull() ?: return "Failed to fetch custom properties!"

        randoms.clear()
        randoms.putAll(response.associateBy { it.name })
        CustomNameReplacer.rebuild(randoms.values)
        return response.joinToString("\n")
    }

    @JvmStatic
    fun clearCustomProperties() {
        randoms.clear()
        CustomNameReplacer.clear()
    }

    init {
        OdinMod.scope.launch {
            updateCustomProperties()
        }
    }

    fun buildDevBody(devName: String, wingsColor: Color, sizeX: Float, sizeY: Float, sizeZ: Float, wings: Boolean, customName: String, password: String): String {
        return Gson().toJson(mapOf(
            "devName" to devName,
            "wingsColor" to listOf(wingsColor.red, wingsColor.green, wingsColor.blue),
            "size" to listOf(sizeX, sizeY, sizeZ),
            "wings" to wings,
            "customName" to customName,
            "password" to password
        ))
    }

    @JvmStatic
    val GAME_PROFILE_KEY: RenderStateDataKey<GameProfile> = RenderStateDataKey.create { "odin:game_profile" }
}