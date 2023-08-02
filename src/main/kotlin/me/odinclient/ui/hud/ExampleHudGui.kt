package me.odinclient.ui.hud

import me.odinclient.features.ModuleManager
import me.odinclient.ui.waypoint.WaypointGUI
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import java.io.IOException
import kotlin.math.sign

object ExampleHudGui : GuiScreen() {
    private var exampleHuds = listOf<ExampleHud>()

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        GlStateManager.pushMatrix()
        if (mc.gameSettings.guiScale == 0) // if you use auto you are a psychopath
            GlStateManager.scale(.5f, .5f, 1f)
        else
            GlStateManager.scale(2 / mc.gameSettings.guiScale.toFloat(), 2 / mc.gameSettings.guiScale.toFloat(), 1f)
        exampleHuds.forEach { it.draw() }
        GlStateManager.popMatrix()
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()
        if (Mouse.getEventDWheel() != 0) {
            exampleHuds.forEach { if (it.handleScroll(Mouse.getEventDWheel().sign * 4)) return }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        exampleHuds.forEach { if(it.mouseClicked(mouseButton)) return }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        exampleHuds.forEach { it.mouseReleased(state) }

        super.mouseReleased(mouseX, mouseY, state)
    }



    override fun initGui() {
        exampleHuds = ModuleManager.huds.map { ExampleHud(it.first, it.second) }
    }

    override fun onGuiClosed() {
        exampleHuds.forEach { it.onClose() }
    }

    override fun doesGuiPauseGame(): Boolean = false
}