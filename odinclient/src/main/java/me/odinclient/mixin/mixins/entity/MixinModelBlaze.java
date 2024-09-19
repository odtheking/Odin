package me.odinclient.mixin.mixins.entity;

import me.odinmain.features.impl.nether.BlazeAttunement;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBlaze;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlaze.class)
public abstract class MixinModelBlaze extends ModelBase {

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void changeBlazeColor(Entity entity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale, CallbackInfo ci) {
        BlazeAttunement.INSTANCE.changeBlazeColor(entity);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderPost(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale, CallbackInfo ci) {
        BlazeAttunement.INSTANCE.renderModelBlazePost();
    }
}