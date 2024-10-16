package me.odinmain.features.impl.nether

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.utils.seconds
import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.skyblock.itemID
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.and
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object EnrageDisplay : Module(
    name = "Enrage Display",
    description = "Timer for cooldown of reaper armor enrage"
) {
    private val unit by SelectorSetting("Unit", arrayListOf("Seconds", "Ticks"))
    private val showUnit by BooleanSetting("Show unit", default = false)

    val animatable = Animatable(0.0.px, 1.px)

    private val HUD = TextHUD(2.5.percent, 2.5.percent) { color, font ->
        if (!preview) element.alphaAnim = animatable
        text(
            "Enrage ",
            color = color,
            font = font,
            size = 30.px
        ) and text({ getDisplay(if (preview) 120 else enrageTimer) }, font = font)
    }.setting(
        ::showUnit,
        ::unit,
    ).setting("Enrage Display")

    private fun getDisplay(ticks: Int): String {
        return when (unit) {
            0 -> "${ticks / 20}${if (showUnit) "s" else ""}"
            else -> "$ticks${if (showUnit) "t" else ""}"
        }
    }

    private var enrageTimer = -1

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            if (it.soundName == "mob.zombie.remedy" && it.pitch == 1.0f && it.volume == 0.5f && mc.thePlayer?.getCurrentArmor(
                    0
                )?.itemID == "REAPER_BOOTS" &&
                mc.thePlayer?.getCurrentArmor(1)?.itemID == "REAPER_LEGGINGS" && mc.thePlayer?.getCurrentArmor(2)?.itemID == "REAPER_CHESTPLATE"
            )
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
                if (enrageTimer == 0) {
                    animatable.animate(0.25.seconds, Animations.EaseOutQuint)
                }
            }
        }
    }

    override fun onKeybind() {
        enrageTimer = 120
        animatable.animate(0.25.seconds, Animations.EaseOutQuint)
    }
}