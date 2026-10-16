package com.odtheking.mixin.mixins;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.services.MinecraftServicesKeyInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftServicesKeyInfo.class, remap = false)
public class YggdrasilSignatureIgnoreMixin {

    @Inject(method = "validateProperty", at = @At("HEAD"), cancellable = true, remap = false)
    public void validate(Property property, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
