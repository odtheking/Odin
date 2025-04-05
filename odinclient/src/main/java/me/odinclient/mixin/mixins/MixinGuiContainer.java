package me.odinclient.mixin.mixins;

import me.odinmain.events.impl.GuiEvent;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.odinmain.utils.Utils.postAndCatch;

@Mixin(value = GuiContainer.class, priority = 1)
public abstract class MixinGuiContainer {

    @Unique
    private final GuiContainer odinMod$gui = (GuiContainer) (Object) this;

    @Shadow
    public Container inventorySlots;

    @Shadow protected int xSize;

    @Shadow protected int ySize;

    @Shadow protected int guiLeft;

    @Shadow protected int guiTop;

    @Shadow private Slot theSlot;

    @Shadow protected abstract boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY);

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(Slot slotIn, CallbackInfo ci) {
        if (postAndCatch(new GuiEvent.DrawSlot(odinMod$gui, slotIn, slotIn.xDisplayPosition, slotIn.yDisplayPosition))) ci.cancel();
    }

    @Inject(method = "drawScreen", at = @At(value = "HEAD"), cancellable = true)
    private void startDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (postAndCatch(new GuiEvent.DrawGuiBackground(odinMod$gui, this.xSize, this.ySize, guiLeft, guiTop))) {
            ci.cancel();

            this.theSlot = null;
            for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
                Slot slot = this.inventorySlots.inventorySlots.get(i);
                if (!this.isMouseOverSlot(slot, mouseX, mouseY) || !slot.canBeHovered()) continue;
                this.theSlot = slot;
            }
        }
    }

    @Inject(method = "drawScreen", at = @At("TAIL"), cancellable = true)
    private void onEndDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (postAndCatch(new GuiEvent.DrawGuiForeground(odinMod$gui, this.xSize, this.ySize, guiLeft, guiTop, mouseX, mouseY))) ci.cancel();
    }
}