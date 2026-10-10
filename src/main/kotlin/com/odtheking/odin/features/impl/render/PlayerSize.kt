package com.odtheking.odin.features.impl.render

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mojang.authlib.GameProfile
import com.mojang.blaze3d.vertex.PoseStack
import com.odtheking.odin.OdinMod
import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import com.odtheking.odin.utils.network.WebUtils.postData
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import java.util.*

object PlayerSize : Module(
    name = "Player Size",
    description = "Changes the size of the player."
) {
    private val useClientSize by BooleanSetting("Use Client Size", false, desc = "Uses your client side size values below on yourself instead of your random size.").withDependency { isRandom }
    private val sizeX by NumberSetting("Size X", 1f, -1.0..3.0, 0.1, desc = "X scale of your client side size.")
    private val sizeY by NumberSetting("Size Y", 1f, -1.0..3.0, 0.1, desc = "Y scale of your client side size.")
    private val sizeZ by NumberSetting("Size Z", 1f, -1.0..3.0, 0.1, desc = "Z scale of your client side size.")
    private var showHidden by DropdownSetting("Show Hidden", desc = "Shows the passcode field for dev features.").withDependency { isRandom }
    private val passcode by StringSetting("Passcode", "odin", desc = "Passcode for dev features.", placeholder = "Enter passcode").withDependency { showHidden && isRandom }

    const val DEV_SERVER = "https://devs.odtheking.com"

    private val sendDevData by ActionSetting("Send Dev Data", desc = "Sends dev data to the server.") {
        showHidden = false
        fun valid(v: Float) = (v in 0.8f..1.6f) || (v in -1.0f..-0.8f)
        if (!valid(sizeX) || !valid(sizeY) || !valid(sizeZ)) {
            modMessage("Global values must be between 0.8..1.6 or -1..-0.8")
            return@ActionSetting
        }
        OdinMod.scope.launch {
            modMessage(postData(DEV_SERVER, Gson().toJson(
                mapOf(
                    "DevName" to mc.user.name,
                    "Size" to listOf(sizeX, sizeY, sizeZ),
                    "CustomName" to " ",
                    "Password" to passcode
                )
            )).getOrNull())
            updateCustomProperties()
        }
    }.withDependency { isRandom }


    var randoms: HashMap<UUID, RandomPlayer> = HashMap()
    val isRandom get() = randoms.containsKey(mc.user.profileId)

    data class RandomPlayer(
        @SerializedName("CustomName")   val customName: String?,
        @SerializedName("DevName")      val name: String,
        @SerializedName("Uuid")         val uuid: UUID,
        @SerializedName("Size")         val scale: List<Float>
    )

    @JvmStatic
    fun preRenderCallbackScaleHook(entityRenderer: AvatarRenderState, matrix: PoseStack) {
        if (!enabled) return
        val gameProfile = entityRenderer.getData(GAME_PROFILE_KEY) ?: return

        if (gameProfile.name == mc.player?.gameProfile?.name && (!isRandom || useClientSize)) {
            if (sizeY < 0) matrix.translate(0f, sizeY * 2, 0f)
            matrix.scale(sizeX, sizeY, sizeZ)
            return
        }

        val random = randoms[gameProfile.id] ?: return

        if (random.scale[1] < 0) matrix.translate(0f, random.scale[1] * 2, 0f)
        matrix.scale(random.scale[0], random.scale[1], random.scale[2])
    }

    suspend fun updateCustomProperties(): String {
        val response = fetchJson<Array<RandomPlayer>>(DEV_SERVER).getOrNull() ?: return "Failed to fetch custom properties!"

        randoms.clear()
        randoms.putAll(response.associateBy { it.uuid })
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

    @JvmStatic
    val GAME_PROFILE_KEY: RenderStateDataKey<GameProfile> = RenderStateDataKey.create { "odin:game_profile" }
}