package me.odinmain.mixin;

import me.odin.events.impl.PostEntityMetadata;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({S1CPacketEntityMetadata.class})
public class MixinS1CPacketEntityMetadata {

    @Redirect(method = {"processPacket(Lnet/minecraft/network/play/INetHandlerPlayClient;)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/INetHandlerPlayClient;handleEntityMetadata(Lnet/minecraft/network/play/server/S1CPacketEntityMetadata;)V"))
    private void redirectProcessPacket(INetHandlerPlayClient instance, S1CPacketEntityMetadata packet) {
        instance.handleEntityMetadata(packet);
        MinecraftForge.EVENT_BUS.post(new PostEntityMetadata(packet));
    }

}