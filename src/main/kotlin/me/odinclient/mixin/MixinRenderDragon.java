package me.odinclient.mixin;

import me.odinclient.OdinClient;
import me.odinclient.features.impl.general.DevPlayers;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.EntityDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderDragon.class)
public class MixinRenderDragon {

    @Inject(method = "doRender(Lnet/minecraft/entity/boss/EntityDragon;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderLiving;doRender(Lnet/minecraft/entity/EntityLiving;DDDFF)V"))
    private void onDoRender(EntityDragon entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (!OdinClient.Companion.getConfig().getPersonalDragon()) return;
        if (entity.getEntityId() == DevPlayers.INSTANCE.getEntityDragon().getEntityId()) {
            BossStatus.bossName = null;
            BossStatus.statusBarTime = 0;
        }
    }
}
