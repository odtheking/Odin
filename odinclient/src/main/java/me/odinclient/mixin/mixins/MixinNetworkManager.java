package me.odinclient.mixin.mixins;

import io.netty.channel.ChannelHandlerContext;
import me.odinmain.events.impl.PacketSentEvent;
import me.odinmain.events.impl.ReceivePacketEvent;
import me.odinmain.utils.ServerUtils;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {NetworkManager.class}, priority = 800)
public class MixinNetworkManager {

    @Unique
    private boolean isCancelled;

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new ReceivePacketEvent(packet)))
            ci.cancel();
    }

    @Inject(method = {"sendPacket(Lnet/minecraft/network/Packet;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (!ServerUtils.INSTANCE.handleSendPacket(packet)) {
            if (isCancelled) ci.cancel();
        }
    }

    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> onSendPacket(Packet<?> packet) {
        PacketSentEvent event = new PacketSentEvent(packet);
        isCancelled = MinecraftForge.EVENT_BUS.post(event);
        return event.getPacket();
    }

}