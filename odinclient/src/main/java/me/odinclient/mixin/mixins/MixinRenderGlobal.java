package me.odinclient.mixin.mixins;

import me.odinmain.features.impl.render.RenderOptimizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Inject(at = @At("HEAD"), method = "renderEntities", cancellable = true)
    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        RenderOptimizer.INSTANCE.hookRenderEntities(renderViewEntity, camera, partialTicks, ci);
    }

}
