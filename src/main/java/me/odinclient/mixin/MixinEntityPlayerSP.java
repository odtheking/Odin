package me.odinclient.mixin;

import me.odinclient.features.impl.skyblock.PortalFix;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {EntityPlayerSP.class}, priority = 999)
public abstract class MixinEntityPlayerSP {

    @Redirect(method = {"onLivingUpdate"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;doesGuiPauseGame()Z"))
    private boolean useChatInPortal(GuiScreen gui) {
        return PortalFix.INSTANCE.useChatInPortalMixin(gui);
    }
}
