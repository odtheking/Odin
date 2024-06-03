package me.odinclient.mixin.mixins;

import me.odinmain.events.impl.EntityLeaveWorldEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class MixinWorld {

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(Entity entityIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new EntityLeaveWorldEvent(entityIn));
    }
}
