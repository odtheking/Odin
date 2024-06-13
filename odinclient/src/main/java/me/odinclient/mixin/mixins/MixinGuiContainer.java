package me.odinclient.mixin.mixins;

import me.odinmain.events.impl.GuiEvent;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiContainer.class, priority = 1)
public abstract class MixinGuiContainer {

    @Unique
    private final GuiContainer gui = (GuiContainer) (Object) this;

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
        if (MinecraftForge.EVENT_BUS.post(new GuiEvent.DrawSlotEvent(inventorySlots, gui, slotIn, slotIn.xDisplayPosition, slotIn.yDisplayPosition)))
            ci.cancel();
    }

    @Inject(method = "drawScreen", at = @At(value = "HEAD"), cancellable = true)
    private void startDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiEvent.DrawGuiContainerScreenEvent event = new GuiEvent.DrawGuiContainerScreenEvent(gui.inventorySlots, gui, this.xSize, this.ySize, guiLeft, guiTop);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            ci.cancel();

            this.theSlot = null;
            for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
                Slot slot = this.inventorySlots.inventorySlots.get(i);
                if (!this.isMouseOverSlot(slot, mouseX, mouseY) || !slot.canBeHovered()) continue;
                this.theSlot = slot;
            }
        }
    }

    @Inject(method = "onGuiClosed", at = @At("HEAD"))
    private void onGuiClosed(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new GuiEvent.GuiClosedEvent(gui));
    }
}