package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.modMessage

object WarpCooldown : Module (
    name = "Warp Cooldown",
    description = "Timer before you can enter a new dungeon",
    category = Category.DUNGEON
) {
    private val hud: HudElement by HudSetting("Warp Timer Hud", 10f, 10f, 1f, true) {
        if (it) {
            text("§eWarp: §a30s", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Warp: 30s", 12f) + 2f to 16f
        } else {
            if (warpTimer.timeLeft() <= 0) return@HudSetting 0f to 0f
            text("§eWarp: §a${String.format("%.2f", warpTimer.timeLeft().toFloat() / 1000)}s", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("§eWarp: §a30s", 12f) + 2f to 12f
        }
    }

    private var warpTimer = Clock(30000)
    private val warpRegex = Regex("\\[[^]]+] (\\w+) entered \\w+ Catacombs, Floor (\\w+)!")

    init {
        onMessage(Regex("(?s).+")) {
            if (!it.contains(warpRegex)) return@onMessage
                warpTimer.updateCD()
        }
    }
}