package me.odinclient.mixin.mixins;

import me.odinclient.features.impl.dungeon.SecretHitboxes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.BlockSkull.FACING;

@Mixin(BlockSkull.class)
public class MixinBlockSkull extends Block {

    public MixinBlockSkull(Material materialIn)
    {
        super(materialIn);
    }

    @Inject(method = "setBlockBoundsBasedOnState", at = @At("HEAD"), cancellable = true)
    private void onSetBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos, CallbackInfo ci)
    {
        if (SecretHitboxes.INSTANCE.addEssence(pos))
        {
            SecretHitboxes.INSTANCE.getExpandedSkulls().put(this, worldIn.getBlockState(pos).getValue(FACING));

            if (SecretHitboxes.INSTANCE.getEnabled())
            {
                this.setBlockBounds(0, 0, 0, 1, 1, 1);
                ci.cancel();
            }
        }
    }


    @Inject(method = "getCollisionBoundingBox", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state, CallbackInfoReturnable<AxisAlignedBB> cir)
    {
        if (SecretHitboxes.INSTANCE.getEnabled() && SecretHitboxes.INSTANCE.getEssence())
        {
            switch (worldIn.getBlockState(pos).getValue(FACING)) {
                default: {
                    this.setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f);
                    break;
                }
                case NORTH: {
                    this.setBlockBounds(0.25f, 0.25f, 0.5f, 0.75f, 0.75f, 1.0f);
                    break;
                }
                case SOUTH: {
                    this.setBlockBounds(0.25f, 0.25f, 0.0f, 0.75f, 0.75f, 0.5f);
                    break;
                }
                case WEST: {
                    this.setBlockBounds(0.5f, 0.25f, 0.25f, 1.0f, 0.75f, 0.75f);
                    break;
                }
                case EAST: {
                    this.setBlockBounds(0.0f, 0.25f, 0.25f, 0.5f, 0.75f, 0.75f);
                }
            }

            AxisAlignedBB collisionBoundingBox = super.getCollisionBoundingBox(worldIn, pos, state);
            this.setBlockBounds(0, 0, 0, 1, 1, 1);
            cir.setReturnValue(collisionBoundingBox);
            cir.cancel();
        }
    }

}