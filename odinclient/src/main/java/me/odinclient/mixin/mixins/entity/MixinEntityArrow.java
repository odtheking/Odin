package me.odinclient.mixin.mixins.entity;

import me.odinmain.events.impl.ArrowEvent;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static me.odinmain.utils.Utils.postAndCatch;

@Mixin(EntityArrow.class)
public class MixinEntityArrow {

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onArrowHit(CallbackInfo ci, MovingObjectPosition movingObjectPosition) {
        if (movingObjectPosition == null || movingObjectPosition.entityHit == null) return;
        postAndCatch(new ArrowEvent.Hit((EntityArrow) (Object) this, movingObjectPosition.entityHit));
    }
}
