package me.odinclient.features.impl.general

import me.odinclient.features.Category
import me.odinclient.features.Module
import net.minecraft.block.material.Material
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NoWaterFOV : Module(
    "No Water FOV",
    category = Category.GENERAL,
    description = "Disable FOV change in water."
) {
    @SubscribeEvent
    fun onFOV(event: EntityViewRenderEvent.FOVModifier) {
        if (event.block.material != Material.water) return
        event.fov = event.fov * 70F / 60F
    }
}