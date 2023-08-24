package me.odinclient.features.impl.render

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.renderText
import me.odinclient.utils.skyblock.LocationUtils.inSkyblock
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


object FirePillar : Module(
    "Show Fire Pillar",
    category = Category.RENDER
) {
    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || !inSkyblock || mc.ingameGUI == null) return
        mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()
            .filter { StringUtils.stripControlCodes(it.name).endsWith("s 8 hits") }.withIndex().forEach {
                val sr = ScaledResolution(mc)
                renderText(
                    text = it.value.name,
                    x = sr.scaledWidth / 2 - mc.fontRendererObj.getStringWidth(it.value.name),
                    y = sr.scaledHeight / 2 - 30 * it.index,
                    scale = 2.0
                )
            }
    }
}