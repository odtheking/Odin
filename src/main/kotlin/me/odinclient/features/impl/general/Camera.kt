package me.odinclient.features.impl.general

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.ui.hud.TextHud
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Camera : Module(
    "Camera",
    category = Category.GENERAL
) {
    private val frontCamera: Boolean by BooleanSetting("No Front Camera", false)
    private val cameraDist: Float by NumberSetting("Distance", 4f, 3.0, 12.0, 0.5)

    private val hud: Boolean by HudSetting(name = "Default Hud", hud = CameraHud)

    fun getCameraDistance(): Float = if (enabled) cameraDist else 4f

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (frontCamera && mc.gameSettings.thirdPersonView == 2) {
            mc.gameSettings.thirdPersonView = 0
        }
    }

    object CameraHud : TextHud(0f, 0f) {
        override fun getLines(example: Boolean): MutableList<String> {
            return if (example) {
                mutableListOf(
                    "Example Camera Hud"
                )
            } else mutableListOf(
                "CameraHud"
            )
        }
    }
}