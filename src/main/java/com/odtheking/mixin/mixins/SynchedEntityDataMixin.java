package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.EntityEvent;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SynchedEntityData.class)
public class SynchedEntityDataMixin {
    @Final
    @Shadow
    private SyncedDataHolder entity;

    @Inject(
            method = "assignValues",
            at = @At("TAIL")
    )
    private void onAssignValues(List<SynchedEntityData.DataValue<?>> list, CallbackInfo ci) {
        Entity self = (Entity) this.entity;
        if (self == null) return;
        new EntityEvent.SetData(self, list).postAndCatch();
    }
}
