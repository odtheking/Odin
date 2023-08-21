package me.odinclient.mixin;

import me.odinclient.features.impl.dungeon.SecretHitboxes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockButton.class)
public class MixinBlockButton extends Block {

    public MixinBlockButton(Material materialIn)
    {
        super(materialIn);
    }

    @Inject(method = "updateBlockBounds", at = @At("HEAD"), cancellable = true)
    private void onUpdateBlockBounds(IBlockState state, CallbackInfo ci)
    {
        if (SecretHitboxes.INSTANCE.getButton())
        {
            SecretHitboxes.INSTANCE.getExpandedButtons().put(this, state);

            if (SecretHitboxes.INSTANCE.getEnabled())
            {
                EnumFacing enumfacing = state.getValue(BlockButton.FACING);
                boolean flag = state.getValue(BlockButton.POWERED);
                float f2 = (flag ? 1 : 2) / 16.0f;

                switch (enumfacing) {
                    case EAST:
                        this.setBlockBounds(0.0f, 0.0f, 0.0f, f2, 1.0f, 1.0f);
                        break;

                    case WEST:
                        this.setBlockBounds(1.0f - f2, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
                        break;

                    case SOUTH:
                        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, f2);
                        break;

                    case NORTH:
                        this.setBlockBounds(0.0f, 0.0f, 1.0f - f2, 1.0f, 1.0f, 1.0f);
                        break;

                    case UP:
                        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 0.0f + f2, 1.0f);
                        break;

                    case DOWN:
                        this.setBlockBounds(0.0f, 1.0f - f2, 0.0f, 1.0f, 1.0f, 1.0f);
                        break;
                }
                ci.cancel();
            }
        }
    }

}
