package me.odinmain.features.impl.render

import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import me.odinmain.OdinMain
import me.odinmain.OdinMain.scope
import me.odinmain.config.Config
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.fetchURLData
import me.odinmain.utils.render.Color
import me.odinmain.utils.sendDataToServer
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.createClickStyle
import me.odinmain.utils.skyblock.getChatBreak
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.ClickGUI
import me.odinmain.utils.ui.hud.EditHUDGui
import net.minecraft.event.ClickEvent
import org.lwjgl.input.Keyboard

@AlwaysActive
object ClickGUIModule: Module(
    name = "Click Gui",
    Keyboard.KEY_RSHIFT,
    description = "Allows you to customize the GUI."
) {
    val blur by BooleanSetting("Blur", false, description = "Toggles the background blur for the gui.")
    val enableNotification by BooleanSetting("Enable notifications", true, description = "Shows you a notification in chat when you toggle an option with a keybind.")
    val color by ColorSetting("Gui Color", Color(50, 150, 220), allowAlpha = false, description = "Color theme in the gui.")
    val switchType by BooleanSetting("Switch Type", true, description = "Switches the type of the settings in the gui.")
    val hudChat by BooleanSetting("Shows HUDs in GUIs", true, description = "Shows HUDs in GUIs.")
    val forceHypixel by BooleanSetting("Force Hypixel", false, description = "Forces the hypixel check to be on (not recommended).")

    val devMessages by BooleanSetting("Dev Message", false, description = "Enables dev messages in chat.")
    val devSize by BooleanSetting("Dev Size", true, description = "Toggles client side dev size.").withDependency { DevPlayers.isDev }
    private val devWings by BooleanSetting("Dev Wings", false, description = "Toggles client side dev wings.").withDependency { DevPlayers.isDev }
    private val devWingsColor by ColorSetting("Dev Wings Color", Colors.WHITE, description = "Color of the dev wings.").withDependency { DevPlayers.isDev }
    private val devSizeX by NumberSetting("Dev Size X", 1f, -1f, 3f, 0.1, description = "X scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeY by NumberSetting("Dev Size Y", 1f, -1f, 3f, 0.1, description = "Y scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeZ by NumberSetting("Dev Size Z", 1f, -1f, 3f, 0.1, description = "Z scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private var showHidden by DropdownSetting("Show Hidden", false).withDependency { DevPlayers.isDev }
    private val passcode by StringSetting("Passcode", "odin", description = "Passcode for dev features.").withDependency { DevPlayers.isDev && showHidden }

    private val reset by ActionSetting("Send Dev Data", description = "Sends dev data to the server.") {
        showHidden = false
        scope.launch {
            modMessage(sendDataToServer(body = "${mc.thePlayer.name}, [${devWingsColor.r},${devWingsColor.g},${devWingsColor.b}], [$devSizeX,$devSizeY,$devSizeZ], $devWings, $passcode", "https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/"))
            DevPlayers.updateDevs()
        }
    }.withDependency { DevPlayers.isDev }

    private val action by ActionSetting("Open Example Hud", description = "Opens an example hud to allow configuration of huds.") {
        OdinMain.display = EditHUDGui
    }

    var lastSeenVersion by StringSetting("Last seen version", "1.0.0", hidden = true, description = "")
    private var joined by BooleanSetting("First join", false, hidden = true, "")
    private var hasSentUpdateMessage = false
    var latestVersionNumber: String? = null

    val panelX = mutableMapOf<Category, NumberSetting<Float>>()
    val panelY = mutableMapOf<Category, NumberSetting<Float>>()
    val panelExtended = mutableMapOf<Category, BooleanSetting>()

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

    fun resetPositions() {
        Category.entries.forEach {
            val incr = 10f + 260f * it.ordinal
            panelX.getOrPut(it) { +NumberSetting(it.name + ",x", default = incr, hidden = true, description = "") }.value = incr
            panelY.getOrPut(it) { +NumberSetting(it.name + ",y", default = 10f, hidden = true, description = "") }.value = 10f
            panelExtended.getOrPut(it) { +BooleanSetting(it.name + ",extended", default = true, hidden = true, description = "") }.enabled = true
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