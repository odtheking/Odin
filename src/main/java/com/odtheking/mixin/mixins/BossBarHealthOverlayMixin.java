package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.RenderBossBarEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossBarHealthOverlayMixin {

    @Inject(method = "extractBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/BossEvent;)V", at = @At("HEAD"), cancellable = true)
    private void onRenderBossBar(GuiGraphicsExtractor graphics, int x, int y, BossEvent event, CallbackInfo ci) {
        if (new RenderBossBarEvent(event).postAndCatch()) ci.cancel();
    }
}