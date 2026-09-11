package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.SetSlotEvent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Inject(method = "setItem", at = @At("TAIL"))
    private void postSetSlot(int slot, int stateId, ItemStack itemStack, CallbackInfo ci) {
        new SetSlotEvent(slot, itemStack, slots, (AbstractContainerMenu) (Object) this).postAndCatch();
    }
}