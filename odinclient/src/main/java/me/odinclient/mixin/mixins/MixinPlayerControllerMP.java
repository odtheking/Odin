package me.odinclient.mixin.mixins;

import me.odinclient.features.impl.dungeon.GhostBlocks;
import me.odinclient.features.impl.skyblock.CancelInteract;
import me.odinmain.events.impl.GuiEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.odinmain.utils.Utils.postAndCatch;

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
        return CancelInteract.isHittingPositionHook(pos, currentItemHittingBlock, currentBlock);
    }

    @Inject(method = "windowClick", at = @At(value = "HEAD"), cancellable = true)
    private void onWindowClick(int windowId, int slotId, int mouseButtonClicked, int mode, EntityPlayer playerIn, CallbackInfoReturnable<ItemStack> cir) {
        if (postAndCatch(new GuiEvent.GuiWindowClickEvent(windowId, slotId, mouseButtonClicked, mode, playerIn)))
            cir.setReturnValue(null);
    }

    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"))
    private void onBlockBreak(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        GhostBlocks.INSTANCE.breakBlock(pos);
    }
}
