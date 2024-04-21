package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.render.mcText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NecronDropTimer : Module(
    name = "Necron Drop Timer",
    description = "Shows a timer for when Necron drops you down.",
    category = Category.FLOOR7
) {
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            mcText("Necron dropping in §a65", 1f, 1f, 2, Color.DARK_RED, shadow = true ,center = false)
            getMCTextWidth("Necron dropping in 65") * 2 + 2f to 16f
        } else if (timer > 0) {
            mcText("§4Necron dropping in ${colorizeTime(timer)}$timer", 1f, 1f, 2, Color.DARK_RED, shadow = true ,center = false)
            getMCTextWidth("Necron dropping in 65") * 2 + 2f to 16f
        } else 0f to 0f
    }

    private fun colorizeTime(time: Byte): String {
        return when {
            time > 30 -> "§a"
            time > 10 -> "§e"
            else -> "§c"
        }
    }

    private var timer: Byte = 0

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        if (timer > 0) {
            timer--
        }
    }
    init {
        onMessage("[BOSS] Necron: I'm afraid, your journey ends now.", false) {
            timer = 60
        }
    }
}