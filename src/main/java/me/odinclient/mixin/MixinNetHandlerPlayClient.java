package me.odinclient.mixin;

import me.odinclient.events.SpawnParticleEvent;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({NetHandlerPlayClient.class})
public class MixinNetHandlerPlayClient {

    @Redirect(method = {"handleParticles"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;ZDDDDDD[I)V"))
    public void redirectHandleParticles(WorldClient world, EnumParticleTypes particleTypes, boolean isLongDistance, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int[] params) {
        MinecraftForge.EVENT_BUS.post(new SpawnParticleEvent(particleTypes, isLongDistance, xCoord, yCoord, zCoord));
        world.spawnParticle(particleTypes, isLongDistance, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, params);
    }
}
