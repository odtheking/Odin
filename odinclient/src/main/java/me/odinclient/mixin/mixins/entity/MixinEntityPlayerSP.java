package me.odinclient.mixin.mixins.entity;

import me.odinclient.features.impl.render.NoDebuff;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntityPlayerSP.class)
abstract public class MixinEntityPlayerSP {
    @Redirect(method = {"pushOutOfBlocks"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;noClip:Z"))
    public boolean shouldPrevent(EntityPlayerSP instance) {
        return NoDebuff.INSTANCE.isNoPush();
    }
}
