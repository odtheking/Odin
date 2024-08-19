package me.odin.mixin.mixins;

import io.netty.channel.ChannelHandlerContext;
import me.odinmain.events.impl.PacketReceivedEvent;
import me.odinmain.events.impl.PacketSentEvent;
import me.odinmain.utils.EventExtensions;
import me.odinmain.utils.ServerUtils;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {NetworkManager.class}, priority = 1002)
public class MixinNetworkManager {

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (EventExtensions.postAndCatch(new PacketReceivedEvent(packet)) && !ci.isCancelled())
            ci.cancel();
    }

    @Inject(method = {"sendPacket(Lnet/minecraft/network/Packet;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (!ServerUtils.INSTANCE.handleSendPacket(packet)) {
            if (EventExtensions.postAndCatch(new PacketSentEvent(packet)) && !ci.isCancelled())
                ci.cancel();
        }
    }
}