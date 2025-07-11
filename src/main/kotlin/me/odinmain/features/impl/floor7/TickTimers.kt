package me.odinmain.features.impl.floor7

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.features.Module
import me.odinmain.utils.render.Colors
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.drawStringWidth
import net.minecraft.network.play.server.S32PacketConfirmTransaction

object TickTimers : Module(
    name = "Tick Timers",
    description = "Displays timers for Necron, Goldor, and Storm."
) {
    private val displayInTicks by BooleanSetting("Display in Ticks", false, desc = "Display the timers in ticks instead of seconds.")
    private val symbolDisplay: Boolean by BooleanSetting("Display Symbol", true, desc = "Displays s or t after the timers.")
    private val showPrefix: Boolean by BooleanSetting("Show Prefix", true, desc = "Shows the prefix of the timers.")

    private val necronHud by HUD("Necron Hud", "Displays a timer for Necron's drop.") {
        if (it)                   drawStringWidth(formatTimer(35, 60, "§4Necron dropping in"), 1f, 1f, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        else if (necronTime >= 0) drawStringWidth(formatTimer(necronTime.toInt(), 60, "§4Necron dropping in"), 1f, 1f, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        else 0f to 0f
    }

    private var necronTime: Byte = -1

    private val goldorHud by HUD("Goldor Hud", "Displays a timer for Goldor's Core entrance opening.") {
        if (it) drawStringWidth(formatTimer(35, 60, "§7Tick:"), 1f, 1f, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        else if ((goldorStartTime >= 0 && startTimer) || goldorTickTime >= 0) {
            val (prefix: String, time: Int, max: Int) = if (goldorStartTime >= 0 && startTimer) Triple("§aStart:", goldorStartTime, 104) else Triple("§7Tick:", goldorTickTime, 60)
            drawStringWidth(formatTimer(time, max, prefix), 1f, 1f, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        } else 0f to 0f
    }
    private val startTimer: Boolean by BooleanSetting("Start timer", false, desc = "Displays a timer counting down until devices/terms are able to be activated/completed.").withDependency { goldorHud.enabled }

    private var goldorTickTime: Int = -1
    private var goldorStartTime: Int = -1

    private val stormHud by HUD("Storm Pad Hud", "Displays a timer for Storm's Pad.") {
        if (it)                    drawStringWidth(formatTimer(15, 20, "§bPad:"), 1f, 1f, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        else if (padTickTime >= 0) drawStringWidth(formatTimer(padTickTime, 20, "§bPad:"), 1f, 1f, Colors.MINECRAFT_DARK_RED) + 2f to 10f
        else 0f to 0f
    }

    private var padTickTime: Int = -1

    init {
        onMessage(Regex("^\\[BOSS] Necron: I'm afraid, your journey ends now\\.$"), { enabled && necronHud.enabled && DungeonUtils.inDungeons }) { necronTime = 60 }

        onMessage(Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$"), { enabled && goldorHud.enabled && DungeonUtils.inDungeons }) { goldorTickTime = 60 }
        onMessage(Regex("^The Core entrance is opening!$"), { enabled && goldorHud.enabled && DungeonUtils.inDungeons }) {
            goldorTickTime = -1
            goldorStartTime = -1
        }

        onMessage(Regex("^\\[BOSS] Storm: I should have known that I stood no chance\\.$")) {
            if (!DungeonUtils.inDungeons) return@onMessage
            if (goldorHud.enabled) goldorStartTime = 104
            if (stormHud.enabled) padTickTime = -1
        }

        onMessage(Regex("^\\[BOSS] Storm: Pathetic Maxor, just like expected\\.$"), { enabled && stormHud.enabled && DungeonUtils.inDungeons }) { padTickTime = 20 }

        onPacket<S32PacketConfirmTransaction> {
            if (necronTime >= 0 && necronHud.enabled) necronTime--
            if (padTickTime >= 0 && stormHud.enabled) padTickTime--
            if (padTickTime == 0 && stormHud.enabled) padTickTime = 20
            if (goldorTickTime >= 0 && goldorHud.enabled) goldorTickTime--
            if (goldorStartTime >= 0 && goldorHud.enabled) goldorStartTime--
            if (goldorTickTime == 0 && goldorStartTime <= 0 && goldorHud.enabled) { goldorTickTime = 60 }
        }

        onWorldLoad {
            necronTime = -1
            goldorTickTime = -1
            goldorStartTime = -1
            padTickTime = -1
        }
    }

    private fun formatTimer(time: Int, max: Int, prefix: String): String {
        val color = when {
            time.toFloat() >= max * 0.66 -> "§a"
            time.toFloat() >= max * 0.33 -> "§6"
            else -> "§c"
        }
        val timeDisplay = if (displayInTicks) "$time${if (symbolDisplay) "t" else ""}" else "${(time / 20f).toFixed()}${if (symbolDisplay) "s" else ""}"
        return "${if (showPrefix) "$prefix " else ""}$color$timeDisplay"
    }
}