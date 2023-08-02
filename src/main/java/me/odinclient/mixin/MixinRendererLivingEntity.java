package me.odinclient.mixin;

import me.odinclient.events.RenderEntityModelEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity {

    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    private void renderModel(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo callbackInfo) {
        if (
            MinecraftForge.EVENT_BUS.post(
                new RenderEntityModelEvent(
                    entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, mainModel
                )
            )
        ) callbackInfo.cancel();
    }
}