package com.odtheking.odin.features.impl.render

import com.google.gson.annotations.SerializedName
import com.odtheking.odin.OdinMod
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.clickgui.settings.AlwaysActive
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.getChatBreak
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import com.odtheking.odin.utils.network.WebUtils.postData
import com.odtheking.odin.utils.skyblock.LocationUtils
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
    val webSocketUrl by StringSetting("Socket URL", "wss://ws.odtheking.com/", 128, "The Websocket server to connect to.").hide()

    private val action by ActionSetting("Open HUD Editor", desc = "Opens the HUD editor when clicked.") { mc.setScreen(HudManager) }
    val devMessage by BooleanSetting("Developer Message", false, desc = "Sends development related messages to the chat.")

    private var firstJoin by BooleanSetting("First join", true, "").hide()

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
    private const val MODRINTH_LINK = "https://modrinth.com/mod/odin/versions"
    private val profileRegex = Regex("Profile ID:\\s*(.{36})")
    private var latestVersionNumber: String? = null
    private var hasSentUpdateMessage = false

    init {
        OdinMod.scope.launch {
            latestVersionNumber = checkNewerVersion(OdinMod.version.toString())
            val name = OdinMod.mc.user.name.takeIf { !it.matches(Regex("Player\\d{2,3}")) } ?: return@launch
            postData("https://api.odtheking.com/tele/", """{"username": "$name", "version": "Fabric ${OdinMod.version}"}""")
        }

        on<ChatPacketEvent> {
            if (!profileRegex.matches(value)) return@on

            if (firstJoin) {
                firstJoin = false
                ModuleManager.saveConfigurations()
                modMessage(
                    Component.literal(getChatBreak()).append(Component.literal("""
                        §7Thanks for installing §3Odin ${OdinMod.version}§7!
                
                        §7Use §d§l/od §r§7to access GUI settings.
                             
                        §7Join the discord for support and suggestions.
                    """.trimIndent()))
                        .append(Component.literal("\n"))
                        .append(
                            Component.literal("§9https://discord.gg/2nCbC9hkxT").withStyle {
                                it.withClickEvent(ClickEvent.OpenUrl(URI("https://discord.gg/2nCbC9hkxT"))).withHoverEvent(
                                    HoverEvent.ShowText(Component.literal("https://discord.gg/2nCbC9hkxT"))
                                )
                            }
                        )
                        .append(Component.literal("\n"))
                        .append(Component.literal(getChatBreak())),
                    "")
            }

            if (hasSentUpdateMessage || latestVersionNumber == null) return@on
            hasSentUpdateMessage = true

            modMessage(
                Component.literal(getChatBreak())
                    .append(Component.literal("§3Odin update available: §f$latestVersionNumber\n\n"))
                    .append(
                        Component.literal("§bGitHub link").withStyle {
                            it.withClickEvent(ClickEvent.OpenUrl(URI(RELEASE_LINK))).withHoverEvent(
                                HoverEvent.ShowText(Component.literal(RELEASE_LINK))
                            )
                        }
                    )
                    .append(Component.literal("\n"))
                    .append(
                        Component.literal("§bModrinth Link").withStyle {
                            it.withClickEvent(ClickEvent.OpenUrl(URI(MODRINTH_LINK))).withHoverEvent(
                                HoverEvent.ShowText(Component.literal(MODRINTH_LINK))
                            )
                        }
                    )
                    .append(Component.literal("\n"))
                    .append(
                        Component.literal("§9Discord link").withStyle {
                            it.withClickEvent(ClickEvent.OpenUrl(URI("https://discord.gg/2nCbC9hkxT"))).withHoverEvent(
                                HoverEvent.ShowText(Component.literal("https://discord.gg/2nCbC9hkxT"))
                            )
                        }
                    )
                    .append(Component.literal("\n\n${getChatBreak()}§r")),
                ""
            )

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