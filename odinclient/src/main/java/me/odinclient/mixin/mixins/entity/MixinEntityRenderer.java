package me.odinclient.mixin.mixins.entity;

import me.odinclient.features.impl.render.Camera;
import me.odinmain.events.impl.RenderOverlayNoCaching;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/*
 * Camera stuff from Floppa Client
 * https://github.com/FloppaCoding/FloppaClient/blob/master/src/main/java/floppaclient/mixins/render/EntityRendererMixin.java
 */
@Mixin(value = EntityRenderer.class)
abstract public class MixinEntityRenderer implements IResourceManagerReloadListener {

    // idea from oneconfig https://github.com/Polyfrost/OneConfig/commit/15d616ec6e57f741ca64b07ff76ba30aaec115a4
    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    private void drawHud(float partialTicks, long nanoTime, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new RenderOverlayNoCaching(partialTicks));
    }

    @Redirect(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setAngles(FF)V"))
    private void lockPlayerLooking(EntityPlayerSP instance, float x, float y) {
        if (!Camera.INSTANCE.getFreelookToggled()) {
            instance.setAngles(x, y);
        }
    }

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setAngles(FF)V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void updateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo ci, boolean flag, float f, float f1, float f2, float f3) {
        if (Camera.INSTANCE.getEnabled()) Camera.INSTANCE.updateCameraAndRender(f2, f3);
    }

    @Shadow private Minecraft mc;
    @Shadow private float thirdPersonDistanceTemp;
    @Shadow private float thirdPersonDistance;

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V", ordinal = 2))
    public void orientCamera(float x, float y, float z, float partialTicks){
        double double0 = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * (double) partialTicks;
        double double1 = mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * (double) partialTicks + (double) mc.thePlayer.getEyeHeight();
        double double2 = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * (double) partialTicks;
        if (Camera.INSTANCE.getFreelookToggled()){
            GlStateManager.translate(0.0F, 0.0F, -Camera.INSTANCE.calculateCameraDistance(double0, double1, double2, Camera.INSTANCE.getCameraDistance()));
        } else {
            GlStateManager.translate(x, y, z);
        }
    }

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

}