package me.odinmain.lwjgl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL2.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL2.nvgCreate;

public class LWJGLTest extends GuiScreen {

    private static long vg = 0;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (vg == 0) {
            vg = nvgCreate(NVG_ANTIALIAS);
            if (vg == 0) {
                throw new RuntimeException("Failed to create nvg context");
            }
        }

        Framebuffer fb = Minecraft.getMinecraft().getFramebuffer();
        if (!fb.isStencilEnabled()) {
            fb.enableStencil();
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        nvgBeginFrame(vg, Display.getWidth(), Display.getHeight(), 1);

        nvgBeginPath(vg);
        nvgRect(vg, 100,100, 120,30);
        nvgCircle(vg, 120,120, 5);
        nvgPathWinding(vg, NVG_HOLE);
        NVGColor color = NVGColor.create();
        nvgRGBA((byte) 255, (byte) 192, (byte) 0, (byte) 255, color);
        nvgFillColor(vg, color);
        nvgFill(vg);

        nvgEndFrame(vg);

        GlStateManager.popAttrib();
    }

}
