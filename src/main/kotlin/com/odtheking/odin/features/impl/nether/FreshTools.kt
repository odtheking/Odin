package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.toFixed

object FreshTools : Module(
    name = "Fresh Tools",
    description = "Tracks your party's fresh tools time.",
) {
    private val notifyFresh by BooleanSetting("Notify Fresh", true, desc = "Notifies your party when you get fresh timer.")
    private val hud by HUD("Fresh timer", "Displays how long players have fresh for.", example = {
        text("§6Player1§f: 9s", 0, 0)
        textDim("§6Player2§f: 5s", 0, mc.font.lineHeight).first to mc.font.lineHeight * 2
    }) {
        if (!KuudraUtils.inKuudra || KuudraUtils.phase != 2) return@HUD

        var yOffset = 0
        KuudraUtils.freshers.forEach { fresher ->
            val timeLeft = KuudraUtils.freshTimeLeft(fresher.value).takeIf { it > 0 } ?: return@forEach
            text("§6${fresher.key}§f: ${(timeLeft / 1000f).toFixed()}s", 0, yOffset)
            yOffset += mc.font.lineHeight
        }
    }

    private val ownFreshRegex = Regex("^Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!$")

    init {
        on<MessageEvent.Chat> {
            if (notifyFresh && KuudraUtils.inKuudra && ownFreshRegex.matches(message))
                sendCommand("pc FRESH")
        }
    }
}