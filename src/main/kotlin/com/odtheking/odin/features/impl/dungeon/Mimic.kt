package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
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
    description = "Highlights and announces mimic kills in dungeons."
) {
    private val mimicMessageToggle by BooleanSetting("Toggle Mimic Message", true, desc = "Toggles the mimic killed message.")
    val mimicMessage by StringSetting("Mimic Message", "Mimic Killed!", 128, desc = "Message sent when mimic is detected as killed.").withDependency { mimicMessageToggle }
    private val reset by ActionSetting("Mimic Killed", desc = "Sends Mimic killed message in party chat.") { mimicKilled() }

    private val princeMessageToggle by BooleanSetting("Send Prince Message", true, desc = "Toggles the prince killed message.")
    val princeMessage by StringSetting("Prince Message", "Prince Killed!", 128, desc = "Message sent when prince is detected as killed.").withDependency { princeMessageToggle }
    private val princeReset by ActionSetting("Prince Killed", desc = "Sends Prince killed message in party chat.") { princeKilled() }

    private val princeRegex = Regex("^A Prince falls\\. \\+1 Bonus Score$")

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
        }
    }

    private fun mimicKilled() {
        if (DungeonUtils.mimicKilled || DungeonUtils.inBoss) return
        if (mimicMessageToggle) sendCommand("pc $mimicMessage")
        DungeonListener.dungeonStats.mimicKilled = true
    }

    private fun princeKilled() {
        if (DungeonUtils.princeKilled || !DungeonUtils.inClear) return
        if (princeMessageToggle) sendCommand("pc $princeMessage")
        DungeonListener.dungeonStats.princeKilled = true
    }
}