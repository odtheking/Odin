package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.EntityEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(
            method = "setItemSlot",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSetItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self == null) return;
        if (new EntityEvent.SetItemSlot(self, equipmentSlot, itemStack).postAndCatch()) ci.cancel();
    }
}
