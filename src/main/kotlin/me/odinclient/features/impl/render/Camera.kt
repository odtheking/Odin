package me.odinclient.features.impl.render

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.ui.hud.HudElement
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.nvg.TextAlign
import me.odinclient.utils.render.gui.nvg.TextPos
import me.odinclient.utils.render.gui.nvg.getTextWidth
import me.odinclient.utils.render.gui.nvg.text
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Camera : Module(
    "Camera",
    category = Category.RENDER,
) {
    private val frontCamera: Boolean by BooleanSetting("No Front Camera", false)
    private val cameraDist: Float by NumberSetting("Distance", 4f, 3.0, 12.0, 0.5)

    private val color: Color by ColorSetting("HUD Color", Color(255, 0, 0))

    private val hud: HudElement by HudSetting("Name", 10f, 10f, 1f, true) { example ->
        val string = if (example) "Example Hud" else "Not Example Hud"
        text(string, 0f, 0f, color, 16f, Fonts.REGULAR, TextAlign.Left, TextPos.Top)
        getTextWidth(string, 16f, Fonts.REGULAR) to 16f
    }

    fun getCameraDistance(): Float {
        return if (enabled) cameraDist else 4f
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (frontCamera && mc.gameSettings.thirdPersonView == 2) {
            mc.gameSettings.thirdPersonView = 0
        }
    }
}