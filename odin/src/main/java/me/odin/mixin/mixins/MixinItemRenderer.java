package me.odin.mixin.mixins;

import me.odinmain.features.impl.render.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemRenderer.class, priority = 1)
public abstract class MixinItemRenderer {

    @Shadow
    private float prevEquippedProgress;

    @Shadow private float equippedProgress;

    @Final
    @Shadow private Minecraft mc;

    @Shadow private ItemStack itemToRender;

    @Shadow protected abstract void rotateArroundXAndY(float angle, float angleY);

    @Shadow protected abstract void setLightMapFromPlayer(AbstractClientPlayer clientPlayer);

    @Shadow protected abstract void rotateWithPlayerRotations(EntityPlayerSP entityplayerspIn, float partialTicks);

    @Shadow protected abstract void renderItemMap(AbstractClientPlayer clientPlayer, float pitch, float equipmentProgress, float swingProgress);

    @Shadow protected abstract void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks);

    @Shadow protected abstract void doBlockTransformations();

    @Shadow protected abstract void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer);

    @Shadow protected abstract void doItemUsedTransformations(float swingProgress);

    @Shadow public abstract void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform);

    @Shadow protected abstract void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress);

    @Shadow protected abstract void transformFirstPersonItem(float equipProgress, float swingProgress);

    @Inject(method = "transformFirstPersonItem", at = @At("HEAD"), cancellable = true)
    public void onTransformFirstPersonItem(float equipProgress, float swingProgress, CallbackInfo ci) {
        if (Animations.INSTANCE.itemTransferHook(equipProgress, swingProgress)) ci.cancel();
    }

    /**
     * @author Cezar
     * @reason 1.7 Block Animation
     */

    @Overwrite
    public void renderItemInFirstPerson(float partialTicks) {
        float f = 1.0f - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
        EntityPlayerSP abstractclientplayer = this.mc.thePlayer;
        float f1 = abstractclientplayer.getSwingProgress(partialTicks);
        float f2 = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
        float f3 = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
        this.rotateArroundXAndY(f2, f3);
        this.setLightMapFromPlayer(abstractclientplayer);
        this.rotateWithPlayerRotations(abstractclientplayer, partialTicks);
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        if (this.itemToRender != null) {
            if (this.itemToRender.getItem() instanceof ItemMap) {
                this.renderItemMap(abstractclientplayer, f2, f, f1);
            } else if (abstractclientplayer.getItemInUseCount() > 0) {
                EnumAction enumaction = this.itemToRender.getItemUseAction();
                boolean isBlockHit = Animations.INSTANCE.getEnabled() && Animations.INSTANCE.getBlockHit();
                switch (enumaction) {
                    case NONE: {
                        this.transformFirstPersonItem(f, 0.0f);
                        break;
                    }
                    case EAT:
                    case DRINK: {
                        this.performDrinking(abstractclientplayer, partialTicks);
                        this.transformFirstPersonItem(f, isBlockHit ? f1 : 0.0f);
                        break;
                    }
                    case BLOCK: {
                        this.transformFirstPersonItem(f, isBlockHit ? f1 : 0.0f);
                        this.doBlockTransformations();
                        break;
                    }
                    case BOW: {
                        this.transformFirstPersonItem(f, isBlockHit ? f1 : 0.0f);
                        this.doBowTransformations(partialTicks, abstractclientplayer);
                    }
                }
            } else {
                this.doItemUsedTransformations(f1);
                this.transformFirstPersonItem(f, f1);
            }
            this.renderItem(abstractclientplayer, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
        } else if (!abstractclientplayer.isInvisible()) {
            this.renderPlayerArm(abstractclientplayer, f, f1);
        }
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    @Inject(method = "doItemUsedTransformations", at = @At("HEAD"), cancellable = true)
    private void noSwing(float swingProgress, CallbackInfo ci) { if (Animations.INSTANCE.getNoSwing() && Animations.INSTANCE.getEnabled()) ci.cancel(); }

}
