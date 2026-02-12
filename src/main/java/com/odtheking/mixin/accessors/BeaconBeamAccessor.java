package com.odtheking.mixin.accessors;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BeaconRenderer.class)
public interface BeaconBeamAccessor {
    @Invoker("submitBeaconBeam")
    static void invokeRenderBeam(
            PoseStack matrices, SubmitNodeCollector queue, Identifier textureId, float beamHeight, float beamRotationDegrees, int minHeight, int maxHeight, int color, float innerScale, float outerScale
    ) {
        throw new UnsupportedOperationException();
    }
}
