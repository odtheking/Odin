package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.EntityEvent;
import com.odtheking.odin.events.GameTimeUpdateEvent;
import com.odtheking.odin.events.ParticleAddEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(
            method = "addEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddEntity(Entity entity, CallbackInfo ci) {
        if (new EntityEvent.Add(entity).postAndCatch()) ci.cancel();
    }

    @Inject(
            method = "doAddParticle",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddParticle(ParticleOptions particleOptions, boolean overrideDelimiter, boolean alwaysShow, double d, double e, double f, double g, double h, double i, CallbackInfo ci) {
        Vec3 pos = new Vec3(d, e, f);
        Vec3 delta = new Vec3(g, h, i);
        if (new ParticleAddEvent(particleOptions.getType(), overrideDelimiter, alwaysShow, pos, delta).postAndCatch()) ci.cancel();
    }

    @Inject(
            method = "setTimeFromServer",
            at = @At("HEAD")
    )
    private void onSetTimeFromServer(long l, long m, boolean bl, CallbackInfo ci) {
        new GameTimeUpdateEvent(l, m, bl).postAndCatch();
    }
}
