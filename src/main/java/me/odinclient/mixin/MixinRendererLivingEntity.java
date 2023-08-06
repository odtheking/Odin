package me.odinclient.mixin;

import me.odinclient.events.RenderEntityModelEvent;
import me.odinclient.features.impl.dungeon.TeammatesOutline;
import me.odinclient.features.impl.general.ArrowTrajectory;
import me.odinclient.features.impl.general.BlazeAtunement;
import me.odinclient.features.impl.general.ESP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity {

    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "renderModel", at = @At("HEAD"))
    private void renderModel(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo callbackInfo) {
        // doesn't post to forge's event bus to gain some performance
        RenderEntityModelEvent event = new RenderEntityModelEvent(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, mainModel);
        TeammatesOutline.INSTANCE.onRenderEntityModel(event);
        ArrowTrajectory.INSTANCE.onRenderModel(event);
        ESP.INSTANCE.onRenderEntityModel(event);
        BlazeAtunement.INSTANCE.onRenderEntityModel(event);
    }
}