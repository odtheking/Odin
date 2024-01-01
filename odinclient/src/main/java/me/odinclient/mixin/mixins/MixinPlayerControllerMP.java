package me.odinclient.mixin.mixins;

import me.odinclient.features.impl.skyblock.NoBreakReset;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Debug(export = true)
@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Shadow private ItemStack currentItemHittingBlock;

    @Shadow private BlockPos currentBlock;

    /**
     * @author a
     * @reason b
     */
    @Overwrite
    private boolean isHittingPosition(BlockPos pos) {
        return NoBreakReset.isHittingPositionHook(pos, currentItemHittingBlock, currentBlock);
    }
}
