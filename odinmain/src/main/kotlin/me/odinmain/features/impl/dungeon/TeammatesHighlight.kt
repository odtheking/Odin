package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.render.world.OutlineUtils
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderVec
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max

object TeammatesHighlight : Module(
    "Teammate Outline",
    category = Category.DUNGEON,
    description = "Enhances visibility of your dungeon teammates and their name tags."
) {
    private val thickness: Float by NumberSetting("Line Width", 2f, 1.0, 5.0, 0.5)
    private val whenVisible: Boolean by BooleanSetting("When Visible", true, description = "Highlights teammates only when they are visible.")
    private val inBoss: Boolean by BooleanSetting("In Boss", true, description = "Highlights teammates in boss rooms.")
    private val outline: Boolean by BooleanSetting("Outline", true, description = "Highlights teammates with an outline.")

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (!shouldRender(event.entity) || !outline) return

        val teammate = DungeonUtils.teammates.find { it.entity == event.entity } ?: return

        if (!whenVisible && mc.thePlayer.canEntityBeSeen(teammate.entity)) return

        OutlineUtils.outlineEntity(event, thickness, teammate.clazz.color, true)
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        DungeonUtils.teammates.forEach { teammate ->
            if (teammate.entity?.let { shouldRender(it) } != true) return@forEach
            if (!whenVisible && mc.thePlayer.canEntityBeSeen(teammate.entity)) return@forEach

            RenderUtils.drawStringInWorld(
                "${teammate.clazz.code}${teammate.name}",
                teammate.entity.renderVec.addVec(y = 2.7),
                depthTest = false,
                increase = false,
                renderBlackBox = false,
                scale = max(0.03f, mc.thePlayer.getDistanceToEntity(teammate.entity) / 250)
            )
        }
    }

    private fun shouldRender(teammate: Entity): Boolean {
        return (inBoss || !DungeonUtils.inBoss) // boss
                && teammate != mc.thePlayer // self
                && DungeonUtils.inDungeons // in dungeon
    }
}