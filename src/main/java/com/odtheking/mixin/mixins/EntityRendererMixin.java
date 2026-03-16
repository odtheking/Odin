package com.odtheking.mixin.mixins;

import com.odtheking.odin.features.impl.render.HidePlayers;
import com.odtheking.odin.features.impl.render.RenderOptimizer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onRender(T entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
        if (!HidePlayers.shouldRenderPlayer(entity)) cir.setReturnValue(false);

        if (RenderOptimizer.hideEntityDeathAnimation() && entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying())
            cir.setReturnValue(false);

        if (RenderOptimizer.hideDyingEntityArmorStand() && entity instanceof ArmorStand armorStand) {
            Entity self = armorStand.level().getEntity(armorStand.getId() - 1);
            if (self instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) cir.setReturnValue(false);
        }
    }
}

