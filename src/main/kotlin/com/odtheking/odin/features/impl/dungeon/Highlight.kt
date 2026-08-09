package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.renderBoundingBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player

object Highlight : Module(
    name = "Highlight",
    description = "Allows you to highlight selected entities."
) {
    private val highlightStar by BooleanSetting("Highlight Starred Mobs", true, desc = "Highlights starred dungeon mobs.")
    private val color by ColorSetting("Highlight color", Colors.WHITE, true, desc = "The color of the highlight.")
    private val renderStyle by SelectorSetting("Render Style", "Outline", listOf("Filled", "Outline", "Filled Outline"), desc = "Style of the box.")
    private val hideNonNames by BooleanSetting("Hide non-starred names", true, desc = "Hides names of entities that are not starred.")

    private val teammateClassGlow by BooleanSetting("Teammate Class Glow", true, desc = "Highlights dungeon teammates based on their class color.")

    private val dungeonMobSpawns = hashSetOf("Lurker", "Dreadlord", "Souleater", "Zombie", "Skeleton", "Skeletor", "Sniper", "Super Archer", "Spider", "Fels", "Withermancer", "Lost Adventurer", "Angry Archaeologist", "Frozen Adventurer")
    // https://regex101.com/r/QQf502/2
    private val starredRegex = Regex("^.*✯ .*\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?.?❤$")

    private val entities = mutableSetOf<Entity>()

    init {
        on<TickEvent.End> {
            if ((!highlightStar && !hideNonNames) || !DungeonUtils.inClear) return@on

            val entitiesToRemove = mutableListOf<Entity>()
            mc.level?.entitiesForRendering()?.forEach { e ->
                if (!e.isAlive || e !is ArmorStand) return@forEach

                val entityName = e.name.string
                if (!dungeonMobSpawns.any { it in entityName }) return@forEach

                val isStarred = starredRegex.matches(entityName)

                if (hideNonNames && e.isInvisible && !isStarred) entitiesToRemove.add(e)

                if (highlightStar && isStarred)
                    mc.level?.getEntities(e, e.boundingBox.move(0.0, -1.0, 0.0)) { isValidEntity(it) }
                        ?.firstOrNull()?.let { entities.add(it) }
            }
            entitiesToRemove.forEach { it.remove(Entity.RemovalReason.DISCARDED) }
            entities.removeIf { entity -> !entity.isAlive }
        }

        on<RenderEvent.Extract> {
            if (!highlightStar || !DungeonUtils.inClear) return@on

            entities.forEach { entity ->
                if (entity.isAlive) drawStyledBox(entity.renderBoundingBox, color, renderStyle, true)
            }
        }

        on<LevelEvent.Load> {
            entities.clear()
        }
    }

    private fun isValidEntity(entity: Entity): Boolean =
        when (entity) {
            is ArmorStand -> false
            is WitherBoss -> false
            is Player -> entity.uuid.version() == 2 && entity != mc.player
            else -> !entity.isInvisible
        }

    @JvmStatic
    fun getTeammateColor(entity: Entity): Int? {
        if (!enabled || !teammateClassGlow || !DungeonUtils.inDungeons || entity !is Player) return null
        return DungeonUtils.dungeonTeammates.find { it.name == entity.name.string }?.clazz?.color?.rgba
    }
}