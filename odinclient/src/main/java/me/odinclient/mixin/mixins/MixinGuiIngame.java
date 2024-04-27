package me.odinclient.mixin.mixins;

import me.odinmain.features.impl.render.PlayerDisplay;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = GuiIngame.class)
public class MixinGuiIngame {

    @ModifyVariable(method = "setRecordPlaying(Ljava/lang/String;Z)V", at = @At("HEAD"), argsOnly = true)
    private String modifyActionBar(String text) {
        return PlayerDisplay.INSTANCE.modifyText(text);
    }
}
