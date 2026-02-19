package com.odtheking.odin.utils.skyblock

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.PlayerTeamEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.noControlCodes
import com.odtheking.odin.utils.startsWithOneOf
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket

object LocationUtils {

    var isInSkyblock: Boolean = false
        private set

    var currentArea: Island = Island.Unknown
        private set

    var lobbyId: String? = null
        private set

    private val lobbyRegex = Regex("\\d\\d/\\d\\d/\\d\\d (\\w{0,6}) *")

    init {
        onReceive<ClientboundPlayerInfoUpdatePacket> {
            if (!isCurrentArea(Island.Unknown) || actions().none { it.equalsOneOf(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME) }) return@onReceive
            val area = entries()?.find { it?.displayName?.string?.startsWithOneOf("Area: ", "Dungeon: ") == true }?.displayName?.string ?: return@onReceive
            currentArea = Island.entries.firstOrNull { area.contains(it.displayName, true) } ?: Island.Unknown
        }

        onReceive<ClientboundSetObjectivePacket> {
            if (!isInSkyblock) isInSkyblock = objectiveName == "SBScoreboard"
        }

        on<PlayerTeamEvent.UpdateParameters> {
            if (!isCurrentArea(Island.Unknown)) return@on
            val text = (team.playerPrefix.string + team.playerSuffix.string).noControlCodes

            lobbyRegex.find(text)?.groupValues?.get(1)?.let { lobbyId = it }
        }

        on<WorldEvent.Load> {
            currentArea = if (mc.isSingleplayer) Island.SinglePlayer else Island.Unknown
            isInSkyblock = false
            lobbyId = null
        }
    }

    fun isCurrentArea(vararg areas: Island): Boolean =
        if (currentArea == Island.SinglePlayer) true
        else areas.any { currentArea == it }
}