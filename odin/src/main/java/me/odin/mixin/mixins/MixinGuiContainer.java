package me.odin.mixin.mixins;

import me.odinmain.events.impl.DrawGuiEvent;
import me.odinmain.events.impl.DrawSlotEvent;
import me.odinmain.events.impl.GuiClosedEvent;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiContainer.class, priority = 999)
public class MixinGuiContainer {

    private final GuiContainer gui = (GuiContainer) (Object) this;

    @Shadow
    public Container inventorySlots;

    @Shadow protected int xSize;

    @Shadow protected int ySize;

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(Slot slotIn, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new DrawSlotEvent(inventorySlots, gui, slotIn, slotIn.xDisplayPosition, slotIn.yDisplayPosition)))
            ci.cancel();
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerForegroundLayer(II)V"))
    private void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new DrawGuiEvent(gui.inventorySlots, gui, this.xSize, this.ySize));
    }

    @Inject(method = "onGuiClosed", at = @At("HEAD"))
    private void onGuiClosed(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new GuiClosedEvent(gui));
    }
}