package com.odtheking.mixin.mixins;

import com.odtheking.odin.features.impl.render.CustomNameReplacer;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public class FontMixin {

    @ModifyVariable(method = "prepareText(Ljava/lang/String;FFIZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At("HEAD"), argsOnly = true, name = "text")
    private String onPrepareTextString(String text) {
        return CustomNameReplacer.replaceStringIfNeeded(text);
    }

    @ModifyVariable(method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;", at = @At("HEAD"), argsOnly = true, name = "text")
    private FormattedCharSequence onPrepareTextSequence(FormattedCharSequence text) {
        return CustomNameReplacer.replaceSequenceIfNeeded(text);
    }

    @ModifyVariable(method = "width(Ljava/lang/String;)I", at = @At("HEAD"), argsOnly = true, name = "str")
    private String onWidthString(String str) {
        return CustomNameReplacer.replaceStringIfNeeded(str);
    }

    @ModifyVariable(method = "width(Lnet/minecraft/network/chat/FormattedText;)I", at = @At("HEAD"), argsOnly = true, name = "text")
    private FormattedText onWidthFormattedText(FormattedText text) {
        return CustomNameReplacer.replaceTextIfNeeded(text);
    }

    @ModifyVariable(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At("HEAD"), argsOnly = true, name = "text")
    private FormattedCharSequence onWidthSequence(FormattedCharSequence text) {
        return CustomNameReplacer.replaceSequenceIfNeeded(text);
    }
}