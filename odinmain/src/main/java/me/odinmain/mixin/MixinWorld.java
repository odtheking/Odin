package me.odinmain.mixin;

import me.odin.events.impl.BlockUpdateEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {

    @Inject(method = "setBlockState(Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z", at = @At("HEAD"))
    private void onsetBlockState(BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir)
    {
        MinecraftForge.EVENT_BUS.post(new BlockUpdateEvent(pos, state));
    }
}
