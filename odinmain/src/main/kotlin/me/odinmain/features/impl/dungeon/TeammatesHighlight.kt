package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.RenderEntityOutlineEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderVec
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.teammatesNoSelf
import net.minecraft.entity.Entity
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TeammatesHighlight : Module(
    "Teammate Highlight",
    category = Category.DUNGEON,
    description = "Enhances visibility of your dungeon teammates and their name tags."
) {
    private val outline: Boolean by BooleanSetting("Outline", true, description = "Highlights teammates with an outline.")

    @SubscribeEvent
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent) {
        if (event.type !== RenderEntityOutlineEvent.Type.XRAY || !DungeonUtils.inDungeons || !outline) return

        event.queueEntitiesToOutline { entity -> getTeammates(entity) }
    }

    @SubscribeEvent
    fun handleNames(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons) return

        teammatesNoSelf.forEach {
            if (it.entity == null || it.name == mc.thePlayer.name) return@forEach
            RenderUtils.drawStringInWorld(
                it.name, it.entity.renderVec.addVec(y = 2.6),
                color = it.clazz.color.rgba,
                depthTest = false, increase = false, renderBlackBox = false,
                scale = 0.05f
            )
        }
    }

    private fun getTeammates(entity: Entity): Int? {
        val teammate = teammatesNoSelf.find { it.entity == entity } ?: return null

        return teammate.clazz.color.rgba
    }
}