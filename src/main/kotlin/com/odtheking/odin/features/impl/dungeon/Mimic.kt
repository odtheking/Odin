package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.EntityEvent
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.world.entity.monster.zombie.Zombie

object Mimic : Module(
    name = "Mimic",
    description = "Detects and announces Mimic, Prince and Bat kills in dungeons."
) {
    private val mimicMessageToggle by BooleanSetting("Send Mimic Message", true, desc = "Toggles the mimic killed message.")
    private val princeMessageToggle by BooleanSetting("Send Prince Message", true, desc = "Toggles the prince killed message.")
    private val batMessageToggle by BooleanSetting("Send Bat Message", true, desc = "Toggles the bat killed message.")

    private val princeRegex = Regex("^A Prince falls\\. \\+1 Bonus Score$")
    private val batRegex = Regex("^A Bat has been slain\\. \\+1 Bonus Score$")

    init {
        on<EntityEvent.Event> {
            if (DungeonUtils.mimicKilled || !DungeonUtils.isFloor(6, 7) || !DungeonUtils.inClear) return@on
            if (id == 3.toByte() && (entity as? Zombie)?.isBaby == true) {
                if (mimicMessageToggle) sendCommand("pc Mimic Killed!")
                DungeonListener.dungeonStats.mimicKilled = true
            }
        }

        on<MessageEvent.Chat> {
            if (DungeonUtils.inClear) when {
                !DungeonUtils.princeKilled && message.matches(princeRegex) -> {
                    if (princeMessageToggle) sendCommand("pc Prince Killed!")
                    DungeonListener.dungeonStats.princeKilled = true
                }

                !DungeonUtils.batKilled && message.matches(batRegex) -> {
                    if (batMessageToggle) sendCommand("pc Bat Killed!")
                    DungeonListener.dungeonStats.batKilled = true
                }
            }
        }
    }
}