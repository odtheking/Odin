package me.odinclient.features.impl.skyblock

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.isHolding
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import net.minecraft.client.settings.KeyBinding
import kotlin.concurrent.schedule
import java.util.Timer

object EasyEther : Module(
    name = "Easy Etherwarp",
    description = "Etherwarps when a key/button is pressed.",
    category = Category.SKYBLOCK,
    tag = TagType.RISKY
) {
    private val etherOnLC: Boolean by BooleanSetting("On Left Click", true, description = "Etherwarps on left click.")
    private val etherOnShift: Boolean by BooleanSetting("On Sneak", true, description = "Etherwarps on sneak.")

    @SubscribeEvent
    fun onMouseEvent(event: MouseEvent) {
        if (etherOnLC && event.button == 0 && event.buttonstate) {
            if (isHolding("ASPECT_OF_THE_VOID")) {
                if (!etherOnShift) {
                    performEtherwarp(withSneak = true)
                } else {
                    performEtherwarp(withSneak = false)
                }
            }
        }
    }

    @SubscribeEvent
    fun onKeyInput(event: KeyInputEvent) {
        if (etherOnShift && mc.gameSettings.keyBindSneak.isKeyDown) {
            if (isHolding("ASPECT_OF_THE_VOID")) {
                performEtherwarp(withSneak = false)
            }
        }
    }

    private fun performEtherwarp(withSneak: Boolean) {
        if (withSneak) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.keyCode, true)
            mc.thePlayer.isSneaking = true
        }

        Timer().schedule(500) {
            rightClick()

            if (withSneak) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.keyCode, false)
                mc.thePlayer.isSneaking = false
            }
        }
    }
}