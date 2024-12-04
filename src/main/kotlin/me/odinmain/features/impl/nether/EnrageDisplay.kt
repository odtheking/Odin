package me.odinmain.features.impl.nether

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.dsl.seconds
import com.github.stivais.aurora.transforms.impl.Alpha
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.skyblock.skyblockID
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.buildText
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EnrageDisplay : Module(
    name = "Enrage Display",
    description = "Displays the Reaper armor's ability duration."
) {
    private val unit by SelectorSetting("Unit", arrayListOf("Seconds", "Ticks"), description = "The unit of time to display.")
    private val showUnit by BooleanSetting("Show unit", default = false, description = "Displays the unit of time for the enrage duration.")

    private val animation = Alpha.Animated(from = 0f, to = 1f)

    private val HUD by TextHUD("Enrage Display") { color, font, shadow ->
        if (!preview) transform(animation)
        buildText(
            string = "Enrage:",
            supplier = { getDisplay(if (preview) 120 else enrageTimer) },
            font = font, color1 = color, color2 = Colors.WHITE, shadow
        )
    }.registerSettings(::unit, ::showUnit).setting("Displays the duration on screen.")

    private fun getDisplay(ticks: Int): String {
        return when (unit) {
            0 -> "${ticks / 20}${if (showUnit) "s" else ""}"
            else -> "$ticks${if (showUnit) "t" else ""}"
        }
    }

    private val reaperArmor = listOf("REAPER_BOOTS", "REAPER_LEGGINGS", "REAPER_CHESTPLATE")
    private var enrageTimer = -1

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            if (it.soundName != "mob.zombie.remedy" || it.pitch != 1.0f || it.volume != 0.5f || reaperArmor.any { id -> mc.thePlayer?.getCurrentArmor(reaperArmor.indexOf(id))?.skyblockID != id }) return@onPacket
            enrageTimer = 120
            animation.animate(0.25.seconds, Animation.Style.EaseOutQuint)
        }
    }

    @SubscribeEvent
    fun onTick(event: ServerTickEvent) {
        if (enrageTimer == 0) animation.animate(0.25.seconds, Animation.Style.EaseOutQuint)
        enrageTimer--
    }
}