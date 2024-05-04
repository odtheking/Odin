package me.odin.mixin.mixins;

import me.odinmain.events.impl.RenderOverlayNoCaching;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class)
abstract public class MixinEntityRenderer implements IResourceManagerReloadListener {

    // idea from oneconfig https://github.com/Polyfrost/OneConfig/commit/15d616ec6e57f741ca64b07ff76ba30aaec115a4
    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    private void drawHud(float partialTicks, long nanoTime, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new RenderOverlayNoCaching(partialTicks));
    }
}
