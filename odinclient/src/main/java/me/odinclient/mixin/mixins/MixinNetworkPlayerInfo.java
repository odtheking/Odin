package me.odinclient.mixin.mixins;

import com.mojang.authlib.GameProfile;
import me.odinmain.features.impl.render.DevPlayers;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(NetworkPlayerInfo.class)
public abstract class MixinNetworkPlayerInfo {

    @Shadow private ResourceLocation locationCape;

    @Shadow @Final private GameProfile gameProfile;

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    private void getDevCape(CallbackInfoReturnable<ResourceLocation> cir) {
        this.locationCape = DevPlayers.INSTANCE.hookGetLocationCape(this.gameProfile);
        if (this.locationCape != null) cir.setReturnValue(this.locationCape);
    }
}
