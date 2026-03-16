package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

object HidePlayers : Module(
    name = "Hide Players",
    description = "Hides players in your vicinity."
) {
    private val onlyInDungeons by BooleanSetting("Only in Dungeons", false, desc = "Only hides players when you are in a dungeon.")
    private val hideAll by BooleanSetting("Hide all", desc = "Hides all players, regardless of distance.")
    private val distance by NumberSetting("Distance", 3f, 0, 32, .5, "The number of blocks away to hide players.", unit = " blocks").withDependency { !hideAll }

    @JvmStatic
    fun shouldRenderPlayer(entity: Entity): Boolean {
        if (!enabled || entity !is Player || entity.uuid.version() != 4 || entity == mc.player || (onlyInDungeons && !DungeonUtils.inDungeons)) return true
        return if (hideAll) false else entity.distanceToSqr(mc.player ?: return true) > (distance * distance)
    }
}