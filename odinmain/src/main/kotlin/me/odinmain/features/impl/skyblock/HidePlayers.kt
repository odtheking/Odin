package me.odinmain.features.impl.skyblock

import me.odinmain.OdinMain.onLegitVersion
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.ServerUtils.getPing
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HidePlayers : Module(
    name = "Hide Players",
    description = "Hides players",
    category = Category.SKYBLOCK
) {
    private val hideAll: Boolean by BooleanSetting("Hide all", default = false, false, "Hides all players, regardless of distance")
    private val distance: Double by NumberSetting("distance", 3.0, 0.0, 32.0, .5, false, "The number of blocks away to hide players.").withDependency { !hideAll }

    @SubscribeEvent
    fun onRenderEntity(event: RenderPlayerEvent.Pre) {
        if (event.entity.getPing() != 1) return
        if (hideAll && event.entity != mc.thePlayer) { event.isCanceled = true }
        val distanceTo = event.entity.getDistanceToEntity(mc.thePlayer)
        if (distanceTo <= distance && !hideAll && event.entity != mc.thePlayer) { event.isCanceled = true }
    }
}