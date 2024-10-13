package me.odinmain.features.impl.nether

import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.skyblock.itemID
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.and
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object EnrageDisplay : Module (
    name = "Enrage Display",
    description = "Timer for cooldown of reaper armor enrage"
) {
    private val showUnit by BooleanSetting("Show unit", default = false)
    private val unit by SelectorSetting("Unit", arrayListOf("Seconds", "Ticks")).withDependency { showUnit }

    private val HUD = TextHUD(2.5.percent, 2.5.percent) { color, font ->
        if (preview) {
            text(
                "Enrage  ",
                color = color,
                font = font,
                size = 30.px
            ) and text(getDisplay(120), font = font)
        } else {
            needs { enrageTimer >= 0f }
            text(
                "Enrage  ",
                color = color,
                font = font,
                size = 30.px
            ) and text({ getDisplay(enrageTimer) }, font = font)
        }
    }.setting(
        ::showUnit,
        ::unit,
    ).setting("Enrage Display")

    /*private val hud: HudElement by HudSetting("Enrage Timer Hud", 10f, 10f, 1f, false) {
        if (it) {
            text("§4Enrage: §a119t", 1f, 9f, Color.RED, 12f, 0, shadow = true)
            getTextWidth("Enrage: 119t", 12f) + 2f to 16f
        } else {
            val colorCode = when {
                enrageTimer >= 60 -> "§a"
                enrageTimer in 30..60 -> "§e"
                enrageTimer in 0..30 -> "§c"
                else -> return@HudSetting 0f to 0f
            }
            text("§4Enrage: ${colorCode}${enrageTimer}t", 1f, 9f, Color.WHITE, 12f, 0, shadow = true)
            getTextWidth("Enrage: 119t", 12f) + 2f to 12f
        }
    }*/

    private fun getDisplay(ticks: Int): String {
        return when (unit) {
            0 -> "${ticks / 20}${if (showUnit) "s" else ""}"
            else -> "$ticks${if (showUnit) "t" else ""}"
        }
    }

    private var enrageTimer = -1

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            if (it.soundName == "mob.zombie.remedy" && it.pitch == 1.0f && it.volume == 0.5f && mc.thePlayer?.getCurrentArmor(0)?.itemID == "REAPER_BOOTS" &&
                mc.thePlayer?.getCurrentArmor(1)?.itemID == "REAPER_LEGGINGS" && mc.thePlayer?.getCurrentArmor(2)?.itemID == "REAPER_CHESTPLATE")
                    enrageTimer = 120
        }
    }

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        enrageTimer--
    }

    init {
        onEvent<ClientTickEvent> { event ->
            if (event.phase == TickEvent.Phase.END) {
                enrageTimer--
            }
        }
    }

    override fun onKeybind() {
        enrageTimer = 120
    }
}