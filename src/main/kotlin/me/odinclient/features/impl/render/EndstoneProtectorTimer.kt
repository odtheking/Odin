package me.odinclient.features.impl.render

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.LocationUtils.inSkyblock
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.round


object EndstoneProtectorTimer : Module(
    name = "Endstone Protector Timer",
    description = "Timer under your reticle to know when the Endstone Protector will spawn.",
    category = Category.RENDER,
) {

    private var timer: Long = 0

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!inSkyblock) return
        if (StringUtils.stripControlCodes(event.message.unformattedText) == "The ground begins to shake as an Endstone Protector rises from below!") {
            mc.thePlayer.playSound("random.orb", 1f, 0.5.toFloat())
            timer = System.currentTimeMillis() + 20000
        }
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || !inSkyblock || mc.ingameGUI == null) return
        if (timer - System.currentTimeMillis() > 0) {
            val time = (timer - System.currentTimeMillis()) / 1000f
            val sr = ScaledResolution(mc)
            val width = sr.scaledWidth / 2 - mc.fontRendererObj.getStringWidth("00.00") / 2
            renderText(
                text = String.format("%.2f", time),
                x = if (width < 10) width + mc.fontRendererObj.getStringWidth("0") else width,
                y = sr.scaledHeight / 2 + 10,
            )
        }
    }

    private fun renderText(text: String, x: Int, y: Int, scale: Double = 1.0, color: Int = 0xFFFFFF) {
        GlStateManager.pushMatrix()
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableBlend()
        GlStateManager.scale(scale, scale, scale)
        var yOffset = y - mc.fontRendererObj.FONT_HEIGHT
        text.split("\n").forEach {
            yOffset += (mc.fontRendererObj.FONT_HEIGHT * scale).toInt()
            mc.fontRendererObj.drawString(
                it,
                round(x / scale).toFloat(),
                round(yOffset / scale).toFloat(),
                color,
                true
            )
        }
        GlStateManager.popMatrix()
    }
}