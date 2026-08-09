package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonListener
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket
import net.minecraft.world.entity.monster.zombie.Zombie
import net.minecraft.world.level.Level

object Mimic : Module(
    name = "Mimic",
    description = "Announces Mimic, Prince and Bat kills in dungeons."
) {
    private val mimicMessageToggle by BooleanSetting("Send Mimic Message", true, desc = "Toggles the mimic killed message.")
    private val reset by ActionSetting("Mimic Killed", desc = "Sends Mimic killed message in party chat.") { mimicKilled() }

    private val princeMessageToggle by BooleanSetting("Send Prince Message", true, desc = "Toggles the prince killed message.")
    private val princeReset by ActionSetting("Prince Killed", desc = "Sends Prince killed message in party chat.") { princeKilled() }

    private val batMessageToggle by BooleanSetting("Send Bat Message", true, desc = "Toggles the bat killed message.")
    private val batReset by ActionSetting("Bat Killed", desc = "Sends Bat killed message in party chat.") { batKilled() }

    private val princeRegex = Regex("^A Prince falls\\. \\+1 Bonus Score$")
    private val batRegex = Regex("^A Bat has been slain\\. \\+1 Bonus Score$")

    init {
        onReceive<ClientboundEntityEventPacket> {
            if (!DungeonUtils.isFloor(6, 7) || DungeonUtils.inBoss || DungeonUtils.mimicKilled) return@onReceive
            if (eventId != (3).toByte()) return@onReceive

            val entity = getEntity(mc.level as Level) as? Zombie ?: return@onReceive
            if (!entity.isBaby) return@onReceive

            mimicKilled()
        }

        on<ChatPacketEvent> {
            if (value.matches(princeRegex)) princeKilled()
            if (value.matches(batRegex)) batKilled()
        }
    }

    private fun mimicKilled() {
        if (DungeonUtils.mimicKilled || !DungeonUtils.inClear) return
        if (mimicMessageToggle) sendCommand("pc Mimic Killed!")
        DungeonListener.dungeonStats.mimicKilled = true
    }

    private fun princeKilled() {
        if (DungeonUtils.princeKilled || !DungeonUtils.inClear) return
        if (princeMessageToggle) sendCommand("pc Prince Killed!")
        DungeonListener.dungeonStats.princeKilled = true
    }

    private fun batKilled() {
        if (DungeonUtils.batKilled || !DungeonUtils.inClear) return
        if (batMessageToggle) sendCommand("pc Bat Killed!")
        DungeonListener.dungeonStats.batKilled = true
    }
}