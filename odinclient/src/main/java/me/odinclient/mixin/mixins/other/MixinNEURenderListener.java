package me.odinclient.mixin.mixins.other;

import me.odinmain.features.impl.dungeon.LeapMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.listener.RenderListener", remap = false)
public class MixinNEURenderListener {

    @Unique
    private final Minecraft mc = Minecraft.getMinecraft();

    @Inject(method = "onGuiScreenDrawPre", at = @At("HEAD"), cancellable = true, remap = false)
    private void neuInvButtons(GuiScreenEvent.DrawScreenEvent.Pre event, CallbackInfo ci) {
        if (LeapMenu.INSTANCE.getEnabled() && mc.currentScreen instanceof GuiChest && ((GuiChest) mc.currentScreen).inventorySlots instanceof ContainerChest) {
            String guiName = ((ContainerChest) ((GuiChest) mc.currentScreen).inventorySlots).getLowerChestInventory().getDisplayName().getUnformattedText();
            if (guiName.equals("Spirit Leap")) {
                ci.cancel();
            }
        }
    }
}
