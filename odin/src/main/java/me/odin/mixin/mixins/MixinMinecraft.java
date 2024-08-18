package me.odin.mixin.mixins;

import me.odinmain.events.impl.ClickEvent;
import me.odinmain.events.impl.PreKeyInputEvent;
import me.odinmain.events.impl.PreMouseInputEvent;
import me.odinmain.features.impl.render.Animations;
import me.odinmain.features.impl.render.CPSDisplay;
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
        int k = Mouse.getEventButton();
        if (Mouse.getEventButtonState()) {
            postAndCatch(new PreMouseInputEvent(k));
        }
    }

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleInput()V")})
    private void handleInput(CallbackInfo ci) {
        PlayerUtils.INSTANCE.handleWindowClickQueue();
    }

    @Inject(method = "rightClickMouse", at = @At("HEAD"), cancellable = true)
    private void rightClickMouse(CallbackInfo ci) {
        if (postAndCatch(new ClickEvent.Right())) ci.cancel();
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
        if (postAndCatch(new ClickEvent.Left())) ci.cancel();
        CPSDisplay.INSTANCE.onLeftClick();
    }
}