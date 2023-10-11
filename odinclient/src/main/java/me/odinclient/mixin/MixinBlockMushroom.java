package me.odinclient.mixin;

import me.odinclient.features.impl.skyblock.FarmingHitboxes;
import net.minecraft.block.BlockMushroom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockMushroom.class)
public class MixinBlockMushroom {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockMushroom;setBlockBounds(FFFFFF)V"))
    private void onConstructor(BlockMushroom instance, float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
    {
        if (FarmingHitboxes.INSTANCE.getMushroom() && FarmingHitboxes.INSTANCE.getEnabled()) instance.setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f);
        else instance.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

}