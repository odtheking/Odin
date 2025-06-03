package me.odinclient.mixin.mixins;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.odinmain.OdinMain.fireShaderRender;

@Mixin(EntityRenderer.class)
abstract class MixinEntityRenderer {

    @Inject(method = "updateCameraAndRender(FJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", ordinal = 0))
    private void injectBeforeGuiSection(float partialTicks, long nanoTime, CallbackInfo ci) {
        fireShaderRender(partialTicks);
    }
}