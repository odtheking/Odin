package me.odinclient.mixin.mixins.entity;

import gg.essential.lib.mixinextras.injector.ModifyExpressionValue;
import me.odinclient.features.impl.render.NoDebuff;
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


    @ModifyExpressionValue(method = {"pushOutOfBlocks"}, at = @At(value = "FIELD", target =  "Lnet/minecraft/client/entity/EntityPlayerSP;noClip:Z"))
    public boolean shouldPrevent(boolean original) {
        return NoDebuff.INSTANCE.isNoPush() || original;
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new MessageSentEvent(message)))
            ci.cancel();
    }
}
