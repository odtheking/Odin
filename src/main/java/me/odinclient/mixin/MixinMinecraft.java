package me.odinclient.mixin;

import me.odinclient.events.impl.PostGuiOpenEvent;
import me.odinclient.events.impl.PreKeyInputEvent;
import me.odinclient.events.impl.PreMouseInputEvent;
import me.odinclient.features.impl.dungeon.CancelInteract;
import me.odinclient.features.impl.render.CPSDisplay;
import me.odinclient.features.impl.render.NoRender;
import me.odinclient.utils.skyblock.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Timer;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow public boolean skipRenderWorld;

    @Shadow private Timer timer;

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

    @Inject(method = "rightClickMouse", at = @At("HEAD"))
    private void rightClickMouse(CallbackInfo ci) {
        CPSDisplay.INSTANCE.onRightClick();
    }

    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void clickMouse(CallbackInfo ci) {
        CPSDisplay.INSTANCE.onLeftClick();
    }

    @Redirect(method = {"rightClickMouse"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;isAirBlock(Lnet/minecraft/util/BlockPos;)Z"))
    public boolean shouldCancelInteract(WorldClient instance, BlockPos blockPos) {
        return CancelInteract.INSTANCE.cancelInteractHook(instance, blockPos);
    }

    @Inject(method = "displayGuiScreen", at = @At("RETURN"))
    private void onDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci)
    {
        MinecraftForge.EVENT_BUS.post(new PostGuiOpenEvent());
    }

    @Inject(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;skipRenderWorld:Z"))
    private void onRunGameLoop(CallbackInfo ci)
    {
        if (this.skipRenderWorld) {
            NoRender.INSTANCE.drawGui();
            try {
                Thread.sleep((long) (50.0 / this.timer.timerSpeed));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}