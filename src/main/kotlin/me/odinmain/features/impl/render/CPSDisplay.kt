package me.odinmain.features.impl.render

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.DropdownSetting
import me.odinmain.clickgui.settings.impl.SelectorSetting
import me.odinmain.events.impl.ClickEvent
import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CPSDisplay : Module(
    name = "CPS Display",
    description = "Displays your clicks per second."
) {
    private val countPackets by BooleanSetting("Count Packets", false, desc = "Counts packets sent outside of the rightclickmouse method, this will be better at detecting other mods' auto clickers, but might show inaccurate values.")
    private val advanced by DropdownSetting("Show Settings", false)
    private val button by SelectorSetting("Button", "Both", arrayListOf("Left", "Right", "Both"), desc = "The button to display the CPS of.").withDependency { advanced }
    private val mouseText by BooleanSetting("Show Button", true, desc = "Shows the button name.").withDependency { advanced }
    private val textColor by ColorSetting("Text Color", Color(239, 239, 239, 1f), allowAlpha = true, desc = "The color of the text.").withDependency { advanced }
    private val hud by HUD("Display", "Displays your clicks per second in the HUD.") {
        leftClicks.removeAll { System.currentTimeMillis() - it > 1000 }
        rightClicks.removeAll { System.currentTimeMillis() - it > 1000 }

        val value = if (button == 0) "${leftClicks.size}" else "${rightClicks.size}"

        if (mouseText) {
            if (button == 2) {
                RenderUtils.drawText("LMB", 1f, 1f, textColor)
                RenderUtils.drawText(leftClicks.size.toString(), 7f, 15f, textColor)

                RenderUtils.drawText("RMB", 35f, 1f, textColor)
                RenderUtils.drawText(rightClicks.size.toString(), 42f, 15f, textColor)
            } else {
                val text = if (button == 0) "LMB" else "RMB"
                RenderUtils.drawText(text, 1f, 1f, textColor)
                RenderUtils.drawText(value, 7f, 15f, textColor)
            }
        } else {
            if (button == 2) {
                RenderUtils.drawText(leftClicks.size.toString(), 1f, 10f, textColor)
                RenderUtils.drawText(rightClicks.size.toString(), 35f, 10f, textColor)
            } else RenderUtils.drawText(value, 5f, 10f, textColor)
        }
        if (button == 2) 54f to 24f else 20f to 24f
    }

    private val leftClicks = mutableListOf<Long>()
    private val rightClicks = mutableListOf<Long>()

    @SubscribeEvent
    fun onLeftClick(event: ClickEvent.Left) {
        leftClicks.add(System.currentTimeMillis())
    }

    @SubscribeEvent
    fun onRightClick(event: ClickEvent.Right) {
        rightClicks.add(System.currentTimeMillis())
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketEvent.Send) { // This is for any block placement packet that gets sent outside the rightclickmouse method :eyes:
        if (event.packet !is C08PacketPlayerBlockPlacement || !countPackets) return
        if (rightClicks.any { System.currentTimeMillis() - it < 5 }) return
        onRightClick(ClickEvent.Right())
    }
}