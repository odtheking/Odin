package me.odinclient.features.impl.general

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CPSDisplay : Module(
    "CPS Display",
    category = Category.GENERAL
) {
    private val leftClicks = mutableListOf<Long>()
    private val rightClicks = mutableListOf<Long>()

    fun onLeftClick() {
        leftClicks.add(System.currentTimeMillis())
    }

    fun onRightClick() {
        rightClicks.add(System.currentTimeMillis())
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Text) {
        leftClicks.removeAll { System.currentTimeMillis() - it > 1000 }
        rightClicks.removeAll { System.currentTimeMillis() - it > 1000 }

        GlStateManager.pushMatrix()
        mc.fontRendererObj.drawStringWithShadow("${leftClicks.size} - ${rightClicks.size} CPS", 2f, 2f, 0xffffff)
        GlStateManager.popMatrix()
    }
}