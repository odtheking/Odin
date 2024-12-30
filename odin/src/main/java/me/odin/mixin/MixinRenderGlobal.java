package me.odin.mixin;

import me.odinmain.features.impl.render.RenderOptimizer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Inject(at = @At("HEAD"), method = "renderEntities", cancellable = true)
    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        RenderOptimizer.hookRenderEntities(renderViewEntity, camera, partialTicks, ci);
    }
}
