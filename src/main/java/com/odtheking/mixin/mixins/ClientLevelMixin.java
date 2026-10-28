package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.ParticleEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "doAddParticle", at = @At("HEAD"), cancellable = true)
    private void onAddParticle(ParticleOptions particle, boolean overrideLimiter, boolean alwaysShowParticles, double x, double y, double z, double xd, double yd, double zd, CallbackInfo ci) {
        if (new ParticleEvent(particle, overrideLimiter, alwaysShowParticles, x, y, z, xd, yd, zd).postAndCatch()) ci.cancel();
    }
}