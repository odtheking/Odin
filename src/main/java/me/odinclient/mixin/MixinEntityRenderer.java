package me.odinclient.mixin;

import me.odinclient.features.impl.render.Camera;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
}