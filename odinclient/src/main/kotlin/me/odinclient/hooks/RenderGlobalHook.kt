package me.odinclient.hooks

import me.odinclient.utils.EntityOutlineRenderer
import me.odinmain.utils.render.world.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.culling.ICamera
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

class RenderGlobalHook {

    fun renderEntitiesOutlines(camera: ICamera?, partialTicks: Float): Boolean {
        val vec = RenderUtils.exactLocation(Minecraft.getMinecraft().renderViewEntity, partialTicks)
        return EntityOutlineRenderer.renderEntityOutlines(camera!!, partialTicks, vec)
    }

    fun shouldRenderEntityOutlines(cir: CallbackInfoReturnable<Boolean?>) {
        if (EntityOutlineRenderer.shouldRenderEntityOutlines()) {
            cir.returnValue = true
        }
    }
}