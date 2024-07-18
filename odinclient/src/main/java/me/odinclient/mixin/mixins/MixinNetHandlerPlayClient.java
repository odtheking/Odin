package me.odinclient.mixin.mixins;

import me.odinclient.features.impl.dungeon.GhostBlocks;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S21PacketChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "handleChunkData", at = @At("TAIL"))
    private void onChunkDataUpdate(S21PacketChunkData packetIn, CallbackInfo ci) {
        GhostBlocks.INSTANCE.postChunkData(packetIn);
    }
}