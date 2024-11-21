package me.odin.mixin.mixins;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.misc.Funny")
public class MixinSkytilsFunny {
    @Dynamic
    @Inject(method = "joinedSkyblock", at = @At("HEAD"), cancellable = true)
    private void onJoinedSkyblockTroll(CallbackInfo ci) {
        ci.cancel();
    }
}