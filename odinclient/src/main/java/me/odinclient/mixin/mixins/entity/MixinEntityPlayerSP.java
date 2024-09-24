package me.odinclient.mixin.mixins.entity;

import me.odinmain.events.impl.MessageSentEvent;
import me.odinmain.utils.EventExtensions;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (EventExtensions.postAndCatch(new MessageSentEvent(message)))
            ci.cancel();
    }
}
