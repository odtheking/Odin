package me.odin.mixin.mixins;

import me.odin.mixin.accessors.IMinecraftAccessor;
import me.odinmain.features.impl.render.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemRenderer.class, priority = 9999)
public abstract class MixinItemRenderer {

    @Final
    @Shadow private Minecraft mc;

    @Shadow private ItemStack itemToRender;

    @Shadow protected abstract void transformFirstPersonItem(float equipProgress, float swingProgress);

    @Inject(method = "transformFirstPersonItem", at = @At("HEAD"), cancellable = true)
    public void onTransformFirstPersonItem(float equipProgress, float swingProgress, CallbackInfo ci) {
        if (Animations.INSTANCE.itemTransferHook(equipProgress, swingProgress)) ci.cancel();
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getItemInUseCount()I"))
    private int noBlockHook(AbstractClientPlayer instance) {
        if (Animations.INSTANCE.getEnabled() && Animations.INSTANCE.getNoBlock()) {
            return Animations.INSTANCE.getItemInUseCountHook(instance, itemToRender);
        }
        return instance.getItemInUseCount();
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 0))
    private void renderItemHook(ItemRenderer instance, float equipProgress, float swingProgress) {
        if (Animations.INSTANCE.getEnabled()) {
            this.transformFirstPersonItem(equipProgress, swingProgress);
        }
        else {
            this.transformFirstPersonItem(equipProgress, swingProgress);
        }
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 1))
    private void renderItemHook1(ItemRenderer instance, float equipProgress, float swingProgress) {
        if (Animations.INSTANCE.getEnabled()) {
            this.transformFirstPersonItem(equipProgress, Animations.INSTANCE.getBlockHit() ? mc.thePlayer.getSwingProgress(((IMinecraftAccessor) mc).getTimer().elapsedPartialTicks) : swingProgress);
        }
        else {
            this.transformFirstPersonItem(equipProgress, swingProgress);
        }
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 2))
    private void renderItemHook2(ItemRenderer instance, float equipProgress, float swingProgress) {
        if (Animations.INSTANCE.getEnabled()) {
            this.transformFirstPersonItem(equipProgress, Animations.INSTANCE.getBlockHit() ? mc.thePlayer.getSwingProgress(((IMinecraftAccessor) mc).getTimer().elapsedPartialTicks) : swingProgress);
        }
        else {
            this.transformFirstPersonItem(equipProgress, swingProgress);
        }
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 3))
    private void renderItemHook3(ItemRenderer instance, float equipProgress, float swingProgress) {
        if (Animations.INSTANCE.getEnabled()) {
            this.transformFirstPersonItem(equipProgress, Animations.INSTANCE.getBlockHit() ? mc.thePlayer.getSwingProgress(((IMinecraftAccessor) mc).getTimer().elapsedPartialTicks) : swingProgress);
        }
        else {
            this.transformFirstPersonItem(equipProgress, swingProgress);
        }
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 4))
    private void renderItemHook4(ItemRenderer instance, float equipProgress, float swingProgress) {
        if (Animations.INSTANCE.getEnabled()) {
            this.transformFirstPersonItem(equipProgress, swingProgress);
        }
        else {
            this.transformFirstPersonItem(equipProgress, swingProgress);
        }
    }

    @Inject(method = "doItemUsedTransformations", at = @At("HEAD"), cancellable = true)
    private void noSwing(float swingProgress, CallbackInfo ci) {
        if (Animations.INSTANCE.getShouldStopSwing()) ci.cancel();
    }

}