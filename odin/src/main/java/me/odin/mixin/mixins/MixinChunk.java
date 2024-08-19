package me.odin.mixin.mixins;

import me.odinmain.events.impl.BlockChangeEvent;
import me.odinmain.utils.EventExtensions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chunk.class)
public abstract class MixinChunk {
    @Shadow
    public abstract IBlockState getBlockState(final BlockPos pos);

    @Shadow @Final private World worldObj;

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    private void onBlockChange(BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir) {
        if (EventExtensions.postAndCatch(new BlockChangeEvent(pos, this.getBlockState(pos), state, this.worldObj)))
            cir.setReturnValue(this.getBlockState(pos));
    }
}
