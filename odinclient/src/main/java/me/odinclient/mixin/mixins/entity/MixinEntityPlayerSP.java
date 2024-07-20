package me.odinclient.mixin.mixins.entity;

import me.odinmain.events.impl.MessageSentEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static me.odinmain.utils.Utils.postAndCatch;

@Mixin(value = EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (postAndCatch(new MessageSentEvent(message)))
            ci.cancel();
    }
}
