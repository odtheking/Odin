package me.odin.mixin.mixins;

import me.odinmain.features.impl.render.NickHider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Shadow protected abstract void renderStringAtPos(String text, boolean shadow);

    @Shadow public abstract int getStringWidth(String text);

    @Inject(method = "renderStringAtPos", at = @At("HEAD"), cancellable = true)
    private void onRenderStringAtPos(String text, boolean shadow, CallbackInfo ci)
    {
        String name = Minecraft.getMinecraft().getSession().getUsername();
        if (text != null && NickHider.INSTANCE.getEnabled() && text.contains(name) && !name.equals(NickHider.INSTANCE.getNick()))
        {
            String nick = NickHider.INSTANCE.getNick().replaceAll("\\$", "");
            text = text.replaceAll("&", "§").replaceAll(name, nick);
            this.renderStringAtPos(text, shadow);
            ci.cancel();
        }
    }

    @Inject(method = "getStringWidth", at = @At("HEAD"), cancellable = true)
    private void onGetStringWidth(String text, CallbackInfoReturnable<Integer> cir)
    {
        String name = Minecraft.getMinecraft().getSession().getUsername();
        if (text != null && NickHider.INSTANCE.getEnabled() && text.contains(name) && !name.equals(NickHider.INSTANCE.getNick()))
        {
            String nick = NickHider.INSTANCE.getNick().replaceAll("\\$", "");
            text = text.replaceAll("&", "§").replaceAll(name, nick);
            cir.setReturnValue(this.getStringWidth(text));
        }
    }

}
