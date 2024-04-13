package me.odinclient.mixin.mixins;

import me.odinmain.features.impl.render.NameChanger;
import me.odinmain.features.impl.render.VisualWords;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = FontRenderer.class)
public class MixinFontRenderer {

    @ModifyVariable(method = "renderStringAtPos", at = @At("HEAD"), argsOnly = true)
    private String modifyRenderStringAtPos(String text) {
        text = NameChanger.modifyString(text);
        return VisualWords.replaceText(text);
    }

    @ModifyVariable(method = "getStringWidth", at = @At(value = "HEAD"), argsOnly = true)
    private String modifyGetStringWidth(String text) {
        text = NameChanger.modifyString(text);
        return VisualWords.replaceText(text);
    }
}