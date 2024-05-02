package me.odin.mixin.mixins;

import me.odinmain.events.impl.RenderOverlayHudCaching;
import me.odinmain.features.impl.render.Camera;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * From Floppa Client
 * https://github.com/FloppaCoding/FloppaClient/blob/master/src/main/java/floppaclient/mixins/render/EntityRendererMixin.java
 */
@Mixin(value = EntityRenderer.class)
abstract public class MixinEntityRenderer implements IResourceManagerReloadListener {

    @Redirect(method = {"orientCamera"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;thirdPersonDistance:F"))
    public float tweakThirdPersonDistance(EntityRenderer instance) {
        return Camera.INSTANCE.getCameraDistance();
    }

    @Redirect(method = {"orientCamera"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;thirdPersonDistanceTemp:F"))
    public float tweakThirdPersonDistanceTemp(EntityRenderer instance) {
        return Camera.INSTANCE.getCameraDistance();
    }

    @ModifyConstant(method = "orientCamera", constant = @Constant(intValue = 8))
    public int cameraClip(int constant) {
        return Camera.INSTANCE.getCameraClipEnabled() ? 0: constant;
    }

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    private void drawHud(float partialTicks, long nanoTime, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new RenderOverlayHudCaching());
    }
}