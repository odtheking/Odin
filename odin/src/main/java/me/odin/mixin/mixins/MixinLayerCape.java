package me.odin.mixin.mixins;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LayerCape.class)
public class MixinLayerCape {

    @Redirect(method = "doRenderLayer(Lnet/minecraft/client/entity/AbstractClientPlayer;FFFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getLocationCape()Lnet/minecraft/util/ResourceLocation;"))
    private ResourceLocation redirectGetLocationCape(AbstractClientPlayer instance) {
        /*String dev = instance.getGameProfile().getName().replace("_", "");
        if (DevPlayers.INSTANCE.getDevs().containsKey(instance.getGameProfile().getName())) {
            return new ResourceLocation("odinmain", "capes/Inton.png");
        }*/
        return instance.getLocationCape();
    }

}
