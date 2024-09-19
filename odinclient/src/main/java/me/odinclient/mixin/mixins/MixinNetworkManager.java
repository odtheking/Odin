package me.odinclient.mixin.mixins;

import io.netty.channel.ChannelHandlerContext;
import me.odinmain.events.impl.*;
import me.odinmain.utils.*;
import net.minecraft.network.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {NetworkManager.class}, priority = 800)
public class MixinNetworkManager {

    @Unique
    private boolean odinClient$isCancelled;

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (EventExtensions.postAndCatch(new PacketReceivedEvent(packet)))
            ci.cancel();
    }

    @Inject(method = {"sendPacket(Lnet/minecraft/network/Packet;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (!ServerUtils.INSTANCE.handleSendPacket(packet)) {
            if (odinClient$isCancelled) {
                odinClient$isCancelled = false;
                ci.cancel();
            }
        }
    }

    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> onSendPacket(Packet<?> packet) {
        PacketSentEvent event = new PacketSentEvent(packet);
        odinClient$isCancelled = EventExtensions.postAndCatch(event);
        return event.getPacket();
    }
}