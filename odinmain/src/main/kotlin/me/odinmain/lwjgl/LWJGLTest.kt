package me.odinmain.lwjgl

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL2.NVG_ANTIALIAS
import org.lwjgl.nanovg.NanoVGGL2.nvgCreate
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11


class LWJGLTest : GuiScreen() {

    var vg: Long = 0

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        if (vg == 0L) {
            vg = nvgCreate(NVG_ANTIALIAS)
            if (vg == 0L) {
                throw RuntimeException("Failed to create nvg context")
            }
        }

        val fb = Minecraft.getMinecraft().framebuffer
        if (!fb.isStencilEnabled) {
            fb.enableStencil()
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)

        nvgBeginFrame(vg, Display.getWidth().toFloat(), Display.getHeight().toFloat(), 1f)

        nvgBeginPath(vg)
        nvgRect(vg, 100f, 100f, 120f, 30f)
        nvgCircle(vg, 120f, 120f, 5f)
        nvgPathWinding(vg, NVG_HOLE)
        val color = NVGColor.create()
        nvgRGBA(255.toByte(), 192.toByte(), 0.toByte(), 255.toByte(), color)
        nvgFillColor(vg, color)
        nvgFill(vg)

        nvgEndFrame(vg)

        GlStateManager.popAttrib()
    }

    var gui: GuiScreen = LWJGLTest()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (mc.thePlayer != null && mc.thePlayer != null) {
            Minecraft.getMinecraft().displayGuiScreen(gui)
        }
    }

}