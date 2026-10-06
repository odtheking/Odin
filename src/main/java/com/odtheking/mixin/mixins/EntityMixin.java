package com.odtheking.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.odtheking.odin.events.EntityEvent;
import com.odtheking.odin.features.impl.dungeon.Highlight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void onGetTeamColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity)(Object)this;

        Integer color = Highlight.getTeammateColor(self);
        if (color != null) cir.setReturnValue(color);
    }

    @WrapOperation(
            method = "moveOrInterpolateTo(Lnet/minecraft/world/entity/PositionPath;FFZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/InterpolationHandler;interpolateTo(Lnet/minecraft/world/entity/PositionPath;FFZ)Z")
    )
     private boolean onMoveOrInterpolateTo(InterpolationHandler instance, PositionPath position, float yRot, float xRot, boolean hasRotation, Operation<Boolean> original) {
        boolean result = original.call(instance, position, yRot, xRot, hasRotation);

        Entity entity = (Entity)(Object)this;
        Vec3 newPos = position != null ? position.endPosition() : Vec3.ZERO;
        new EntityEvent.Move(entity, newPos, yRot, xRot, entity.onGround()).postAndCatch();

        return result;
    }

    @Inject(method = "setOnGround", at = @At("TAIL"))
    private void onSetOnGround(boolean onGround, CallbackInfo ci) {
        Entity entity = (Entity)(Object)this;
        new EntityEvent.Move(entity, Vec3.ZERO, 0f, 0f, onGround).postAndCatch();
    }
}