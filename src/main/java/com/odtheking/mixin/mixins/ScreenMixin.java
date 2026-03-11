package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.ScreenEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "renderWithTooltipAndSubtitles", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (new ScreenEvent.Render((Screen) (Object) this, context, mouseX, mouseY).postAndCatch()) ci.cancel();
    }
}