package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.BoxStyle
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
    private val renderStyle by SelectorSetting("Render Style", BoxStyle.OUTLINE, desc = "Style of the box.")
    private val hideNonNames by BooleanSetting("Hide non-starred names", true, desc = "Hides names of entities that are not starred.")

    private val teammateClassGlow by BooleanSetting("Teammate Class Glow", true, desc = "Highlights dungeon teammates based on their class color.")

    private val dungeonMobSpawns = hashSetOf("Lurker", "Dreadlord", "Souleater", "Zombie", "Skeleton", "Skeletor", "Sniper", "Super Archer", "Spider", "Fels", "Withermancer", "Lost Adventurer", "Angry Archaeologist", "Frozen Adventurer")
    // https://regex101.com/r/QQf502/2
    private val starredRegex = Regex("^.*✯ .*\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?.?❤$")

    private val highlighted = HashMap<ArmorStand, Entity>()
    private val toRemove = ArrayList<Entity>()

    init {
        on<TickEvent.End> {
            if ((!highlightStar && !hideNonNames) || !DungeonUtils.inClear) return@on

            for (e in level.entitiesForRendering()) {
                if (e !is ArmorStand || !e.isAlive || e in highlighted) continue
                val name = e.customName?.string ?: continue

                val isStarred = '✯' in name && starredRegex.matches(name)
                val hide = hideNonNames && e.isInvisible && !isStarred
                if (!hide && !(highlightStar && isStarred)) continue
                if (dungeonMobSpawns.none { it in name }) continue

                if (hide) toRemove.add(e)
                else level.getEntities(e, e.boundingBox.move(0.0, -1.0, 0.0)) { isValidEntity(it) }.firstOrNull()?.let { highlighted[e] = it }
            }
            toRemove.forEach { it.remove(Entity.RemovalReason.DISCARDED) }
            toRemove.clear()
            highlighted.values.removeIf { !it.isAlive }
        }

        on<RenderExtractEvent> {
            if (!highlightStar || !DungeonUtils.inClear) return@on

            highlighted.values.forEach { entity ->
                if (entity.isAlive) drawStyledBox(entity.renderBoundingBox, color, renderStyle, true)
            }
        }

        on<LevelEvent.Load> {
            highlighted.clear()
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
        val name = entity.gameProfile.name
        return DungeonUtils.dungeonTeammates.find { it.name == name }?.clazz?.color?.rgba
    }
}