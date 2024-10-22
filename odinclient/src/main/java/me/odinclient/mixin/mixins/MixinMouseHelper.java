package me.odinclient.mixin.mixins;

import me.odinmain.features.impl.skyblock.NoCursorReset;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MouseHelper.class, priority = 999)
public class MixinMouseHelper {
    // This mixin is used to prevent the mouse from being reset when the player opens a gui
    @Inject(method = "ungrabMouseCursor", at = @At("HEAD"), cancellable = true)
    private void ungrabMouseCursor(CallbackInfo ci) {
        if (NoCursorReset.shouldHookMouse()) {
            ci.cancel();
            Mouse.setGrabbed(false);
        }
    }
}