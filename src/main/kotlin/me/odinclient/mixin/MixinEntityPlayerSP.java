package me.odinclient.mixin;

import me.odinclient.config.OdinConfig;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {EntityPlayerSP.class}, priority = 999)
public abstract class MixinEntityPlayerSP {

    @Redirect(method = {"onLivingUpdate"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;doesGuiPauseGame()Z"))
    private boolean useChatInPortal(GuiScreen gui) {
        if (OdinConfig.INSTANCE.getPortalFix()) {
            return (!(gui instanceof net.minecraft.client.gui.inventory.GuiContainer) || gui.doesGuiPauseGame());
        } else return gui.doesGuiPauseGame();
    }
}
