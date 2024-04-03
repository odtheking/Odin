package me.odinmain.features.impl.skyblock

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
import me.odinmain.utils.skyblock.itemID
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EnrageDisplay : Module (
    name = "Enrage Display",
    description = "Timer for cooldown of reaper armor enrage",
    category = Category.SKYBLOCK
) {
    private val hud: HudElement by HudSetting("Enrage Timer Hud", 10f, 10f, 1f, true) {
        if (it) {
            text("§4Enrage§f: 119t", 1f, 9f, Color.RED, 12f, OdinFont.REGULAR, shadow = true)
            getTextWidth("Enrage: 119t", 12f) + 2f to 16f
        } else {
            if (enrageTime.time >= 60) {
                text("§4Enrage§f: ${enrageTime.time}t", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR, shadow = true)
            }
            if (enrageTime.time in 30..60) {
                text("§4Enrage§f: §e${enrageTime.time}t", 1f, 9f, Color.YELLOW, 12f, OdinFont.REGULAR, shadow = true)
            }
            if (enrageTime.time in 0..30) {
                text("§4Enrage§f: §4${enrageTime.time}t", 1f, 9f, Color.RED, 12f, OdinFont.REGULAR, shadow = true)
            }


            getTextWidth("Enrage: 119t", 12f) + 2f to 12f
        }
    }
    data class Timer(var time: Int)
    private var enrageTime = Timer(0)

    @SubscribeEvent
    fun onSound(event:PlaySoundEvent) {
        if (event.name != "mob.zombie.remedy" || event.sound.pitch != 1.0f || event.sound.volume != .5f || mc.thePlayer?.getCurrentArmor(0)?.itemID != "REAPER_BOOTS" || mc.thePlayer?.getCurrentArmor(1)?.itemID != "REAPER_LEGGINGS" || mc.thePlayer?.getCurrentArmor(2)?.itemID != "REAPER_CHESTPLATE") return
        enrageTime = Timer(120)
    }
    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        enrageTime.time--
    }

}