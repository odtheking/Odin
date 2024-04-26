package me.odin.mixin.mixins;

import me.odinmain.events.impl.DrawSlotOverlayEvent;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

    @Inject(method = "renderItemOverlayIntoGUI", at = @At("HEAD"), cancellable = true)
    private void renderItemOverlayPost(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new DrawSlotOverlayEvent(stack, xPosition, yPosition, text)))
            ci.cancel();
    }
}
