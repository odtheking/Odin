package me.odinclient.mixin.mixins;

import me.odinmain.events.impl.BlockUpdateEvent;
import me.odinmain.events.impl.EntityLeaveWorldEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {

    @Inject(method = "setBlockState(Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z", at = @At("HEAD"))
    private void onsetBlockState(BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        MinecraftForge.EVENT_BUS.post(new BlockUpdateEvent(pos, state));
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(Entity entityIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new EntityLeaveWorldEvent(entityIn));
    }
}
