package me.odinclient.mixin.accessors;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface IMinecraftAccessor {

    @Accessor("timer")
    Timer getTimer();

    @Invoker
    public void invokeUpdateDebugProfilerName(int keyCount);
}
