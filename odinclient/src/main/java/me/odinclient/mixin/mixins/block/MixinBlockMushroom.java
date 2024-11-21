package me.odinclient.mixin.mixins.block;

import me.odinclient.features.impl.skyblock.FarmingHitboxes;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockMushroom;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockMushroom.class)
public class MixinBlockMushroom extends BlockBush {

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        if (FarmingHitboxes.INSTANCE.getEnabled()) FarmingHitboxes.INSTANCE.setFullBlock(worldIn.getBlockState(pos).getBlock());
        return super.getSelectedBoundingBox(worldIn, pos);
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
        if (FarmingHitboxes.INSTANCE.getEnabled()) FarmingHitboxes.INSTANCE.setFullBlock(worldIn.getBlockState(pos).getBlock());
        return super.collisionRayTrace(worldIn, pos, start, end);
    }
}
