package me.odinmain.features.impl.render

import com.google.gson.JsonParser
import me.odinmain.OdinMain
import me.odinmain.clickgui.ClickGUI
import me.odinmain.clickgui.HudManager
import me.odinmain.clickgui.settings.AlwaysActive
import me.odinmain.clickgui.settings.impl.*
import me.odinmain.config.Config
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.fetchURLData
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.*
import net.minecraft.event.ClickEvent
import org.lwjgl.input.Keyboard

@AlwaysActive
object ClickGUIModule: Module(
    name = "Click Gui",
    Keyboard.KEY_RSHIFT,
    description = "Allows you to customize the GUI."
) {
    val blur by BooleanSetting("Blur", false, desc = "Toggles the background blur for the gui.")
    val enableNotification by BooleanSetting("Enable notifications", true, desc = "Shows you a notification in chat when you toggle an option with a keybind.")
    val clickGUIColor by ColorSetting("Gui Color", Color(50, 150, 220), allowAlpha = false, desc = "Color theme in the gui.")
    val switchType by BooleanSetting("Switch Type", true, desc = "Switches the type of the settings in the gui.")
    val hudChat by BooleanSetting("Shows HUDs in GUIs", true, desc = "Shows HUDs in GUIs.")

    val devMessages by BooleanSetting("Dev Message", false, desc = "Enables dev messages in chat.")

    private val action by ActionSetting("Open Example Hud", desc = "Opens an example hud to allow configuration of huds.") {
        OdinMain.display = HudManager
    }

    var lastSeenVersion by StringSetting("Last seen version", "1.0.0", desc = "").hide()
    private var joined by BooleanSetting("First join", false, "").hide()
    private var hasSentUpdateMessage = false
    var latestVersionNumber: String? = null

    data class PanelData(var x: Float = 10f, var y: Float = 10f, var extended: Boolean = true)
    val panelSetting by MapSetting("Panel Settings", mutableMapOf<Category, PanelData>())

    fun resetPositions() {
        Category.entries.forEach {
            panelSetting[it] = PanelData(10f + 260f * it.ordinal, 10f, true)
        }
    }

    init {
        execute(250) {
            if (!LocationUtils.isInSkyblock) return@execute

            if (!hasSentUpdateMessage && latestVersionNumber != null) {
                hasSentUpdateMessage = true

                val link = "https://github.com/odtheking/Odin/releases/latest"

                modMessage("""
                ${getChatBreak()}
                §d§kOdinClientOnBottomWeHateOdinClientLiterallyTheWorstMod
                    
                §3Update available: §f$latestVersionNumber
                """.trimIndent(), "")

                modMessage("§b$link", "", createClickStyle(ClickEvent.Action.OPEN_URL, link))

                modMessage("""
                
                §d§kOdinClientOnBottomWeHateOdinClientLiterallyTheWorstMod
                ${getChatBreak()}§r
                
                """.trimIndent(), "")
                PlayerUtils.alert("Odin Update Available")
            }

            if (joined) destroyExecutor()
            joined = true
            Config.save()

            modMessage("""
            ${getChatBreak()}
            §d§kOdinClientOnBottomWeHateOdinClientLiterallyTheWorstMod
            
            §7Thanks for installing §3Odin ${OdinMain.VERSION}§7!

            §7Use §d§l/od §r§7to access GUI settings.
            §7Use §d§l/od help §r§7for all of of the commands.
             
            §7Join the discord for support and suggestions.
            """.trimIndent(), "")

            modMessage("§9https://discord.gg/2nCbC9hkxT", "", createClickStyle(ClickEvent.Action.OPEN_URL, "https://discord.gg/2nCbC9hkxT"))

            modMessage("""
            §7Odin tracks your IGN and mod version.
            §d§kOdinClientOnBottomWeHateOdinClientLiterallyTheWorstMod
            ${getChatBreak()}
            
            """.trimIndent(), "")
        }
        resetPositions()
    }

    fun checkNewerVersion(currentVersion: String): String? {
        val newestVersion = try {
            JsonParser().parse(fetchURLData("https://api.github.com/repos/odtheking/Odin/releases/latest")).asJsonObject
        } catch (e: Exception) { return null }

        if (isSecondNewer(currentVersion, newestVersion.get("tag_name").asString)) return newestVersion.get("tag_name").asString.toString().replace("\"", "")
        return null
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



    override fun onKeybind() {
        this.toggle()
    }

    override fun onEnable() {
        OdinMain.display = ClickGUI
        super.onEnable()
        toggle()
    }
}