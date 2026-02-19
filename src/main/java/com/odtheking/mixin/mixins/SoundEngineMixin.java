package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.PlaySoundEvent;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Inject(
            method = "play",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSoundEnginePlay(SoundInstance soundInstance, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (soundInstance == null || soundInstance.getSound() == null) return;
        Vec3 soundPos = new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
        if (new PlaySoundEvent(
                soundInstance.getSound(),
                soundInstance.getSource(),
                soundPos,
                soundInstance.getVolume(),
                soundInstance.getPitch()
        ).postAndCatch()) cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
    }
}
