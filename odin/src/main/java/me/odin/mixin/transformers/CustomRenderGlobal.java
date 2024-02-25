package me.odin.mixin.transformers;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderGlobal.class)
public interface CustomRenderGlobal {
    @Accessor("entityOutlineFramebuffer")
    Framebuffer getEntityOutlineFramebuffer();

    @Accessor("entityOutlineShader")
    ShaderGroup getEntityOutlineShader();
}
