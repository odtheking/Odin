package me.odinclient.mixin;

import me.odinclient.OdinClient;
import me.odinclient.features.impl.general.DevPlayers;
import me.odinclient.features.impl.general.PersonalDragon;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Render.class)
public class MixinRender {

    @Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
    private void onRenderShadow(Entity entityIn, double x, double y, double z, float shadowAlpha, float partialTicks, CallbackInfo ci) {
        if (!PersonalDragon.INSTANCE.getEnabled() || !(entityIn instanceof EntityDragon)) return;
        if (entityIn.getEntityId() == PersonalDragon.INSTANCE.getEntityDragon().getEntityId()) {
            ci.cancel();
        }
    }
}
