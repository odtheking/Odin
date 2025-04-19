package me.odin.mixin.mixins.entity;

import me.odinmain.utils.skyblock.ArrowTracker;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityArrow.class)
public class MixinEntityArrow {

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onArrowHit(CallbackInfo ci, MovingObjectPosition movingObjectPosition) {
        if (movingObjectPosition == null || movingObjectPosition.entityHit == null) return;
        ArrowTracker.onArrowHit((EntityArrow) (Object) this, movingObjectPosition.entityHit);
    }
}
