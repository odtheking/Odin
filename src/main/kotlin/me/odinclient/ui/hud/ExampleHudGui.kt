package me.odinclient.ui.hud

import me.odinclient.features.ModuleManager
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager

object ExampleHudGui : GuiScreen() {
    private var exampleHuds = arrayListOf<ExampleHud>()

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        GlStateManager.pushMatrix()
        if (mc.gameSettings.guiScale == 0) // if you use auto you are a psychopath
            GlStateManager.scale(.5f, .5f, 1f)
        else
            GlStateManager.scale(2 / mc.gameSettings.guiScale.toFloat(), 2 / mc.gameSettings.guiScale.toFloat(), 1f)
        exampleHuds.forEach { it.draw() }
        GlStateManager.popMatrix()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        exampleHuds.forEach { it.mouseClicked(mouseButton) }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        exampleHuds.forEach { it.mouseReleased(state) }

        super.mouseReleased(mouseX, mouseY, state)
    }



    override fun initGui() {
        exampleHuds.addAll(ModuleManager.huds.map { ExampleHud(it.first) })
    }

    override fun onGuiClosed() {
        exampleHuds.forEach { it.onClose() }
    }

    override fun doesGuiPauseGame(): Boolean = false
}