package me.odinclient.mixin.mixins;

import me.odinmain.features.impl.render.RandomPlayers;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {

    @Inject(method = "preRenderCallback(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V", at = @At("TAIL"))
    private void onPreRenderCallback(AbstractClientPlayer entitylivingbaseIn, float partialTickTime, CallbackInfo ci) {
        RandomPlayers.preRenderCallbackScaleHook(entitylivingbaseIn);
    }
}
