package me.odinclient.mixin.model;

import me.odinclient.features.impl.general.BlazeAtunement;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public abstract class MixinModelBiped extends ModelBase {

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void changeBlazeColor(Entity entity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale, CallbackInfo ci) {
        BlazeAtunement.INSTANCE.changeBipedColor(entity, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, ci);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderPost(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale, CallbackInfo ci) {
        BlazeAtunement.INSTANCE.renderModelBipedPost(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, ci);
    }
}
