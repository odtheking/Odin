package me.odinclient.mixin.mixins.block;

import me.odinclient.features.impl.render.NoCarpet;
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
        if (NoCarpet.INSTANCE.noCarpetHook((BlockCarpet) this.blockState.getBlock())) ci.cancel();
    }

}