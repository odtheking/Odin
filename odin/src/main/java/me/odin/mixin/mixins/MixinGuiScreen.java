package me.odin.mixin.mixins;

import me.odinmain.events.impl.GuiEvent;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.odinmain.utils.Utils.postAndCatch;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Unique
    private final GuiScreen odin$gui = (GuiScreen) (Object) this;

    @Inject(method = "handleMouseInput", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/GuiScreen.mouseClicked(III)V"), cancellable = true)
    private void onMouseInput(CallbackInfo ci){
        if (Mouse.getEventButtonState())
            if (postAndCatch(new GuiEvent.MouseClick(odin$gui, Mouse.getEventButton(), Mouse.getEventX(), Mouse.getEventY()))) ci.cancel();
    }

    @Inject(method = "handleMouseInput", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/GuiScreen.mouseReleased(III)V"), cancellable = true)
    private void onReleaseMouseInput(CallbackInfo ci){
        if (!Mouse.getEventButtonState())
            if (postAndCatch(new GuiEvent.GuiMouseReleaseEvent(odin$gui, Mouse.getEventButton(), Mouse.getEventX(), Mouse.getEventY()))) ci.cancel();
    }

    @Inject(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;keyTyped(CI)V"), cancellable = true)
    private void onHandleKeyboardInput(CallbackInfo ci) {
        if (Keyboard.getEventKeyState())
            if (postAndCatch(new GuiEvent.KeyPress(odin$gui, Keyboard.getEventKey(), Keyboard.getEventCharacter()))) ci.cancel();
    }
}

