package me.odinmain.features.impl.skyblock

import me.odinmain.OdinMain.onLegitVersion
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.ServerUtils.getPing
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HidePlayers : Module(
    name = "Hide Players",
    description = "Hides players",
    category = Category.SKYBLOCK
) {
    private val hideAll: Boolean by BooleanSetting("Hide all", default = false, false, "Hides all players, regardless of distance")
    private val distance: Double by NumberSetting("distance", 3.0, 0.0, 32.0, .5, false, "The number of blocks away to hide players.").withDependency { !hideAll }
    private val clickThrough: Boolean by BooleanSetting("Click Through", default = false, false, "Allows clicking through players.").withDependency { !onLegitVersion }

    @SubscribeEvent
    fun onRenderEntity(event: RenderPlayerEvent.Pre) {
        if (event.entity.getPing() != 1 || clickThrough || event.entity == mc.thePlayer) return
        if (hideAll && event.entity != mc.thePlayer) { event.isCanceled = true }
        val distanceTo = event.entity.getDistanceToEntity(mc.thePlayer)
        if (distanceTo <= distance && event.entity != mc.thePlayer || hideAll && event.entity != mc.thePlayer ) { event.isCanceled = true }
    }

    @SubscribeEvent
    fun onPosUpdate(event: LivingEvent.LivingUpdateEvent) {
        if (event.entity.getPing() != 1 || !clickThrough || event.entity == mc.thePlayer) return
        val distanceTo = event.entity.getDistanceToEntity(mc.thePlayer)
        if (distanceTo <= distance && event.entity != mc.thePlayer || hideAll && event.entity != mc.thePlayer ) {
            event.entity.posX = 9999999.0
            event.isCanceled = true
        }
    }

}