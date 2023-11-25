package me.odinclient.mixin.mixins;

import me.odinclient.features.impl.skyblock.FarmingHitboxes;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.block.BlockCocoa.AGE;

@Mixin(BlockCocoa.class)
public abstract class MixinBlockCocoa extends BlockDirectional {

    protected MixinBlockCocoa(Material material) {
        super(material);
    }

    /**
     * @author Cezar
     * @reason Full crop hitboxes
     */
    @Overwrite
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        EnumFacing enumfacing = iblockstate.getValue(FACING);
        int i = iblockstate.getValue(AGE);
        int j = 4 + i * 2;
        int k = 5 + i * 2;
        float f = (float)j / 2.0f;
        switch (enumfacing) {
            case SOUTH: {
                this.setBlockBounds((8.0f - f) / 16.0f, (12.0f - (float)k) / 16.0f, (15.0f - (float)j) / 16.0f, (8.0f + f) / 16.0f, 0.75f, 0.9375f);
                break;
            }
            case NORTH: {
                this.setBlockBounds((8.0f - f) / 16.0f, (12.0f - (float)k) / 16.0f, 0.0625f, (8.0f + f) / 16.0f, 0.75f, (1.0f + (float)j) / 16.0f);
                break;
            }
            case WEST: {
                this.setBlockBounds(0.0625f, (12.0f - (float)k) / 16.0f, (8.0f - f) / 16.0f, (1.0f + (float)j) / 16.0f, 0.75f, (8.0f + f) / 16.0f);
                break;
            }
            case EAST: {
                this.setBlockBounds((15.0f - (float)j) / 16.0f, (12.0f - (float)k) / 16.0f, (8.0f - f) / 16.0f, 0.9375f, 0.75f, (8.0f + f) / 16.0f);
            }
        }
        AxisAlignedBB collisionBoundingBox = new AxisAlignedBB(pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getZ() + maxX, pos.getY() + maxY, pos.getZ() + maxZ);
        this.setBlockBoundsBasedOnState(worldIn, pos);
        return collisionBoundingBox;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
        if (FarmingHitboxes.INSTANCE.getEnabled()){
            FarmingHitboxes.INSTANCE.setFullBlock(worldIn.getBlockState(pos).getBlock());
        }
        return super.collisionRayTrace(worldIn, pos, start, end);
    }

    @Inject(method = "setBlockBoundsBasedOnState", at = @At("HEAD"), cancellable = true)
    private void modifyBlockHitbox(IBlockAccess worldIn, BlockPos pos, CallbackInfo ci) {
        if (FarmingHitboxes.INSTANCE.getEnabled()) {
            worldIn.getBlockState(pos).getBlock().setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f);
            ci.cancel();
        }
    }

}