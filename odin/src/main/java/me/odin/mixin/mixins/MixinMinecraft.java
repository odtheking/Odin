package me.odin.mixin.mixins;

import me.odinmain.events.impl.*;
import me.odinmain.features.impl.render.Animations;
import me.odinmain.features.impl.render.CPSDisplay;
import me.odinmain.utils.EventExtensions;
import me.odinmain.utils.skyblock.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {Minecraft.class}, priority = 800)
public class MixinMinecraft {

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V")})
    public void keyPresses(CallbackInfo ci) {
        int k = (Keyboard.getEventKey() == 0) ? (Keyboard.getEventCharacter() + 256) : Keyboard.getEventKey();
        if (Keyboard.getEventKeyState()) {
            EventExtensions.postAndCatch(new PreKeyInputEvent(k));
        }
    }

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false)})
    public void mouseKeyPresses(CallbackInfo ci) {
        if (Mouse.getEventButtonState()) {
            EventExtensions.postAndCatch(new PreMouseInputEvent(Mouse.getEventButton()));
        }
    }

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleInput()V")})
    private void handleInput(CallbackInfo ci) {
        PlayerUtils.INSTANCE.handleWindowClickQueue();
    }

    @Inject(method = "rightClickMouse", at = @At("HEAD"), cancellable = true)
    private void rightClickMouse(CallbackInfo ci) {
        if (EventExtensions.postAndCatch(new ClickEvent.RightClickEvent())) ci.cancel();
        CPSDisplay.INSTANCE.onRightClick();
        /*
        Taken from [Sk1erLLC's OldAnimations Mod](https://github.com/Sk1erLLC/OldAnimations) to enable block hitting
        */
        if (Animations.INSTANCE.getBlockHit() && Minecraft.getMinecraft().playerController.getIsHittingBlock() &&
                Minecraft.getMinecraft().thePlayer.getHeldItem() != null &&
                (Minecraft.getMinecraft().thePlayer.getHeldItem().getItemUseAction() != EnumAction.NONE ||
                        Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() instanceof ItemBlock)) {
            Minecraft.getMinecraft().playerController.resetBlockRemoving();
        }
    }

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    private void clickMouse(CallbackInfo ci) {
        if (EventExtensions.postAndCatch(new ClickEvent.LeftClickEvent())) ci.cancel();
        CPSDisplay.INSTANCE.onLeftClick();
    }
}