package me.odin.mixin.mixins;

import me.odinmain.events.impl.MessageSentEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new MessageSentEvent(message)))
            ci.cancel();
    }
}