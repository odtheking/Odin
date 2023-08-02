package me.odinclient.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {

    @Invoker("rightClickMouse")
    void invokeRightClickMouse();

    @Invoker("clickMouse")
    void invokeClickMouse();

}
