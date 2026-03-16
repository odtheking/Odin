package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.commands.fetchAndDisplayCataStats
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.formatNumber
import com.odtheking.odin.utils.formatTime
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.network.hypixelapi.RequestUtils
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.PartyUtils
import com.odtheking.odin.utils.skyblock.dungeon.Floor
import kotlinx.coroutines.launch
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component

object BetterPartyFinder : Module(
    name = "Better Party Finder",
    description = "Provides stats when a player joins your party. Includes autokick functionality.",
) {
    private val statsDisplay by BooleanSetting("Stats display", true, desc = "Displays stats of players who join your party")
    private val sendKickLine by BooleanSetting("Send Kick Line", true, desc = "Sends a line in party chat to kick a player.").withDependency { statsDisplay }

    private val autoKickToggle by BooleanSetting("Auto Kick", desc = "Automatically kicks players who don't meet requirements.")
    private val floor by SelectorSetting("Floor", "F7", Floor.entries.mapNotNull { if (!it.isMM) it.name else null }, desc = "Determines which floor to check pb.").withDependency { autoKickToggle }
    private val mmToggle by BooleanSetting("Master Mode", true, desc = "Use master mode times").withDependency { autoKickToggle }
    private val informKicked by BooleanSetting("Inform Kicked", desc = "Informs the player why they were kicked.").withDependency { autoKickToggle }
    private val maximumSeconds by NumberSetting("Minimum PB", 400, 60, 480, 5, desc = "Minimum amount of seconds before kicking.", unit = "s").withDependency { autoKickToggle }
    private val secretsMin by NumberSetting("Minimum Secrets", 0, 0, 200, desc = "Secret minimum in thousands for kicking.", unit = "k").withDependency { autoKickToggle }
    private val magicalPowerReq by NumberSetting("Magical Power", 1300, 0, 2000, 20, desc = "Magical power minimum for kicking.").withDependency { autoKickToggle  }
    private val apiOffKick by BooleanSetting("Api Off Kick", false, desc = "Kicks if the player's api is off. If this setting is disabled, it will ignore the item check when players have api disabled.").withDependency { autoKickToggle }

    private val kickCache by BooleanSetting("Kick Cache", true, desc = "Caches kicked players to automatically kick when they attempt to rejoin.").withDependency { autoKickToggle }
    private val action by ActionSetting("Clear Cache", desc = "Clears the kick list cache.") { kickedList.clear() }.withDependency { autoKickToggle && kickCache }

    //https://regex101.com/r/XYnAVm/2
    private val pfRegex = Regex("^Party Finder > (?:\\[.{1,7}])? ?(.{1,16}) joined the dungeon group! \\(.*\\)$")

    private val kickedList = mutableSetOf<String>()

    init {
        on<ChatPacketEvent> {
            if (!statsDisplay && !autoKickToggle) return@on
            val (name) = pfRegex.find(value)?.destructured ?: return@on
            if (name == mc.player?.name?.string) return@on

            scope.launch {
                val profile = RequestUtils.getProfile(name)
                if (statsDisplay) fetchAndDisplayCataStats(profile)
                if (sendKickLine) modMessage(Component.literal("§aPress to kick $name").withStyle {
                    it.withClickEvent(ClickEvent.RunCommand("/party kick $name"))
                })

                if (autoKickToggle && PartyUtils.isLeader()) {
                    if (kickCache && name in kickedList ) {
                        sendCommand("party kick $name")
                        modMessage("Kicked $name since they have been kicked previously.")
                        return@launch
                    }

                    val kickedReasons = mutableListOf<String>()

                    val currentProfile = profile.getOrElse { return@launch modMessage(it.message) }.memberData ?: return@launch modMessage("Could not find member data for $name")

                    val dungeon = if (!mmToggle) currentProfile.dungeons.dungeonTypes.catacombs else currentProfile.dungeons.dungeonTypes.mastermode
                    dungeon.fastestTimeSPlus["$floor"]?.let {
                        if (maximumSeconds < it / 1000)
                            kickedReasons.add(
                                "Did not meet time req for ${if (mmToggle) "m" else "f"}$floor: ${formatTime(it.toLong())}/${formatTime(maximumSeconds * 1000L, 0)}"
                            )
                    } ?: kickedReasons.add("Couldn't confirm completion status for ${if (mmToggle) "m" else "f"}$floor")

                    currentProfile.dungeons.secrets.let { currentProfile.playerStats.bloodMobKills / 4 + it }.let {
                        if (it < (secretsMin * 1000)) kickedReasons.add("Did not meet secret req: ${formatNumber(it.toString())}/${secretsMin}k")
                    }

                    if (currentProfile.inventoryApi) {
                        val mp = currentProfile.magicalPower
                        if (mp < magicalPowerReq) kickedReasons.add("Did not meet mp req: ${mp}/$magicalPowerReq")
                    } else if (apiOffKick) kickedReasons.add("Inventory API is off")

                    if (kickedReasons.isNotEmpty()) {
                        if (informKicked) {
                            schedule(6) { sendCommand("party kick $name") }
                            sendCommand("pc Kicked $name for: ${kickedReasons.joinToString(", ")}")
                        } else sendCommand("party kick $name")

                        kickedList.add(name)
                        return@launch modMessage("Kicking $name for: \n${kickedReasons.joinToString(" \n")}")
                    }
                }
            }
        }
    }
}