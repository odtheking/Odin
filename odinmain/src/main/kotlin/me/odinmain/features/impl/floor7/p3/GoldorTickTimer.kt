package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.RealServerTick
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getTextWidth
import me.odinmain.utils.render.text
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.world.World
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GoldorTickTimer : Module(
    name = "Goldor Tick Timer",
    category = Category.FLOOR7,
    description = "Tick Timer for when goldor kills players"
) {
    private val startTimer: Boolean by BooleanSetting("Start Timer", default = true, description = "4.5 second countdown until terms/devices are able to be completed")
    private val hud: HudElement by HudSetting("Timer Hud", 10f, 10f, 1f, true) {
        if (it) {
            text("§7Tick: §a59t", 1f, 9f, Color.RED, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Tick: 119t", 12f) + 2f to 16f
        } else {
            val colorCode = when {
                tickTime.time >= 40 -> "§a"
                tickTime.time in 20..40 -> "§6"
                tickTime.time in 0..20 -> "§c"
                else -> return@HudSetting 0f to 0f
            }
            val text = if(isStarting)
                "§aStart"
            else "§8Tick"

            text("${text}: ${colorCode}${tickTime.time}t", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Start: 119t", 12f) + 2f to 12f
        }
    }

    data class Timer(var time: Int)
    private var tickTime = Timer(0)
    private var isStarting = false
    private var shouldLoad = false
    private val preStartRegex = Regex("\\[BOSS] Storm: I should have known that I stood no chance\\.")
    private val startRegex = Regex("\\[BOSS] Goldor: Who dares trespass into my domain\\?")
    private val endRegex = Regex("The Core entrance is opening!")

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val msg = event.message
        if (!msg.matches(preStartRegex) && !msg.matches(startRegex) && !msg.matches(endRegex) || msg.contains("Storm") && !startTimer) return
        isStarting = msg.contains("Storm")
        shouldLoad = !msg.contains("Core")
        if (!shouldLoad) return


        tickTime = if (isStarting)
            Timer(104)
        else Timer(60)
    }
    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        if(!shouldLoad) {
            tickTime = Timer(-1)
            return
        }
        tickTime.time--

        if (!isStarting && tickTime.time <= 0) {
            tickTime = Timer(60)
        }
    }

    init {
        onWorldLoad {
            shouldLoad = false
            tickTime = Timer(-1)
        }
    }

}