package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.GuiEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (new GuiEvent.Render((Screen) (Object) this, graphics, mouseX, mouseY).postAndCatch()) ci.cancel();
    }

    @Inject(method = "extractSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (new GuiEvent.RenderSlot((Screen) (Object) this, graphics, slot).postAndCatch()) ci.cancel();
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClickedSlot(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        if (new GuiEvent.SlotClick((Screen) (Object) this, slotId, buttonNum).postAndCatch()) ci.cancel();
    }

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
    private void onDrawMouseoverTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (new GuiEvent.DrawTooltip((Screen) (Object) this, graphics, mouseX, mouseY).postAndCatch()) ci.cancel();
    }
}