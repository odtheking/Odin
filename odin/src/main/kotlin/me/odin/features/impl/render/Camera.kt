package me.odin.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Camera : Module(
    name = "Camera",
    category = Category.RENDER,
    description = "Allows you to change camera settings."
) {
    private val frontCamera by BooleanSetting("No Front Camera", description = "Disables the front camera.")

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return

        if (frontCamera && mc.gameSettings.thirdPersonView == 2)
            mc.gameSettings.thirdPersonView = 0
    }
}