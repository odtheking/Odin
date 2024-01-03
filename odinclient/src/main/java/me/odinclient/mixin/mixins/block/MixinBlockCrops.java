package me.odinclient.mixin.mixins.block;

import me.odinclient.features.impl.skyblock.FarmingHitboxes;
import net.minecraft.block.BlockCrops;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockCrops.class)
public abstract class MixinBlockCrops extends MixinBlock {

    @Override
    public void getSelectedBoundingBox(World worldIn, BlockPos pos, CallbackInfoReturnable<AxisAlignedBB> cir) {
        if (FarmingHitboxes.INSTANCE.getEnabled()) {
            FarmingHitboxes.INSTANCE.setFullBlock(worldIn.getBlockState(pos).getBlock());
        }
    }

    @Override
    public void collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end, CallbackInfoReturnable<MovingObjectPosition> cir) {
        if (FarmingHitboxes.INSTANCE.getEnabled()) {
            FarmingHitboxes.INSTANCE.setFullBlock(worldIn.getBlockState(pos).getBlock());
        }
    }

}
