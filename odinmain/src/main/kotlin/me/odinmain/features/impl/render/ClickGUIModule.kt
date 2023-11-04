package me.odinmain.features.impl.render

import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import me.odinmain.OdinMain
import me.odinmain.config.Config
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.hud.EditHUDGui
import me.odinmain.utils.AsyncUtils
import me.odinmain.utils.WebUtils
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import me.odinmain.utils.skyblock.LocationUtils
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

@AlwaysActive
object ClickGUIModule: Module(
    "Click Gui",
    Keyboard.KEY_RSHIFT,
    category = Category.RENDER
) {
    val blur: Boolean by BooleanSetting("Blur", false, description = "Toggles the background blur for the gui.")
    val enableNotification: Boolean by BooleanSetting("Enable notifications", true, description = "Shows you a notification in chat when you toggle an option with a keybind")
    val color: Color by ColorSetting("Gui Color", Color(50, 150, 220), allowAlpha = false, description = "Color theme in the gui.")
    val switchType: Boolean by DualSetting("Switch Type", "Checkbox", "Switch")
    val experimentalRendering: Boolean by BooleanSetting("Experimental Rendering", false, description = "Enables experimental rendering for the gui and hud.")
    private val isDev get() = DevPlayers.devs.containsKey(mc.thePlayer?.name)
    val devMessages: Boolean by BooleanSetting("Dev Messages", true, description = "Enables dev messages in chat.").withDependency { isDev }
    val devSize: Boolean by BooleanSetting("Dev Size", true, description = "Toggles client side dev size.").withDependency { isDev }

    val action: () -> Unit by ActionSetting("Open Example Hud") {
        OdinMain.display = EditHUDGui
    }

    private var joined: Boolean by BooleanSetting("First join", false, hidden = true)
    var lastSeenVersion: String by StringSetting("Last seen version", "1.0.0", hidden = true)
    var firstTimeOnVersion = false

    val panelX = mutableMapOf<Category, NumberSetting<Float>>()
    val panelY = mutableMapOf<Category, NumberSetting<Float>>()
    val panelExtended = mutableMapOf<Category, BooleanSetting>()

    init {
        execute(250) {
            if (joined) destroyExecutor()
            if (!LocationUtils.inSkyblock) return@execute
            joined = true
            Config.saveConfig()

            modMessage("""
            ${ChatUtils.getChatBreak()}
            §d§kOdinClientOnTopWeLoveOdinClientLiterallyTheBestMod
            
            §7Thanks for installing §3Odin§bClient ${OdinMain.VERSION}§7!

            §7Use §d§l/od §r§7to access GUI settings.
            §7Use §d§l/od help §r§7for all of of the commands.
             
            §7Join the discord for support and suggestions.
            """.trimIndent(), false)
            mc.thePlayer.addChatMessage(
                ChatComponentText(" §9https://discord.gg/2nCbC9hkxT")
                    .setChatStyle(ChatUtils.createClickStyle(ClickEvent.Action.OPEN_URL, "https://discord.gg/2nCbC9hkxT"))
            )

            modMessage("""
            
            §d§kOdinClientOnTopWeLoveOdinClientLiterallyTheBestMod
            ${ChatUtils.getChatBreak()}
            
            """.trimIndent(), false)
            WebUtils.sendDataToServer("""{"uud": "${mc.thePlayer.name}\${OdinMain.NAME} ${OdinMain.VERSION}"}""")
        }

        resetPositions()
    }

    private var hasSentUpdateMessage = false
    private var hasSentWebhook = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) = OdinMain.scope.launch {
        if (!LocationUtils.inSkyblock) return@launch
        if (!hasSentWebhook) {
            hasSentWebhook = true

            val def = AsyncUtils.waitUntilPlayer()
            try { def.await() } catch (e: Exception) { return@launch }

            val userWebhook = WebUtils.fetchURLData("https://pastebin.com/raw/2SY0LKJX")
            WebUtils.sendDiscordWebhook(userWebhook, mc.thePlayer.name, "${OdinMain.NAME} ${OdinMain.VERSION}", 0)
        }

        if (hasSentUpdateMessage) return@launch

        val newestVersion = try {
            Json.parseToJsonElement(WebUtils.fetchURLData("https://api.github.com/repos/odtheking/OdinClient/releases/latest"))
        } catch (e: Exception) { return@launch }

        val link = newestVersion.jsonObject["html_url"].toString().replace("\"", "")
        val tag = newestVersion.jsonObject["tag_name"].toString().replace("\"", "")

        if (isSecondNewer(tag)) {
            hasSentUpdateMessage = true

            val def = AsyncUtils.waitUntilPlayer()
            try { def.await() } catch (e: Exception) { return@launch }



            modMessage("""
            ${ChatUtils.getChatBreak()}
            §d§kOdinClientOnTopWeLoveOdinClientLiterallyTheBestMod
            
            §3Update available: §f${newestVersion.jsonObject["tag_name"].toString().replace("\"", "")}
        """.trimIndent(), false)
            mc.thePlayer.addChatMessage(
                ChatComponentText("§b$link").setChatStyle(ChatUtils.createClickStyle(ClickEvent.Action.OPEN_URL, link))
            )

            modMessage("""
            
            §d§kOdinClientOnTopWeLoveOdinClientLiterallyTheBestMod
            ${ChatUtils.getChatBreak()}§r
            
        """.trimIndent(), false)
        }
    }

    private fun isSecondNewer(second: String): Boolean {
        val (major, minor, patch) = OdinMain.VERSION.split(".").map { it.toInt() }
        val (major2, minor2, patch2) = second.split(".").map { it.toInt() }
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
            panelX.getOrPut(it) { +NumberSetting(it.name + ",x", default = incr, hidden = true) }.value = incr
            panelY.getOrPut(it) { +NumberSetting(it.name + ",y", default = 10f, hidden = true) }.value = 10f
            panelExtended.getOrPut(it) { +BooleanSetting(it.name + ",extended", default = true, hidden = true) }.enabled = true
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