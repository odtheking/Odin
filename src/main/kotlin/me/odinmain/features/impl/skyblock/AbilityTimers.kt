package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Colors
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.isHolding
import me.odinmain.utils.skyblock.skyblockID
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.drawStringWidth
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.ceil

object AbilityTimers : Module(
    name = "Ability Timers",
    description = "Provides timers for Wither Impact, Tactical Insertion, and Enrage."
) {
    private val witherHud by HUD("Wither Impact Hud", "Displays the cooldown of Wither Impact.") {
        if (witherImpactTicks <= 0 && (hideWhenDone || !LocationUtils.isInSkyblock) && !it) return@HUD 0f to 0f
        drawStringWidth(witherImpactText, 1f, 1f, Colors.WHITE) + 2f to 10f
    }
    private val compact: Boolean by BooleanSetting("Compact Mode", true, desc = "Compacts the Hud to just one character wide.").withDependency { witherHud.enabled }
    private val hideWhenDone: Boolean by BooleanSetting("Hide When Ready", true, desc = "Hides the hud when the cooldown is over.").withDependency { witherHud.enabled }

    private val tacHud by HUD("Tactical Insertion Hud", "Displays the cooldown of Tactical Insertion.") {
        if (tacTimer == 0 && !it) return@HUD 0f to 0f
        drawStringWidth("§6Tac: ${tacTimer.color(40, 20)}${(tacTimer / 20f).toFixed()}s", 1f, 1f, color = Colors.WHITE) + 2f to 10f
    }

    private val enrageHud by HUD("Enrage Hud", "Displays the cooldown of Enrage.") {
        if (enrageTimer == 0 && !it) return@HUD 0f to 0f
        drawStringWidth("§4Enrage: ${enrageTimer.color(80, 40)}${(enrageTimer / 20f).toFixed()}s", 1f, 1f, Colors.WHITE) + 2f to 10f
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

        onWorldLoad {
            witherImpactTicks = -1
            enrageTimer = 0
            tacTimer = 0
        }
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        if (witherImpactTicks > 0 && witherHud.enabled) witherImpactTicks--
        if (enrageTimer > 0  && enrageHud.enabled) enrageTimer--
        if (tacTimer > 0 && tacHud.enabled) tacTimer--
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