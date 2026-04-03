package com.odtheking.mixin.mixins;

import com.odtheking.odin.features.impl.skyblock.NoItemPlace;
import com.odtheking.odin.utils.skyblock.LocationUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.PlayerHeadBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void cancelPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!NoItemPlace.INSTANCE.getEnabled()) return;
        if (!LocationUtils.INSTANCE.isInSkyblock()) return;
        if (((BlockItem) (Object) this).getBlock() instanceof PlayerHeadBlock) return;
        cir.setReturnValue(InteractionResult.FAIL);
    }
}
