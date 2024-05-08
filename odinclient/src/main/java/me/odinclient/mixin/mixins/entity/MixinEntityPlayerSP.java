package me.odinclient.mixin.mixins.entity;

import gg.essential.lib.mixinextras.injector.ModifyExpressionValue;
import me.odinclient.features.impl.render.NoDebuff;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EntityPlayerSP.class)
abstract public class MixinEntityPlayerSP {
    @ModifyExpressionValue(method = {"pushOutOfBlocks"}, at = @At(value = "FIELD", target =  "Lnet/minecraft/client/entity/EntityPlayerSP;noClip:Z"))
    public boolean shouldPrevent(boolean original, EntityPlayerSP instance) {
        return NoDebuff.INSTANCE.isNoPush() || original;
    }
}
