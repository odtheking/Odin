package me.odinclient.mixin;

import me.odinclient.features.impl.general.LockCursor;
import me.odinclient.features.impl.qol.NoCursorReset;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MouseHelper.class, priority = 999)
public class MixinMouseHelper {
    // This mixin is used to prevent the mouse from being reset when the player opens a gui
    @Inject(method = "ungrabMouseCursor", at = @At("HEAD"), cancellable = true)
    private void ungrabMouseCursor(CallbackInfo ci) {
        if (NoCursorReset.INSTANCE.shouldHookMouse()) {
            ci.cancel();
            Mouse.setGrabbed(false);
        }
    }

    @Redirect(method = "mouseXYChange", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getDX()I", remap = false))
    private int getDX() {
        return LockCursor.INSTANCE.getEnabled() ? 0 : Mouse.getDX();
    }

    @Redirect(method = "mouseXYChange", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getDY()I", remap = false))
    private int getDY() {
        return LockCursor.INSTANCE.getEnabled() ? 0 : Mouse.getDY();
    }

}