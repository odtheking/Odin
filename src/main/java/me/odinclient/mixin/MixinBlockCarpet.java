package me.odinclient.mixin;

import me.odinclient.features.impl.general.NoCarpet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockCarpet.class)
public class MixinBlockCarpet extends Block {

    public MixinBlockCarpet(Material materialIn)
    {
        super(materialIn);
    }

    @Inject(method = "setBlockBoundsFromMeta", at = @At("HEAD"), cancellable = true)
    private void onSetBlockBoundsFromMeta(int meta, CallbackInfo ci)
    {
        NoCarpet.INSTANCE.getCarpetList().add((BlockCarpet)this.blockState.getBlock());
        if (NoCarpet.INSTANCE.getEnabled())
        {
            this.setBlockBounds(0f, 0f, 0f, 1f, 0f, 1f);
            ci.cancel();
        }
    }

}
