package me.odinclient.mixin.mixins;

import me.odinclient.features.impl.skyblock.NoBreakReset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Shadow private ItemStack currentItemHittingBlock;

    @Shadow @Final private Minecraft mc;

    @Shadow private BlockPos currentBlock;

    @Inject(method = "isHittingPosition", at = @At("RETURN"), cancellable = true)
    private void onIsHittingPosition(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (NoBreakReset.INSTANCE.getEnabled()) {
            ItemStack itemStack = this.mc.thePlayer.getHeldItem();
            String lore = itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8).toString();
            if (lore.contains("GAUNTLET") || lore.contains("DRILL") || lore.contains("PICKAXE")) {
                cir.setReturnValue(pos.equals(this.currentBlock) && (itemStack.getItem() == this.currentItemHittingBlock.getItem()));
            }
        }
        else {
            cir.setReturnValue(cir.getReturnValue());
        }
    }

}
