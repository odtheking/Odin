package me.odinclient.mixin;

import me.odinclient.features.impl.dungeon.CancelInteract;
import me.odinclient.features.impl.dungeon.SecretTriggerbot;
import me.odinclient.utils.skyblock.PlayerUtils;
import me.odinmain.events.impl.ClickEvent;
import me.odinmain.events.impl.PostGuiOpenEvent;
import me.odinmain.events.impl.PreKeyInputEvent;
import me.odinmain.events.impl.PreMouseInputEvent;
import me.odinmain.features.impl.render.Animations;
import me.odinmain.features.impl.render.CPSDisplay;
import me.odinmain.features.impl.render.RenderOptimizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Inject(method = {"runTick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleInput()V")})
    private void handleInput(CallbackInfo ci) {
        PlayerUtils.INSTANCE.handleWindowClickQueue();
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isPressed()Z", ordinal = 11))
    private void preRightClicks(CallbackInfo ci) {
        SecretTriggerbot.INSTANCE.tryTriggerbot();
    }

    @Inject(method = "rightClickMouse", at = @At("HEAD"), cancellable = true)
    private void rightClickMouse(CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new ClickEvent.RightClickEvent())) {
            ci.cancel();
            CPSDisplay.INSTANCE.onRightClick();
            return;
        }
        /*
        Taken from Sk1erLLC's OldAnimations Mod
        */
        if (Animations.INSTANCE.getBlockHit() &&
                Minecraft.getMinecraft().playerController.getIsHittingBlock() &&
                Minecraft.getMinecraft().thePlayer.getHeldItem() != null &&
                (Minecraft.getMinecraft().thePlayer.getHeldItem().getItemUseAction() != EnumAction.NONE ||
                Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() instanceof ItemBlock)) {
            Minecraft.getMinecraft().playerController.resetBlockRemoving();
        }
    }

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    private void clickMouse(CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new ClickEvent.LeftClickEvent())) ci.cancel();
        CPSDisplay.INSTANCE.onLeftClick();
    }

    @Inject(method = "displayGuiScreen", at = @At("RETURN"))
    private void onDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PostGuiOpenEvent());
    }

    @Redirect(method = {"rightClickMouse"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;isAirBlock(Lnet/minecraft/util/BlockPos;)Z"))
    public boolean shouldCancelInteract(WorldClient instance, BlockPos blockPos) {
        return CancelInteract.INSTANCE.cancelInteractHook(instance, blockPos);
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

}