package me.odinmain.features.impl.render

import kotlinx.coroutines.launch
import me.odinmain.OdinMain
import me.odinmain.OdinMain.scope
import me.odinmain.config.Config
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.hud.EditHUDGui
import me.odinmain.utils.render.Color
import me.odinmain.utils.sendDataToServer
import me.odinmain.utils.skyblock.*
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import org.lwjgl.input.Keyboard

@AlwaysActive
object ClickGUIModule: Module(
    name = "Click Gui",
    Keyboard.KEY_RSHIFT,
    category = Category.RENDER,
    description = "Allows you to customize the GUI."
) {
    val blur by BooleanSetting("Blur", false, description = "Toggles the background blur for the gui.")
    val enableNotification by BooleanSetting("Enable notifications", true, description = "Shows you a notification in chat when you toggle an option with a keybind.")
    val color by ColorSetting("Gui Color", Color(50, 150, 220), allowAlpha = false, description = "Color theme in the gui.")
    val switchType by DualSetting("Switch Type", "Checkbox", "Switch", default = true, description = "Switches the type of the settings in the gui.")
    val hudChat by BooleanSetting("Shows HUDs in GUIs", true, description = "Shows HUDs in GUIs.")
    val forceHypixel by BooleanSetting("Force Hypixel", false, description = "Forces the hypixel check to be on (not recommended).")
    val updateMessage by SelectorSetting("Update Message", "Beta", arrayListOf("Beta", "Full", "None"), description = "Shows the update message in chat.")

    val devMessages by BooleanSetting("Dev Messages", false, description = "Enables dev messages in chat.").withDependency { DevPlayers.isDev }
    val devSize by BooleanSetting("Dev Size", true, description = "Toggles client side dev size.").withDependency { DevPlayers.isDev }
    private val devWings by BooleanSetting("Dev Wings", false, description = "Toggles client side dev wings.").withDependency { DevPlayers.isDev }
    private val devWingsColor by ColorSetting("Dev Wings Color", Color(255, 255, 255), description = "Color of the dev wings.").withDependency { DevPlayers.isDev }
    private val devSizeX by NumberSetting("Dev Size X", 1f, -1f, 3f, 0.1, description = "X scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeY by NumberSetting("Dev Size Y", 1f, -1f, 3f, 0.1, description = "Y scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeZ by NumberSetting("Dev Size Z", 1f, -1f, 3f, 0.1, description = "Z scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private var showHidden by DropdownSetting("Show Hidden", false).withDependency { DevPlayers.isDev }
    private val passcode by StringSetting("Passcode", "odin", description = "Passcode for dev features.").withDependency { DevPlayers.isDev && showHidden }

    val reset by ActionSetting("Send Dev Data", description = "Sends dev data to the server.") {
        showHidden = false
        scope.launch {
            modMessage(sendDataToServer(body = "${mc.thePlayer.name}, [${devWingsColor.r},${devWingsColor.g},${devWingsColor.b}], [$devSizeX,$devSizeY,$devSizeZ], $devWings, $passcode", "https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/"))
            DevPlayers.updateDevs()
        }
    }.withDependency { DevPlayers.isDev }

    val action by ActionSetting("Open Example Hud", description = "Opens an example hud to allow configuration of huds.") {
        OdinMain.display = EditHUDGui
    }

    private var joined by BooleanSetting("First join", false, hidden = true, "")
    var lastSeenVersion: String by StringSetting("Last seen version", "1.0.0", hidden = true, description = "")
    var firstTimeOnVersion = false

    val panelX = mutableMapOf<Category, NumberSetting<Float>>()
    val panelY = mutableMapOf<Category, NumberSetting<Float>>()
    val panelExtended = mutableMapOf<Category, BooleanSetting>()

    init {
        execute(250) {
            if (joined) destroyExecutor()
            if (!LocationUtils.isInSkyblock) return@execute
            joined = true
            Config.save()

            modMessage("""
            ${getChatBreak()}
            §d§kOdinOnTopWeLoveOdinLiterallyTheBestModAAAAAAAAAAAAAAAA
            
            §7Thanks for installing §3Odin ${OdinMain.VERSION}§7!

            §7Use §d§l/od §r§7to access GUI settings.
            §7Use §d§l/od help §r§7for all of of the commands.
             
            §7Join the discord for support and suggestions.
            """.trimIndent(), "")
            mc.thePlayer.addChatMessage(
                ChatComponentText(" §9https://discord.gg/2nCbC9hkxT")
                    .setChatStyle(createClickStyle(ClickEvent.Action.OPEN_URL, "https://discord.gg/2nCbC9hkxT"))
            )

            modMessage("""
            
            §d§kOdinOnTopWeLoveOdinLiterallyTheBestModAAAAAAAAAAAAAAAA
            ${getChatBreak()}
            
            """.trimIndent(), "")
        }
        resetPositions()
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