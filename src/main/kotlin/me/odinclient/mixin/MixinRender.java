package me.odinclient.mixin;

import me.odinclient.OdinClient;
import me.odinclient.features.impl.general.DevPlayers;
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
        if (!OdinClient.Companion.getConfig().getPersonalDragon() || !(entityIn instanceof EntityDragon)) return;
        if (entityIn.getEntityId() == DevPlayers.INSTANCE.getEntityDragon().getEntityId()) {
            ci.cancel();
        }
    }
}
