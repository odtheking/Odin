package com.odtheking.mixin.mixins;

import com.odtheking.odin.features.impl.skyblock.OverlayType;
import com.odtheking.odin.features.impl.skyblock.PlayerDisplay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceBarRenderer.class)
public class ExperienceBarRendererMixin {

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void cancelXPBar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (PlayerDisplay.shouldCancelOverlay(OverlayType.XP)) ci.cancel();
    }
}
