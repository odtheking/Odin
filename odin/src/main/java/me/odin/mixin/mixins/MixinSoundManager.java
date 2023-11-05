package me.odin.mixin.mixins;

import me.odinmain.utils.skyblock.PlayerUtils;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {SoundManager.class}, priority = 900)
public class MixinSoundManager {

    @Inject(method = "getNormalizedVolume", at = @At("HEAD"), cancellable = true)
    private void bypassPlayerVolume(ISound sound, SoundPoolEntry entry, SoundCategory category, CallbackInfoReturnable<Float> cir) {
        if (PlayerUtils.INSTANCE.getShouldBypassVolume()) cir.setReturnValue(MathHelper.clamp_float(sound.getVolume(), 0f, 1f));
    }

}