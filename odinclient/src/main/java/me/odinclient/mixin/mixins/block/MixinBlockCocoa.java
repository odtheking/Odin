package me.odinclient.mixin.mixins.block;

import me.odinclient.features.impl.skyblock.FarmingHitboxes;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.BlockCocoa.AGE;

@Mixin(BlockCocoa.class)
public abstract class MixinBlockCocoa extends BlockDirectional {

    protected MixinBlockCocoa(Material material) {
        super(material);
    }

    @Inject(method = "getCollisionBoundingBox", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state, CallbackInfoReturnable<AxisAlignedBB> cir) {
        if (FarmingHitboxes.INSTANCE.getEnabled()) {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            EnumFacing enumfacing = iblockstate.getValue(FACING);
            int i = iblockstate.getValue(AGE);
            int j = 4 + i * 2;
            int k = 5 + i * 2;
            float f = (float)j / 2.0F;
            switch (enumfacing) {
                case SOUTH:
                    this.setBlockBounds((8.0F - f) / 16.0F, (12.0F - (float)k) / 16.0F, (15.0F - (float)j) / 16.0F, (8.0F + f) / 16.0F, 0.75F, 0.9375F);
                    break;
                case NORTH:
                    this.setBlockBounds((8.0F - f) / 16.0F, (12.0F - (float)k) / 16.0F, 0.0625F, (8.0F + f) / 16.0F, 0.75F, (1.0F + (float)j) / 16.0F);
                    break;
                case WEST:
                    this.setBlockBounds(0.0625F, (12.0F - (float)k) / 16.0F, (8.0F - f) / 16.0F, (1.0F + (float)j) / 16.0F, 0.75F, (8.0F + f) / 16.0F);
                    break;
                case EAST:
                    this.setBlockBounds((15.0F - (float)j) / 16.0F, (12.0F - (float)k) / 16.0F, (8.0F - f) / 16.0F, 0.9375F, 0.75F, (8.0F + f) / 16.0F);
            }

            AxisAlignedBB collisionBoundingBox = super.getCollisionBoundingBox(worldIn, pos, state);
            this.setBlockBounds(0, 0, 0, 1, 1, 1);
            FarmingHitboxes.INSTANCE.setFullBlock(this);
            cir.setReturnValue(collisionBoundingBox);
            cir.cancel();
        }
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
        if (FarmingHitboxes.INSTANCE.getEnabled()) FarmingHitboxes.INSTANCE.setFullBlock(worldIn.getBlockState(pos).getBlock());
        return super.collisionRayTrace(worldIn, pos, start, end);
    }

    @Inject(method = "setBlockBoundsBasedOnState", at = @At("HEAD"), cancellable = true)
    private void modifyBlockHitbox(IBlockAccess worldIn, BlockPos pos, CallbackInfo ci) {
        if (FarmingHitboxes.INSTANCE.getEnabled()) {
            FarmingHitboxes.INSTANCE.setFullBlock(worldIn.getBlockState(pos).getBlock());
            ci.cancel();
        }
    }
}