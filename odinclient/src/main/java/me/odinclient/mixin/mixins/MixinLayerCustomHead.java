package me.odinclient.mixin.mixins;

import me.odinclient.features.impl.render.HideArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerCustomHead.class)
public abstract class MixinLayerCustomHead implements LayerRenderer<EntityLivingBase> {

    @Inject(method = "doRenderLayer", at = @At(value = "HEAD"), cancellable = true)
    private void renderCustomHeadLayer(EntityLivingBase entitylivingbaseIn, float f, float g, float partialTicks, float h, float i, float j, float scale, CallbackInfo ci) {
        if (HideArmor.shouldHideSkull(entitylivingbaseIn))
            ci.cancel();
    }
}
