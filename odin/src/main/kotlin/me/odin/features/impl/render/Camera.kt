package me.odin.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Camera : Module(
    "Camera",
    category = Category.RENDER,
    description = "Allows you to change qualities about third person view."
) {
    private val frontCamera: Boolean by BooleanSetting("No Front Camera")

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (frontCamera && mc.gameSettings.thirdPersonView == 2)
            mc.gameSettings.thirdPersonView = 0
    }
}