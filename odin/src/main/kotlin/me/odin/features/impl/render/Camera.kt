package me.odin.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Camera : Module(
    name = "Camera",
    category = Category.RENDER,
    description = "Allows you to change camera settings."
) {
    private val frontCamera by BooleanSetting("No Front Camera", description = "Disables the front camera.")
    private val customFOV by BooleanSetting("Custom FOV", description = "Allows you to change the FOV.")
    private val fov by NumberSetting("FOV", mc.gameSettings.fovSetting, 1f, 180f, 1f, description = "Changes the FOV.").withDependency { customFOV }

    private var previousFov = mc.gameSettings.fovSetting

    override fun onEnable() {
        previousFov = mc.gameSettings.fovSetting
        super.onEnable()
    }

    override fun onDisable() {
        mc.gameSettings.fovSetting = previousFov
        super.onDisable()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        if (customFOV && mc.gameSettings.fovSetting != fov)
            mc.gameSettings.fovSetting = fov

        if (frontCamera && mc.gameSettings.thirdPersonView == 2)
            mc.gameSettings.thirdPersonView = 0
    }
}