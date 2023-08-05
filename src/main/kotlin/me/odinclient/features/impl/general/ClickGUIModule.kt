package me.odinclient.features.impl.general

import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import me.odinclient.OdinClient
import me.odinclient.OdinClient.Companion.display
import me.odinclient.config.Config
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.AlwaysActive
import me.odinclient.features.settings.impl.*
import me.odinclient.ui.clickgui.ClickGUI
import me.odinclient.ui.hud.ExampleHudGui
import me.odinclient.utils.AsyncUtils
import me.odinclient.utils.WebUtils
import me.odinclient.utils.render.Color
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.LocationUtils
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

@AlwaysActive
object ClickGUIModule: Module(
    "ClickGUI",
    Keyboard.KEY_RSHIFT,
    category = Category.GENERAL,
) {
    val blur: Boolean by BooleanSetting("Blur", false, description = "Toggles the background blur for the gui.")
    val enableNotification: Boolean by BooleanSetting("Enable notifications", false, description = "Shows you a notification in chat when you toggle an option with a keybind")
    val color: Color by ColorSetting("GUI Color", Color(50, 150, 220), allowAlpha = false, description = "Color theme in the gui.")
    val switchType: Boolean by DualSetting("Switch Type", "Checkbox", "Switch")
    val action: () -> Unit by ActionSetting("Open Example Hud") {
        display = ExampleHudGui
    }

    private var hasJoined: Boolean by BooleanSetting("First join", false, hidden = true)

    val panelX = mutableMapOf<Category, NumberSetting<Float>>()
    val panelY = mutableMapOf<Category, NumberSetting<Float>>()
    val panelExtended = mutableMapOf<Category, BooleanSetting>()

    init {
        execute(250) {
            if (hasJoined) destroyExecutor()
            if (!LocationUtils.inSkyblock) return@execute
            hasJoined = true
            Config.saveConfig()

            modMessage("""
            ${ChatUtils.getChatBreak().dropLast(1)}
            §d§kOdinClientOnTopWeLoveOdinClientLiterallyTheBestMod
            
            §7Thanks for installing §3Odin§bClient ${OdinClient.VERSION}§7!

             §eUse §d§l/od §r§eto access GUI settings.
             §eUse §d§l/od help §r§efor all of of the commands.
             
             §eJoin the discord for support and suggestions.
        """.trimIndent(), "")
            OdinClient.mc.thePlayer.addChatMessage(
                ChatComponentText(" §9https://discord.gg/2nCbC9hkxT")
                .setChatStyle(ChatUtils.createClickStyle(ClickEvent.Action.OPEN_URL, "https://discord.gg/2nCbC9hkxT"))
            )

            modMessage("""
            
            §d§kOdinClientOnTopWeLoveOdinClientLiterallyTheBestMod
            ${ChatUtils.getChatBreak()}
            
        """.trimIndent(), "")
        }

        resetPositions()
    }

    private var hasSentMessage = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) = OdinClient.scope.launch {
        if (hasSentMessage) return@launch

        val newestVersion = try {
            Json.parseToJsonElement(WebUtils.fetchURLData("https://api.github.com/repos/odtheking/OdinClient/releases/latest"))
        } catch (e: Exception) { return@launch }

        val link = newestVersion.jsonObject["html_url"].toString().replace("\"", "")
        val tag = newestVersion.jsonObject["tag_name"].toString().replace("\"", "")
        var curVers = OdinClient.VERSION

        if (isSecondNewer(curVers, tag)) {
            hasSentMessage = true

            val def = AsyncUtils.waitUntilPlayer()
            try { def.await() } catch (e: Exception) { return@launch }

            OdinClient.mc.thePlayer.addChatMessage(
                ChatComponentText("§3Odin§bClient §8»§r §7Update available! §r${newestVersion.jsonObject["tag_name"].toString()} §e$link")
                    .setChatStyle(ChatUtils.createClickStyle(ClickEvent.Action.OPEN_URL, link))
            )
        }
    }

    fun isSecondNewer(current: String, second: String): Boolean {
        val (major, minor, patch) = current.split(".").map { it.toInt() }
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
        Category.values().forEach {
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
        display = ClickGUI
        super.onEnable()
        toggle()
    }
}
