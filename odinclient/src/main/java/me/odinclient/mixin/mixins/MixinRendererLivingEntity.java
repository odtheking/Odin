package me.odinclient.mixin.mixins;

import me.odinmain.OdinMain;
import me.odinmain.events.impl.RenderEntityModelEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.odinmain.utils.Utils.postAndCatch;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity<T extends EntityLivingBase> {

    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "setBrightness", at = @At(value = "HEAD"), cancellable = true)
    private  <T extends EntityLivingBase> void setBrightness(T entity, float partialTicks, boolean combineTextures, CallbackInfoReturnable<Boolean> cir) {
        if (OdinMain.INSTANCE.isShaderRunning()) cir.setReturnValue(false);
    }

    @Inject(method = "renderLayers", at = @At("RETURN"), cancellable = true)
    private void onRenderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_, CallbackInfo ci) {
        if (postAndCatch(new RenderEntityModelEvent(
                entitylivingbaseIn, p_177093_2_, p_177093_3_, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_, mainModel
        ))) {
            ci.cancel();
        }
    }

    @Inject(method = "renderName*", at = @At("HEAD"), cancellable = true)
    private void onRenderName(T entity, double x, double y, double z, CallbackInfo ci) {
        if (OdinMain.INSTANCE.isShaderRunning()) ci.cancel();
    }
}

