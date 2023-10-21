package me.odinclient.mixin;

import me.odinclient.features.impl.dungeon.SecretHitboxes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.block.BlockLever.FACING;

@Mixin(BlockLever.class)
public class MixinBlockLever extends Block {

    public MixinBlockLever(Material materialIn)
    {
        super(materialIn);
    }

    @Inject(method = "setBlockBoundsBasedOnState", at = @At("HEAD"), cancellable = true)
    private void onSetBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos, CallbackInfo ci)
    {
        if (SecretHitboxes.INSTANCE.getLever())
        {
            if (pos.getX() >= 58 && pos.getX() <= 62 && pos.getY() >= 133 && pos.getY() <= 136 && pos.getZ() == 142) { return; }
            SecretHitboxes.INSTANCE.getExpandedLevers().put(this, worldIn.getBlockState(pos).getValue(FACING));

            if (SecretHitboxes.INSTANCE.getEnabled())
            {
                this.setBlockBounds(0, 0, 0, 1, 1, 1);
                ci.cancel();
            }
        }
    }

}