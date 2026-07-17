package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.commands.fetchAndDisplayCataStats
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.PartyEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.formatNumber
import com.odtheking.odin.utils.formatTime
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.loreString
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.network.hypixelapi.RequestUtils
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.PartyUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.Floor
import kotlinx.coroutines.launch
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import java.util.concurrent.ConcurrentHashMap

object BetterPartyFinder : Module(
    name = "Better Party Finder",
    description = "Provides stats when a player joins your party. Includes autokick functionality.",
) {
    private val statsDisplay by BooleanSetting("Stats display", true, desc = "Displays stats of players who join your party")
    private val sendKickLine by BooleanSetting("Send Kick Line", true, desc = "Sends a line in party chat to kick a player.").withDependency { statsDisplay }

    private val autoKickToggle by BooleanSetting("Auto Kick", desc = "Automatically kicks players who don't meet requirements.")
    private val floor by SelectorSetting("Floor", "F7", Floor.entries.mapNotNull { if (!it.isMM) it.name else null }, desc = "Determines which floor to check pb.").withDependency { autoKickToggle }
    private val mmToggle by BooleanSetting("Master Mode", true, desc = "Use master mode times").withDependency { autoKickToggle }
    private val informKicked by BooleanSetting("Inform Kicked", desc = "Informs the player why they were kicked.").withDependency { autoKickToggle || noDupe }
    private val maximumSeconds by NumberSetting("Minimum PB", 400, 0, 480, 5, desc = "Minimum amount of seconds before kicking. Set to 0 to ignore PB.", unit = "s").withDependency { autoKickToggle }
    private val secretsMin by NumberSetting("Minimum Secrets", 0, 0, 200, desc = "Secret minimum in thousands for kicking.", unit = "k").withDependency { autoKickToggle }
    private val magicalPowerReq by NumberSetting("Magical Power", 1300, 0, 2000, 20, desc = "Magical power minimum for kicking.").withDependency { autoKickToggle  }
    private val apiOffKick by BooleanSetting("Api Off Kick", false, desc = "Kicks if the player's api is off. If this setting is disabled, it will ignore the item check when players have api disabled.").withDependency { autoKickToggle }

    private val noDupe by BooleanSetting("No Dupe", false, desc = "Automatically kicks a player if another party member already has the same class.")

    private val kickCache by BooleanSetting("Kick Cache", true, desc = "Caches kicked players to automatically kick when they attempt to rejoin.").withDependency { autoKickToggle }
    private val action by ActionSetting("Clear Cache", desc = "Clears the kick list cache.") { kickedList.clear() }.withDependency { autoKickToggle && kickCache }

    //https://regex101.com/r/XYnAVm/2
    private val pfRegex = Regex("^Party Finder > (?:\\[.{1,7}])? ?(.{1,16}) joined the dungeon group! \\((\\w+) Level \\d+\\)$")
    private val queuedRegex = Regex("^Party Finder > Your party has been queued in the dungeon finder!$")
    // "Dungeon Classes" item (Catacombs Gate menu) lore, e.g. "Currently Selected: Tank"
    private val selectedClassRegex = Regex("Currently Selected: (\\w+)", RegexOption.IGNORE_CASE)

    private val kickedList = mutableSetOf<String>()

    // Tracks the class of each accepted party member for the No Dupe check.
    // Concurrent: written on the network thread (join/leave) and the coroutine thread (autokick).
    private val partyClasses = ConcurrentHashMap<String, String>()

    init {
        on<PartyEvent.Leave> { partyClasses.keys.retainAll(members.toSet()) }

        // API-free: read the client's own class off the "Dungeon Classes" item lore (Catacombs Gate menu).
        onReceive<ClientboundContainerSetSlotPacket> {
            if (!noDupe) return@onReceive
            val menu = (mc.screen as? AbstractContainerScreen<*>)?.menu ?: return@onReceive
            val self = mc.player?.gameProfile?.name ?: return@onReceive

            val cls = menu.items.firstNotNullOfOrNull { stack ->
                if (stack.isEmpty) return@firstNotNullOfOrNull null
                stack.loreString.firstNotNullOfOrNull { line ->
                    selectedClassRegex.find(line)?.groupValues?.get(1)?.let { name ->
                        DungeonClass.entries.firstOrNull { it != DungeonClass.EMPTY && it.name.equals(name, ignoreCase = true) }
                    }
                }
            } ?: return@onReceive

            if (partyClasses.put(self, cls.name) != cls.name)
                modMessage("§7No Dupe detected your class: §${cls.colorCode}${cls.name}")
        }

        on<ChatPacketEvent> {
            if (!statsDisplay && !autoKickToggle && !noDupe) return@on

            // Seed the client's own class on queue (fallback to API if the GUI never provided it).
            if (noDupe && queuedRegex.matches(value)) {
                val self = mc.player?.gameProfile?.name ?: return@on
                if (!partyClasses.containsKey(self)) scope.launch {
                    RequestUtils.getProfile(self).getOrNull()?.memberData?.dungeons?.selectedClass?.let {
                        if (partyClasses.putIfAbsent(self, it) == null) {
                            val cls = DungeonClass.entries.firstOrNull { c -> c != DungeonClass.EMPTY && c.name.equals(it, ignoreCase = true) }
                            modMessage("§7No Dupe detected your class: §${cls?.colorCode ?: 'f'}${cls?.name ?: it}")
                        }
                    }
                }
                return@on
            }

            val (name, playerClass) = pfRegex.find(value)?.destructured ?: return@on
            if (name == mc.player?.name?.string) return@on

            // Dupe detection runs synchronously so the class is recorded before the next joiner is checked.
            if (noDupe && PartyUtils.isLeader()) {
                val dupeOwner = partyClasses.entries.firstOrNull { it.key != name && it.key in PartyUtils.members && it.value.equals(playerClass, ignoreCase = true) }?.key
                if (dupeOwner != null) {
                    if (informKicked) {
                        schedule(6) { sendCommand("party kick $name") }
                        sendCommand("pc Kicked $name for duplicate class $playerClass (already taken by $dupeOwner)")
                    } else sendCommand("party kick $name")
                    modMessage("Kicking $name for duplicate class: $playerClass (already taken by $dupeOwner)")
                    return@on
                }
                partyClasses[name] = playerClass
            }

            if (!statsDisplay && !autoKickToggle) return@on

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

                    val currentProfile = profile.getOrNull()?.memberData ?: return@launch modMessage("Could not find member data for $name")

                    val dungeon = if (!mmToggle) currentProfile.dungeons.dungeonTypes.catacombs else currentProfile.dungeons.dungeonTypes.mastermode
                    if (maximumSeconds > 0) {
                        dungeon.fastestTimeSPlus["$floor"]?.let {
                            if (maximumSeconds < it / 1000)
                                kickedReasons.add(
                                    "Did not meet time req for ${if (mmToggle) "m" else "f"}$floor: ${formatTime(it.toLong())}/${formatTime(maximumSeconds * 1000L, 0)}"
                                )
                        } ?: kickedReasons.add("Couldn't confirm completion status for ${if (mmToggle) "m" else "f"}$floor")
                    }

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
                        // Was recorded synchronously for No Dupe; free the class since they're being kicked.
                        partyClasses.remove(name)
                        return@launch modMessage("Kicking $name for: \n${kickedReasons.joinToString(" \n")}")
                    }
                }
            }
        }
    }
}