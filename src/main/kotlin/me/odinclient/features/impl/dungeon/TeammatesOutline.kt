package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.RenderEntityModelEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.render.OutlineUtils
import me.odinclient.utils.render.RenderUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max

object TeammatesOutline : Module(
    "Teammate Outline",
    category = Category.DUNGEON
) {
    private val thickness: Float by NumberSetting("Line Width", 2f, 1.0, 5.0, 0.5)
    private val inBoss: Boolean by BooleanSetting("In boss")

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (!DungeonUtils.inDungeons || !DungeonUtils.teammates.any { it.first == event.entity } || (inBoss && DungeonUtils.inBoss)) return
        if (!config.teammatesOutlineWhenVisible && mc.thePlayer.canEntityBeSeen(event.entity)) return
        val color = DungeonUtils.teammates.first { it.first == event.entity }.second.color
        OutlineUtils.outlineEntity(
            event,
            thickness,
            color,
            false
        )
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons || (!inBoss && DungeonUtils.inBoss)) return
        DungeonUtils.teammates.forEach {
            if (mc.thePlayer.canEntityBeSeen(it.first)) return
            RenderUtils.drawStringInWorld(
                RenderUtils.renderVec(it.first).addVector(0.0, 2.7, 0.0),
                "${it.second.code}${it.first.name}",
                false,
                max(1.5f, (mc.thePlayer.getDistanceToEntity(it.first) / 15)),
                false
            )
        }
    }
}