package me.odin.mixin;

import me.odinmain.features.impl.render.Animations;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(method = {"transformFirstPersonItem(FF)V"}, at = @At("HEAD"), cancellable = true)
    public void itemTransform(float equipProgress, float swingProgress, CallbackInfo ci) {
        if (Animations.INSTANCE.itemTransferHook(equipProgress, swingProgress)) ci.cancel();
    }

}
