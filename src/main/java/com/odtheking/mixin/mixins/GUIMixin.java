package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.ScreenCloseEvent;
import com.odtheking.odin.events.ScreenEvent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GUIMixin {

    @Inject(method = "setScreen", at = @At("TAIL"))
    private void setScreen(Screen screen, CallbackInfo ci) {
        if (screen != null) new ScreenEvent.Open(screen).postAndCatch();
        else ScreenCloseEvent.INSTANCE.postAndCatch();
    }
}