package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.StringSetting
import me.odinmain.events.impl.ArrowEvent
import me.odinmain.features.Module
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.ui.getTextWidth
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ArrowHit : Module(
    name = "Arrow hit",
    description = "Counts how many arrows you hit in certain time periods."
) {
    private val resetOnNumber by BooleanSetting("Reset on number", false, desc = "Reset the arrow count after a certain number of arrows.")
    private val resetCount by StringSetting("Reset count", 999999.toString(), 16, desc = "The amount of arrows to hit before resetting the count.")
    private val resetOnWorldLoad by BooleanSetting("Reset on world load", true, desc = "Reset the arrow count when you join a world.")
    val resetOnDragons by BooleanSetting("Reset on dragon spawn", true, desc = "Reset the arrow count when a m7 dragon has spawned.")

    private var arrowCount = 0

    private val hud by HUD("Display", "Shows the amount of arrows hit in the HUD.") {
        RenderUtils.drawText("${if (it) "156" else arrowCount}", 1f, 1f, Colors.WHITE)
        getTextWidth("156").toFloat() to mc.fontRendererObj.FONT_HEIGHT
    }
    init {
        onWorldLoad { if (resetOnWorldLoad) arrowCount = 0  }
    }

    @SubscribeEvent
    fun onArrowHit(event: ArrowEvent.Hit) {
        arrowCount++
        if (arrowCount >= (resetCount.toIntOrNull() ?: 9999) && resetOnNumber) arrowCount = 0
    }

    fun onDragonSpawn() {
        arrowCount = 0
    }

    override fun onKeybind() {
        if (mc.currentScreen != null || !enabled) return
        arrowCount = 0
    }
}
