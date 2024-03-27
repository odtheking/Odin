package me.odin.mixin.mixins;

import me.odinmain.features.impl.render.Animations;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityLivingBase.class, priority = 9999)
public abstract class MixinEntityLivingBase {

    @Shadow
    public abstract boolean isPotionActive(Potion potionIn);

    @Shadow public abstract PotionEffect getActivePotionEffect(Potion potionIn);

    @Inject(method = {"getArmSwingAnimationEnd()I"}, at = @At("HEAD"), cancellable = true)
    public void adjustSwingLength(CallbackInfoReturnable<Integer> cir) {
        if (!Animations.INSTANCE.getEnabled()) return;
        int length = Animations.INSTANCE.getIgnoreHaste() ? 6 : this.isPotionActive(Potion.digSpeed) ?
                6 - (1 + this.getActivePotionEffect(Potion.digSpeed).getAmplifier()) :
                (this.isPotionActive(Potion.digSlowdown) ?
                        6 + (1 + this.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 :
                        6);
        cir.setReturnValue(Math.max((int)(length* Math.exp(-Animations.INSTANCE.getSpeed())), 1));
    }

}
