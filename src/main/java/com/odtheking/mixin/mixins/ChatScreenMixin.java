package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.MessageSentEvent;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String msg, boolean addToRecent, CallbackInfo ci) {
        if (new MessageSentEvent(msg).postAndCatch()) ci.cancel();
    }
}
