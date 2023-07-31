package me.odinclient.mixin;

import me.odinclient.features.impl.dungeon.SecretHitboxes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
                this.setBlockBounds(0, 0, 0, 1, 1, 1);
                ci.cancel();
            }
        }
    }

}
