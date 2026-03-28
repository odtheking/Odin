package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.GuiEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (new GuiEvent.Render((Screen) (Object) this, guiGraphics, mouseX, mouseY).postAndCatch()) ci.cancel();
    }

    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(GuiGraphics guiGraphics, Slot slot, int i, int j, CallbackInfo ci) {
        if (new GuiEvent.RenderSlot((Screen) (Object) this, guiGraphics, slot).postAndCatch()) ci.cancel();
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClickedSlot(Slot slot, int i, int j, ClickType clickType, CallbackInfo ci) {
        if (new GuiEvent.SlotClick((Screen) (Object) this, i, j).postAndCatch()) ci.cancel();
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void onDrawMouseoverTooltip(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
        if (new GuiEvent.DrawTooltip((Screen) (Object) this, context, mouseX, mouseY).postAndCatch()) ci.cancel();
    }
}