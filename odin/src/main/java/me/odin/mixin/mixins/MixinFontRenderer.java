package me.odin.mixin.mixins;

import me.odinmain.features.impl.render.NickHider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FontRenderer.class)
public class MixinFontRenderer {

    @ModifyVariable(method = "renderStringAtPos", at = @At("HEAD"), argsOnly = true)
    private String modifyRenderStringAtPos(String text) {
        if (text == null) return null;
        String name = Minecraft.getMinecraft().getSession().getUsername();
        String nick = NickHider.INSTANCE.getNick().replaceAll("&", "§").replaceAll("\\$", "");
        return text.replaceAll(name, nick);
    }

    @ModifyVariable(method = "getStringWidth", at = @At(value = "HEAD"), argsOnly = true)
    private String modifyGetStringWidth(String text) {
        if (text == null) return null;
        String name = Minecraft.getMinecraft().getSession().getUsername();
        String nick = NickHider.INSTANCE.getNick().replaceAll("&", "§").replaceAll("\\$", "");
        return text.replaceAll(name, nick);
    }

}
