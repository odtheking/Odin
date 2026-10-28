package com.odtheking.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.odtheking.odin.events.BlockUpdateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    @WrapOperation(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onBlockChange(LevelChunkSection section, int sectionX, int sectionY, int sectionZ, BlockState state, Operation<BlockState> original, @Local(argsOnly = true, name = "pos") BlockPos pos) {
        BlockState old = original.call(section, sectionX, sectionY, sectionZ, state);
        if (old != state) new BlockUpdateEvent(pos, old, state).postAndCatch();
        return old;
    }
}