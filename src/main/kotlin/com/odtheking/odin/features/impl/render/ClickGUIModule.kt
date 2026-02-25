package com.odtheking.odin.features.impl.render

import com.google.gson.annotations.SerializedName
import com.odtheking.odin.OdinMod
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.clickgui.settings.AlwaysActive
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.getChatBreak
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import kotlinx.coroutines.launch
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import org.lwjgl.glfw.GLFW
import java.net.URI
import kotlin.math.max
import kotlin.math.round

@AlwaysActive
object ClickGUIModule : Module(
    name = "Click GUI",
    description = "Allows you to customize the UI.",
    key = GLFW.GLFW_KEY_RIGHT_SHIFT
) {
    val enableNotification by BooleanSetting("Chat notifications", true, desc = "Sends a message when you toggle a module with a keybind")
    val clickGUIColor by ColorSetting("Color", Color(50, 150, 220), desc = "The color of the Click GUI.")

    val roundedPanelBottom by BooleanSetting("Rounded Panel Bottoms", true, desc = "Whether to extend panels to make them rounded at the bottom.")

    val hypixelApiUrl by StringSetting("API URL", "https://api.odtheking.com/hypixel/", 128, "The Hypixel API server to connect to.").hide()
    val webSocketUrl by StringSetting("WebSocket URL", "wss://api.odtheking.com/ws/", 128, "The Websocket server to connect to.").hide()

    private val action by ActionSetting("Open HUD Editor", desc = "Opens the HUD editor when clicked.") { mc.setScreen(HudManager) }
    val devMessage by BooleanSetting("Developer Message", false, desc = "Sends development related messages to the chat.")

    override fun onKeybind() {
        toggle()
    }

    override fun onEnable() {
        mc.setScreen(ClickGUI)
        super.onEnable()
        toggle()
    }

    val panelSetting by MapSetting("Panel Settings", mutableMapOf<String, PanelData>())
    data class PanelData(var x: Float = 10f, var y: Float = 10f, var extended: Boolean = true)

    fun resetPositions() {
        Category.categories.entries.forEachIndexed { index, (categoryName, _) ->
            val setting = panelSetting.getOrPut(categoryName) { PanelData() }
            setting.x = 10f + 260f * index
            setting.y = 10f
            setting.extended = true
        }
    }

    private const val RELEASE_LINK = "https://github.com/odtheking/OdinFabric/releases/latest"
    private var latestVersionNumber: String? = null
    private var hasSentUpdateMessage = false

    init {
        OdinMod.scope.launch {
            latestVersionNumber = checkNewerVersion(OdinMod.version.toString())
        }

        on<WorldEvent.Load> {
           if (hasSentUpdateMessage || latestVersionNumber == null) return@on
            hasSentUpdateMessage = true

            modMessage("""
            ${getChatBreak()}
                
            §3Odin update available: §f$latestVersionNumber
            """.trimIndent(), "")

            modMessage(Component.literal("§b$RELEASE_LINK").withStyle {
                it.withClickEvent(ClickEvent.OpenUrl(URI(RELEASE_LINK)))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal(RELEASE_LINK)))
            }, "")

            modMessage("""
            
            ${getChatBreak()}§r
            
            """.trimIndent(), "")
            alert("Odin Update Available")
        }
    }

    fun getStandardGuiScale(): Float {
        val verticalScale = (mc.window.screenHeight.toFloat() / 1080f) / NVGRenderer.devicePixelRatio()
        val horizontalScale = (mc.window.screenWidth.toFloat() / 1920f) / NVGRenderer.devicePixelRatio()
        return round(max(verticalScale, horizontalScale).coerceIn(1f, 3f) * 10f) / 10f
    }

    private suspend fun checkNewerVersion(currentVersion: String): String? {
        val newest = fetchJson<Release>("https://api.github.com/repos/odtheking/OdinFabric/releases/latest").getOrElse { return null }

        return if (isSecondNewer(currentVersion, newest.tagName)) newest.tagName else null
    }

    private fun isSecondNewer(currentVersion: String, previousVersion: String?): Boolean {
        if (currentVersion.isEmpty() || previousVersion.isNullOrEmpty()) return false

        val (major, minor, patch) = currentVersion.split(".").mapNotNull { it.toIntOrNull() }
        val (major2, minor2, patch2) = previousVersion.split(".").mapNotNull { it.toIntOrNull() }

        return when {
            major > major2 -> false
            major < major2 -> true
            minor > minor2 -> false
            minor < minor2 -> true
            patch > patch2 -> false
            patch < patch2 -> true
            else -> false // equal, or something went wrong, either way it's best to assume it's false.
        }
    }

    private data class Release(
        @SerializedName("tag_name")
        val tagName: String
    )
}