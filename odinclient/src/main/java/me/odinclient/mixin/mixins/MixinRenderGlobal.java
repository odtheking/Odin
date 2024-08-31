package me.odinclient.mixin.mixins;

import me.odinmain.ui.util.shader.OutlineShader;
import me.odinmain.utils.render.HighlightRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z"))
    public void renderEntitiesOutlines(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        HighlightRenderer.renderEntityOutline(camera, partialTicks);
    }

    @Inject(method = "renderEntities", at = @At(value = "TAIL", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z"))
    public void prenderEntitiesOutlines(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        OutlineShader.INSTANCE.draw();
    }
}
