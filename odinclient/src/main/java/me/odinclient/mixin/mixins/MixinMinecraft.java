package me.odinclient.mixin.mixins;

import me.odinclient.features.impl.skyblock.CancelInteract;
import me.odinmain.events.impl.ClickEvent;
import me.odinmain.events.impl.InputEvent;
import me.odinmain.features.impl.render.CPSDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.odinmain.utils.Utils.postAndCatch;

@Mixin(value = {Minecraft.class}, priority = 800)
public class MixinMinecraft {

    @Shadow public EntityPlayerSP thePlayer;

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V")})
    public void keyPresses(CallbackInfo ci) {
        if (Keyboard.getEventKeyState()) postAndCatch(new InputEvent.Keyboard((Keyboard.getEventKey() == 0) ? (Keyboard.getEventCharacter() + 256) : Keyboard.getEventKey()));
    }

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventButton()I", remap = false)})
    public void mouseKeyPresses(CallbackInfo ci) {
        if (Mouse.getEventButtonState()) postAndCatch(new InputEvent.Mouse(Mouse.getEventButton()));
    }

    @Inject(method = "rightClickMouse", at = @At("HEAD"), cancellable = true)
    private void rightClickMouse(CallbackInfo ci) {
        if (postAndCatch(new ClickEvent.Right())) ci.cancel();
        CPSDisplay.onRightClick();
    }

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    private void clickMouse(CallbackInfo ci) {
        if (postAndCatch(new ClickEvent.Left())) ci.cancel();
        CPSDisplay.onLeftClick();
    }

    @Redirect(method = {"rightClickMouse"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;isAirBlock(Lnet/minecraft/util/BlockPos;)Z"))
    public boolean shouldCancelInteract(WorldClient instance, BlockPos blockPos) {
        return CancelInteract.cancelInteractHook(instance, blockPos);
    }
}