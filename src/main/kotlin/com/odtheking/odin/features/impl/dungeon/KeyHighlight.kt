package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.EntityEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.render.drawStyledBox
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB

object KeyHighlight : Module(
    name = "Key Highlight",
    description = "Highlights wither and blood keys in dungeons."
) {
    private val announceKeySpawn by BooleanSetting("Announce Key Spawn", true, desc = "Announces when a key is spawned.")
    private val witherColor by ColorSetting("Wither Color", Colors.BLACK.withAlpha(0.8f), true, desc = "The color of the box.")
    private val bloodColor by ColorSetting("Blood Color", Colors.MINECRAFT_RED.withAlpha(0.8f), true, desc = "The color of the box.")

    private var currentKey: KeyType? = null

    init {
        on<EntityEvent.SetData> {
            if (!DungeonUtils.inClear) return@on
            val entity = entity as? ArmorStand ?: return@on
            if (currentKey?.entity == entity) return@on
            currentKey = KeyType.entries.find { it.displayName == entity.name?.string } ?: return@on
            currentKey?.entity = entity

            if (announceKeySpawn) alert("§${currentKey?.colorCode}${entity.name?.string}§7 spawned!")
        }

        on<RenderEvent.Extract> {
            if (!DungeonUtils.inClear) return@on
            if (currentKey == null || currentKey?.entity == null) return@on
            currentKey?.let { keyType ->
                if (keyType.entity?.isAlive == false) {
                    currentKey = null
                    return@on
                }
                val position = keyType.entity?.position() ?: return@on
                drawStyledBox(AABB.unitCubeFromLowerCorner(position.add(-0.5, 1.0, -0.5)), keyType.color(), 2, true)
            }
        }

        on<WorldEvent.Load> {
            currentKey = null
        }
    }

    private enum class KeyType(val displayName: String, val color: () -> Color, val colorCode: Char) {
        Wither("Wither Key", { witherColor }, '8'),
        Blood("Blood Key", { bloodColor }, 'c');

        var entity: Entity? = null
    }
}