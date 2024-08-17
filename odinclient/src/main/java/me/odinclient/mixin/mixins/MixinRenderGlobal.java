package me.odinclient.mixin.mixins;

import me.odinmain.ui.util.shader.OutlineShader;
import me.odinmain.utils.render.Color;
import me.odinmain.utils.render.HighlightRenderer;
import me.odinmain.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ICamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z"))
    public void renderEntitiesOutlines(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        HighlightRenderer.renderEntityOutline(camera, partialTicks);
    }
}
