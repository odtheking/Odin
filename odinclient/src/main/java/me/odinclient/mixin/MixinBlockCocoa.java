package me.odinclient.mixin;

import me.odinclient.features.impl.skyblock.FarmingHitboxes;
import net.minecraft.block.BlockCocoa;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockCocoa.class)
public class MixinBlockCocoa {

    @Inject(method = "setBlockBoundsBasedOnState", at = @At("HEAD"), cancellable = true)
    private void onSetBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos, CallbackInfo ci)
    {
        if (FarmingHitboxes.INSTANCE.setBlockBoundsMixin(worldIn.getBlockState(pos).getBlock())) ci.cancel();
    }

}