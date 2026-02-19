package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.EntityEvent;
import com.odtheking.odin.features.impl.dungeon.Highlight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private Vec3 position;

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void onGetTeamColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity)(Object)this;

        Integer color = Highlight.getTeammateColor(self);
        if (color != null) cir.setReturnValue(color);
    }

    @Inject(
            method = "moveOrInterpolateTo(Ljava/util/Optional;Ljava/util/Optional;Ljava/util/Optional;)V",
            at = @At("HEAD")
    )
    private void onMoveOrInterpolateTo(Optional<Vec3> pos, Optional<Float> yRot, Optional<Float> xRot, CallbackInfo ci) {
        Entity entity = (Entity)(Object)this;
        InterpolationHandler interpolationHandler = entity.getInterpolation();
        if (interpolationHandler == null) {
            Vec3 newPos = pos.orElse(entity.position());
            Vec3 posDelta = newPos.subtract(this.position);

            Float newYRot = yRot.orElse(entity.getYRot());
            float yRotDelta = newYRot - entity.getYRot();

            Float newXRot = xRot.orElse(entity.getXRot());
            float xRotDelta = newXRot - entity.getXRot();

            if (posDelta == Vec3.ZERO && yRotDelta == 0.0f && xRotDelta == 0.0f) return;

            new EntityEvent.Move(entity, newPos, newYRot, newXRot, entity.onGround()).postAndCatch();
        }
    }

    @Inject(
            method = "setOnGround",
            at = @At("HEAD")
    )
    private void onSetOnGround(boolean onGround, CallbackInfo ci) {
        Entity entity = (Entity)(Object)this;
        new EntityEvent.Move(entity, Vec3.ZERO, 0f, 0f, onGround).postAndCatch();
    }

    @Inject(
            method = "handleEntityEvent",
            at = @At("HEAD")
    )
    private void onHandleEntityEvent(byte id, CallbackInfo ci) {
        Entity entity = (Entity)(Object)this;
        new EntityEvent.Event(entity, id).postAndCatch();
    }
}