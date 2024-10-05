package me.odin.mixin.mixins;

import me.odinmain.events.impl.ClickEvent;
import me.odinmain.events.impl.PreKeyInputEvent;
import me.odinmain.events.impl.PreMouseInputEvent;
import me.odinmain.features.impl.render.CPSDisplay;
import me.odinmain.utils.skyblock.PlayerUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.odinmain.utils.Utils.postAndCatch;

@Mixin(value = {Minecraft.class}, priority = 800)
public class MixinMinecraft {

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V")})
    public void keyPresses(CallbackInfo ci) {
        int k = (Keyboard.getEventKey() == 0) ? (Keyboard.getEventCharacter() + 256) : Keyboard.getEventKey();
        if (Keyboard.getEventKeyState()) {
            postAndCatch(new PreKeyInputEvent(k));
        }
    }

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false)})
    public void mouseKeyPresses(CallbackInfo ci) {
        if (Mouse.getEventButtonState()) {
            postAndCatch(new PreMouseInputEvent(Mouse.getEventButton()));
        }
    }

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleInput()V")})
    private void handleInput(CallbackInfo ci) {
        PlayerUtils.INSTANCE.handleWindowClickQueue();
    }

    @Inject(method = "rightClickMouse", at = @At("HEAD"), cancellable = true)
    private void rightClickMouse(CallbackInfo ci) {
        if (postAndCatch(new ClickEvent.RightClickEvent())) ci.cancel();
        CPSDisplay.INSTANCE.onRightClick();
    }

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    private void clickMouse(CallbackInfo ci) {
        if (postAndCatch(new ClickEvent.LeftClickEvent())) ci.cancel();
        CPSDisplay.INSTANCE.onLeftClick();
    }
}