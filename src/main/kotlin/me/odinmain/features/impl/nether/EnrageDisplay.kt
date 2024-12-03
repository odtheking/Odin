package me.odinmain.features.impl.nether

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.dsl.seconds
import com.github.stivais.aurora.transforms.impl.Alpha
import me.odinmain.events.impl.PacketEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.skyblock.skyblockID
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EnrageDisplay : Module(
    name = "Enrage Display",
    description = "Displays the Reaper armor's ability duration."
) {
    private val unit by SelectorSetting("Unit", arrayListOf("Seconds", "Ticks"), description = "The unit of time to display.")
    private val showUnit by BooleanSetting("Show unit", default = false, description = "Displays the unit of time for the enrage duration.")

    // test
    private val animation = Alpha.Animated(from = 0f, to = 1f)

//    private val HUD = TextHUD("Enrage Display") { color, font ->
//        if (!preview) transform(animation)
//        text(
//            "Enrage ",
//            color = color,
//            font = font,
//            size = 30.px
//        ) and text({ getDisplay(if (preview) 120 else enrageTimer) }, font = font)
//    }.registerSettings(
//        ::unit,
//        ::showUnit
//    ).setting("Displays the duration on screen.")

    private fun getDisplay(ticks: Int): String {
        return when (unit) {
            0 -> "${ticks / 20}${if (showUnit) "s" else ""}"
            else -> "$ticks${if (showUnit) "t" else ""}"
        }
    }

    private var enrageTimer = -1

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        val packet = event.packet as? S29PacketSoundEffect ?: return
        if (packet.soundName == "mob.zombie.remedy" && packet.pitch == 1.0f && packet.volume == 0.5f) {
            if (
                mc.thePlayer?.getCurrentArmor(0)?.skyblockID == "REAPER_BOOTS" &&
                mc.thePlayer?.getCurrentArmor(1)?.skyblockID == "REAPER_LEGGINGS" &&
                mc.thePlayer?.getCurrentArmor(2)?.skyblockID == "REAPER_CHESTPLATE"
            ) {
                enrageTimer = 120
                animation.animate(0.25.seconds, Animation.Style.EaseOutQuint)
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ServerTickEvent) {
        enrageTimer--
        if (enrageTimer == 0) {
            animation.animate(0.25.seconds, Animation.Style.EaseOutQuint)
        }
    }
}