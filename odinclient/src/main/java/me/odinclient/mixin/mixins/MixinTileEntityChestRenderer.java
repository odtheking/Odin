package me.odinclient.mixin.mixins;

import me.odinmain.events.impl.RenderChestEvent;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityChestRenderer.class)
public class MixinTileEntityChestRenderer {

    @Inject(method = { "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityChest;DDDFI)V" }, at = { @At("HEAD") })
    public void onDrawChest(TileEntityChest te, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new RenderChestEvent.Pre(te, x, y, z, partialTicks));
    }

    @Inject(method = { "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityChest;DDDFI)V" }, at = { @At("RETURN") })
    public void onDrawChestPost(final TileEntityChest te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new RenderChestEvent.Post(te, x, y, z, partialTicks));
    }

}
