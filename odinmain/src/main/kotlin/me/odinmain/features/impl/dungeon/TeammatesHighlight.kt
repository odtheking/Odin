package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.distanceSquaredTo
import me.odinmain.utils.render.OutlineUtils
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.dungeonTeammatesNoSelf
import net.minecraft.entity.Entity
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeammatesHighlight : Module(
    "Teammate Highlight",
    category = Category.DUNGEON,
    description = "Enhances visibility of your dungeon teammates and their name tags."
) {
    private val showClass: Boolean by BooleanSetting("Show Class", true, description = "Shows the class of the teammate.")
    private val showOutline: Boolean by BooleanSetting("Outline", true, description = "Highlights teammates with an outline.")
    private val showName: Boolean by BooleanSetting("Name", true, description = "Highlights teammates with a name tag.")
    private val thickness: Float by NumberSetting("Line Width", 4f, 1.0, 10.0, 0.5, description = "The thickness of the outline.")
    private val whenVisible: Boolean by BooleanSetting("When Visible", true, description = "Highlights teammates only when they are visible.")
    private val inBoss: Boolean by BooleanSetting("In Boss", true, description = "Highlights teammates in boss rooms.")

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (!shouldRender(event.entity) || !showOutline) return

        val teammate = dungeonTeammatesNoSelf.find { it.entity == event.entity } ?: return

        if (!whenVisible && mc.thePlayer.canEntityBeSeen(teammate.entity)) return

        OutlineUtils.outlineEntity(event, thickness, teammate.clazz.color, true)
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons || !showName) return
        dungeonTeammatesNoSelf.forEach { teammate ->
            if (teammate.entity?.let { shouldRender(it) } != true) return@forEach
            if (!whenVisible && mc.thePlayer.canEntityBeSeen(teammate.entity)) return@forEach
            if (teammate.entity.distanceSquaredTo(mc.thePlayer) >= 2333) return@forEach

            Renderer.drawStringInWorld(
                if (showClass) "${teammate.name} §e[${teammate.clazz.name[0]}]" else teammate.name,
                teammate.entity.renderVec.addVec(y = 2.6),
                color = teammate.clazz.color,
                depth = false, renderBlackBox = false,
                scale = 0.05f
            )
        }
    }

    private fun shouldRender(teammate: Entity): Boolean {
        return (inBoss || !DungeonUtils.inBoss) // boss
                && teammate != mc.thePlayer // self
                && DungeonUtils.inDungeons // in dungeon
    }
}