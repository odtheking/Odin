package me.odinclient.mixin.mixins;

import me.odinmain.OdinMain;
import me.odinmain.events.impl.GuiEvent;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.odinmain.utils.Utils.postAndCatch;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

    @Inject(method = "renderItemOverlayIntoGUI", at = @At("HEAD"), cancellable = true)
    private void renderItemOverlayPost(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
        if (postAndCatch(new GuiEvent.DrawSlotOverlay(stack, xPosition, yPosition, text))) ci.cancel();
    }

    @Redirect(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasEffect()Z"))
    private boolean redirectHasEffect(ItemStack stack) {
        return stack.hasEffect() && !OdinMain.INSTANCE.isShaderRunning();
    }
}