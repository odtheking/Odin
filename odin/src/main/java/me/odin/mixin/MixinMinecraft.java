package me.odin.mixin;

import me.odinmain.events.impl.PostGuiOpenEvent;
import me.odinmain.events.impl.PreKeyInputEvent;
import me.odinmain.events.impl.PreMouseInputEvent;
import me.odinmain.features.impl.render.Animations;
import me.odinmain.features.impl.render.CPSDisplay;
import me.odinmain.features.impl.render.RenderOptimizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {Minecraft.class}, priority = 800)
public class MixinMinecraft {

    @Shadow public boolean skipRenderWorld;

    @Shadow public EntityPlayerSP thePlayer;

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V")})
    public void keyPresses(CallbackInfo ci) {
        int k = (Keyboard.getEventKey() == 0) ? (Keyboard.getEventCharacter() + 256) : Keyboard.getEventKey();
        if (Keyboard.getEventKeyState()) {
            MinecraftForge.EVENT_BUS.post(new PreKeyInputEvent(k));
        }
    }

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false)})
    public void mouseKeyPresses(CallbackInfo ci) {
        int k = Mouse.getEventButton();
        if (Mouse.getEventButtonState()) {
            MinecraftForge.EVENT_BUS.post(new PreMouseInputEvent(k));
        }
    }

    @Inject(method = "rightClickMouse", at = @At("HEAD"))
    private void rightClickMouse(CallbackInfo ci) {
        CPSDisplay.INSTANCE.onRightClick();
    }

    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void clickMouse(CallbackInfo ci) {
        CPSDisplay.INSTANCE.onLeftClick();
    }

    @Inject(method = { "runGameLoop" }, at = { @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;skipRenderWorld:Z") })
    public void skipRenderWorld(final CallbackInfo ci) {
        if (this.skipRenderWorld) {
            RenderOptimizer.INSTANCE.drawGui();
            try {
                Thread.sleep((long)(50.0f / ((MinecraftAccessor) this).getTimer().timerSpeed));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "displayGuiScreen", at = @At("RETURN"))
    private void onDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PostGuiOpenEvent());
    }

    @Inject(method = "sendClickBlockToController", at = @At("HEAD"))
    private void onSendClickBlockToController(boolean leftClick, CallbackInfo ci) {
        if (Animations.INSTANCE.getBlockHit() && this.thePlayer.isUsingItem() && leftClick) Animations.INSTANCE.swingItemHook();
    }

}