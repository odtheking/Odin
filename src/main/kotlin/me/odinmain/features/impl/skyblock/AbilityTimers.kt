package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.mcText
import me.odinmain.utils.render.mcTextAndWidth
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.isHolding
import me.odinmain.utils.skyblock.skyblockID
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.math.ceil

object AbilityTimers : Module(
    "Ability Timers",
    description = "Various Timers for various items.",
    category = Category.SKYBLOCK
) {
    private val witherImpact: Boolean by BooleanSetting("Wither Impact", default = false, description = "Wither Impact cooldown timer.")
    private val compact: Boolean by BooleanSetting("Compact Mode", default = true, description = "Compacts the Hud to just one character wide.").withDependency { witherImpact }
    private val hideWhenDone: Boolean by BooleanSetting("Hide When Ready", default = true, description = "Hides the hud when the cooldown is over.").withDependency { witherImpact }
    private val witherHud: HudElement by HudSetting("Wither Impact Hud", 10f, 10f, 1f, true) {
        if (witherImpactTicks == 0 && (hideWhenDone || !LocationUtils.inSkyblock) && !it) return@HudSetting 0f to 0f
        val width = if (compact) 6f else 65f
        mcText(witherImpactText, width/2f, 0f, 1f, Color.WHITE, shadow = true)
        width to 10f
    }.withDependency { witherImpact }

    private val tacHud by HudSetting("Tactical Insertion Hud", 10f, 10f, 1f, true) {
        if (tacTimer == 0 && !it) return@HudSetting 0f to 0f
        mcTextAndWidth("§6Tac: ${tacTimer.color(40, 20)}${tacTimer.formatTicks}s", 1f, 1f, 1f, color = Color.WHITE, center = false) + 2f to 12f
    }

    private val enrageHud by HudSetting("Enrage Hud", 10f, 10f, 1f, true) {
        if (enrageTimer == 0 && !it) return@HudSetting 0f to 0f
        mcTextAndWidth("§4Enrage: ${enrageTimer.color(80, 40)}${enrageTimer.formatTicks}s", 0f, 0f, 1f, Color.WHITE, center = false) + 2f to 12f
    }

    private var witherImpactTicks = 0
    private var tacTimer = 0
    private var enrageTimer = 0

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            if (it.soundName == "mob.zombie.remedy" && it.pitch == 0.6984127f && it.volume == 1f && witherImpact) witherImpactTicks = 100
            if (it.soundName == "fire.ignite" && it.pitch == 0.74603176f && it.volume == 1f && isHolding("TACTICAL_INSERTION") && tacHud.enabled) tacTimer = 60
            if (it.soundName == "mob.zombie.remedy" && it.pitch == 1.0f && it.volume == 0.5f && mc.thePlayer?.getCurrentArmor(0)?.skyblockID == "REAPER_BOOTS" &&
                mc.thePlayer?.getCurrentArmor(1)?.skyblockID == "REAPER_LEGGINGS" && mc.thePlayer?.getCurrentArmor(2)?.skyblockID == "REAPER_CHESTPLATE"
                && enrageHud.enabled)
                enrageTimer = 120
        }

        onWorldLoad {
            witherImpactTicks = 0
            tacTimer = 0
            enrageTimer = 0
        }
    }

    private inline val witherImpactText: String get() =
        if (compact) if (witherImpactTicks == 0) "§aR" else "${witherImpactTicks.color(61, 21)}${ceil(witherImpactTicks / 20.0).toInt()}"
        else if (witherImpactTicks == 0) "§6Shield: §aReady" else "§6Shield: ${witherImpactTicks.color(61, 21)}${witherImpactTicks.formatTicks}s"

    private fun Int.color(compareFirst: Int, compareSecond: Int): String {
        return when {
            this >= compareFirst-> "§e"
            this >= compareSecond -> "§6"
            else -> "§4"
        }
    }

    private inline val Int.formatTicks get() = String.format(Locale.US, "%.2f", this / 20.0)

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        if (witherImpactTicks > 0 && witherImpact) witherImpactTicks--
        if (tacTimer > 0 && tacHud.enabled) tacTimer--
        if (enrageTimer > 0  && enrageHud.enabled) enrageTimer--
    }
}