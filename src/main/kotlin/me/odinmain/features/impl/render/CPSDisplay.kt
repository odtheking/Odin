package me.odinmain.features.impl.render

import me.odinmain.events.impl.ClickEvent
import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.dropShadow
import me.odinmain.utils.render.roundedRectangle
import me.odinmain.utils.ui.clickgui.animations.impl.EaseInOut
import me.odinmain.utils.ui.clickgui.util.ColorUtil.brighter
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CPSDisplay : Module(
    name = "CPS Display",
    description = "Displays your clicks per second."
) {
    private val countPackets by BooleanSetting("Count Packets", false, description = "Counts packets sent outside of the rightclickmouse method, this will be better at detecting other mods' auto clickers, but might show inaccurate values.")
    private val advanced by DropdownSetting("Show Settings", false)
    private val button by SelectorSetting("Button", "Both", arrayListOf("Left", "Right", "Both"), description = "The button to display the CPS of.").withDependency { advanced }
    private val mouseText by BooleanSetting("Show Button", true, description = "Shows the button name.").withDependency { advanced }
    private val color by ColorSetting("Color", Color(21, 22, 23, 0.5f), allowAlpha = true, description = "The color of the display.").withDependency { advanced }
    private val textColor by ColorSetting("Text Color", Color(239, 239, 239, 1f), allowAlpha = true, description = "The color of the text.").withDependency { advanced }
    private val outline by BooleanSetting("Outline", true, description = "Adds an outline to the display.").withDependency { advanced }
    private val hud by HudSetting("Display", 10f, 10f, 2f, false) {
        leftClicks.removeAll { System.currentTimeMillis() - it > 1000 }
        rightClicks.removeAll { System.currentTimeMillis() - it > 1000 }

        val value = if (button == 0) "${leftClicks.size}" else "${rightClicks.size}"
        val anim = if (button == 0) leftAnim else rightAnim
        val color = color.brighter(leftAnim.get(1f, 1.5f, leftAnim.getPercent() >= 50))
        if (button == 2) {
            roundedRectangle(0f, 0f, 50f, 38f, color, color, color, 0f , 9f, 0f, 9f, 0f, 0f)
            roundedRectangle(50f, 0f, 50f, 38f, color, color, color, 0f , 0f, 9f, 0f, 9f, 0f)

            if (outline) dropShadow(0f, 0f, 100f, 36f, 10f)
        } else {
            roundedRectangle(0f, 0f, 50f, 36f, color.brighter(anim.get(1f, 1.5f, anim.getPercent() >= 50)), 9f)
            if (outline) dropShadow(0f, 0f, 50f, 36f, 10f)
        }

        if (mouseText) {
            if (button == 2) {
                RenderUtils.drawText("LMB", 15f, 1f, 1.0, textColor, center = false)
                RenderUtils.drawText(leftClicks.size.toString(), 20f, 15f, 2.0, textColor, center = false)

                RenderUtils.drawText("RMB", 65f, 1f, 1.0, textColor, center = false)
                RenderUtils.drawText(rightClicks.size.toString(), 70f, 15f, 2.0, textColor, center = false)
            } else {
                val text = if (button == 0) "LMB" else "RMB"
                RenderUtils.drawText(text, 15f, 1f, 1.0, textColor, center = false)
                RenderUtils.drawText(value, 20f, 15f, 2.0, textColor, center = false)
            }
        } else {
            if (button == 2) {
                RenderUtils.drawText(leftClicks.size.toString(), 15f, 10f, 2.0, textColor, center = false)
                RenderUtils.drawText(rightClicks.size.toString(), 65f, 10f, 2.0, textColor, center = false)
            } else RenderUtils.drawText(value, 20f, 10f, 2.0, textColor, center = false)
        }
        if (button == 2) 100f to 38f else 50f to 38f
    }

    private val leftAnim = EaseInOut(300)
    private val rightAnim = EaseInOut(300)

    private val leftClicks = mutableListOf<Long>()
    private val rightClicks = mutableListOf<Long>()

    @SubscribeEvent
    fun onLeftClick(event: ClickEvent.Left) {
        leftClicks.add(System.currentTimeMillis())
        leftAnim.start(true)
    }

    @SubscribeEvent
    fun onRightClick(event: ClickEvent.Right) {
        rightClicks.add(System.currentTimeMillis())
        rightAnim.start(true)
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketEvent.Send) { // This is for any block placement packet that gets sent outside the rightclickmouse method :eyes:
        if (event.packet !is C08PacketPlayerBlockPlacement || !countPackets) return
        if (rightClicks.any { System.currentTimeMillis() - it < 5 }) return
        onRightClick(ClickEvent.Right())
    }
}