package me.odinmain.features.impl.skyblock

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.mcTextAndWidth
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.isHolding
import me.odinmain.utils.skyblock.skyblockID
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.Colors
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import kotlin.math.ceil

object AbilityTimers : Module(
    name = "Ability Timers",
    desc = "Provides timers for Wither Impact, Tactical Insertion, and Enrage."
) {
    private val witherHud by HudSetting("Wither Impact Hud", 10f, 10f, 1f, true) {
        if (witherImpactTicks <= 0 && (hideWhenDone || !LocationUtils.isInSkyblock) && !it) return@HudSetting 0f to 0f
        val width = if (compact) 6f else 65f
        RenderUtils.drawText(witherImpactText, width / 2f, 0f, 1f, Colors.WHITE, shadow = true, center = true)
        width to 12f
    }
    private val compact: Boolean by BooleanSetting("Compact Mode", true, desc = "Compacts the Hud to just one character wide.").withDependency { witherHud.enabled }
    private val hideWhenDone: Boolean by BooleanSetting("Hide When Ready", true, desc = "Hides the hud when the cooldown is over.").withDependency { witherHud.enabled }

    private val tacHud by HudSetting("Tactical Insertion Hud", 10f, 10f, 1f, true) {
        if (tacTimer == 0 && !it) return@HudSetting 0f to 0f
        mcTextAndWidth("§6Tac: ${tacTimer.color(40, 20)}${(tacTimer / 20f).toFixed()}s", 1f, 1f, 1f, color = Colors.WHITE, center = false) + 2f to 12f
    }

    private val enrageHud by HudSetting("Enrage Hud", 10f, 10f, 1f, true) {
        if (enrageTimer == 0 && !it) return@HudSetting 0f to 0f
        mcTextAndWidth("§4Enrage: ${enrageTimer.color(80, 40)}${(enrageTimer / 20f).toFixed()}s", 0f, 0f, 1f, Colors.WHITE, center = false) + 2f to 12f
    }

    private var witherImpactTicks: Int = -1
    private var enrageTimer = 0
    private var tacTimer = 0

    init {
        onPacket<S29PacketSoundEffect> {
            when {
                it.soundName == "mob.zombie.remedy" && it.pitch == 0.6984127f && it.volume == 1f && witherHud.enabled && witherImpactTicks != -1 -> witherImpactTicks = 100
                it.soundName == "fire.ignite" && it.pitch == 0.74603176f && it.volume == 1f && isHolding("TACTICAL_INSERTION") && tacHud.enabled -> tacTimer = 60
                it.soundName == "mob.zombie.remedy" && it.pitch == 1.0f && it.volume == 0.5f && mc.thePlayer?.getCurrentArmor(0)?.skyblockID == "REAPER_BOOTS" &&
                        mc.thePlayer?.getCurrentArmor(1)?.skyblockID == "REAPER_LEGGINGS" && mc.thePlayer?.getCurrentArmor(2)?.skyblockID == "REAPER_CHESTPLATE"
                        && enrageHud.enabled -> enrageTimer = 120
            }
        }

        onPacket<C08PacketPlayerBlockPlacement> {
            if (mc.thePlayer?.heldItem?.skyblockID?.equalsOneOf("ASTRAEA", "HYPERION", "VALKYRIE", "SCYLLA", "NECRON_BLADE") == false || witherImpactTicks != -1) return@onPacket
            witherImpactTicks = 0
        }

        onPacket<S32PacketConfirmTransaction> {
            if (witherImpactTicks > 0 && witherHud.enabled) witherImpactTicks--
            if (enrageTimer > 0  && enrageHud.enabled) enrageTimer--
            if (tacTimer > 0 && tacHud.enabled) tacTimer--
        }

        onWorldLoad {
            witherImpactTicks = -1
            enrageTimer = 0
            tacTimer = 0
        }
    }

    private inline val witherImpactText: String get() =
        if (compact) if (witherImpactTicks <= 0) "§aR" else "${witherImpactTicks.color(61, 21)}${ceil(witherImpactTicks / 20f).toInt()}"
        else if (witherImpactTicks <= 0) "§6Shield: §aReady" else "§6Shield: ${witherImpactTicks.color(61, 21)}${(witherImpactTicks / 20f).toFixed()}s"

    private fun Int.color(compareFirst: Int, compareSecond: Int): String {
        return when {
            this >= compareFirst-> "§e"
            this >= compareSecond -> "§6"
            else -> "§4"
        }
    }
}