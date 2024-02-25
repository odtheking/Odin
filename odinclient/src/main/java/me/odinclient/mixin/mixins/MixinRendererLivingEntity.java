package me.odinclient.mixin.mixins;

import me.odinclient.hooks.RendererLivingEntityHook;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity {

    @Unique
    private final RendererLivingEntityHook odin$hook = new RendererLivingEntityHook();

    @Redirect(method = "setScoreTeamColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"))
    public void setOutlineColor(float colorRed, float colorGreen, float colorBlue, float colorAlpha, EntityLivingBase entity) {
        odin$hook.setOutlineColor(colorRed, colorGreen, colorBlue, colorAlpha, entity);
    }
}
