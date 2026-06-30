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
    private val timeType by SelectorSetting("Time Type", "S+", listOf("S+", "S"), desc = "Whether to check S+ completion times or S completion times.").withDependency { autoKickToggle }
    private val informKicked by BooleanSetting("Inform Kicked", desc = "Informs the player why they were kicked.").withDependency { autoKickToggle }
    private val maximumSeconds by NumberSetting("Minimum PB", 400, 60, 480, 5, desc = "Minimum amount of seconds before kicking.", unit = "s").withDependency { autoKickToggle }
    private val secretsMin by NumberSetting("Minimum Secrets", 0, 0, 200, desc = "Secret minimum in thousands for kicking.", unit = "k").withDependency { autoKickToggle }
    private val magicalPowerReq by NumberSetting("Magical Power", 1300, 0, 2000, 20, desc = "Magical power minimum for kicking.").withDependency { autoKickToggle  }
    private val apiOffKick by BooleanSetting("Api Off Kick", false, desc = "Kicks if the player's api is off. If this setting is disabled, it will ignore the item check when players have api disabled.").withDependency { autoKickToggle }

    private val enchantCheck by BooleanSetting("Terminator Enchants", false, desc = "Kicks players whose Terminator is missing the required enchant levels below. 0 = ignore that enchant.").withDependency { autoKickToggle }

    private val bowEnchants = linkedMapOf(
        "power" to ("Power" to 7),
        "dragon_hunter" to ("Dragon Hunter" to 5),
        "snipe" to ("Snipe" to 3),
        "overload" to ("Overload" to 5),
        "vicious" to ("Vicious" to 5),
        "ultimate_soul_eater" to ("Ultimate Soul Eater" to 5),
        "ultimate_chimera" to ("Ultimate Chimera" to 5),
        "ultimate_fatal_tempo" to ("Ultimate Fatal Tempo" to 5),
        "ultimate_swarm" to ("Ultimate Swarm" to 5),
        "ultimate_inferno" to ("Ultimate Inferno" to 5),
        "ultimate_duplex" to ("Ultimate Duplex" to 5),
        "ultimate_rend" to ("Ultimate Rend" to 5),
        "ultimate_reiterate" to ("Ultimate Reiterate" to 5),
        "ultimate_wise" to ("Ultimate Wise" to 5),
        "cubism" to ("Cubism" to 6),
        "impaling" to ("Impaling" to 3),
        "aiming" to ("Aiming" to 5),
        "chance" to ("Chance" to 5),
        "flame" to ("Flame" to 2),
        "punch" to ("Punch" to 2),
        "piercing" to ("Piercing" to 1),
        "infinite_quiver" to ("Infinite Quiver" to 10),
        "smoldering" to ("Smoldering" to 5),
        "divine_gift" to ("Divine Gift" to 3),
        "tabasco" to ("Tabasco" to 3),
        "toxophilite" to ("Toxophilite" to 1),
    )

    private val enchantReqs: Map<String, NumberSetting<Int>> = bowEnchants.entries.associate { (key, info) ->
        key to registerSetting(
            NumberSetting(info.first, 0, 0, info.second, desc = "Minimum ${info.first} level required on the Terminator (0 = off).")
                .withDependency { autoKickToggle && enchantCheck }
        )
    }

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
                    val rank = if (timeType == 0) "S+" else "S"
                    val times = if (timeType == 0) dungeon.fastestTimeSPlus else dungeon.fastestTimeS
                    times["$floor"]?.let {
                        if (maximumSeconds < it / 1000)
                            kickedReasons.add(
                                "Did not meet $rank time req for ${if (mmToggle) "m" else "f"}$floor: ${formatTime(it.toLong())}/${formatTime(maximumSeconds * 1000L, 0)}"
                            )
                    } ?: kickedReasons.add("Couldn't confirm $rank completion status for ${if (mmToggle) "m" else "f"}$floor")

                    currentProfile.dungeons.secrets.let { currentProfile.playerStats.bloodMobKills / 4 + it }.let {
                        if (it < (secretsMin * 1000)) kickedReasons.add("Did not meet secret req: ${formatNumber(it.toString())}/${secretsMin}k")
                    }

                    if (currentProfile.inventoryApi) {
                        val mp = currentProfile.magicalPower
                        if (mp < magicalPowerReq) kickedReasons.add("Did not meet mp req: ${mp}/$magicalPowerReq")
                    } else if (apiOffKick) kickedReasons.add("Inventory API is off")

                    if (enchantCheck && currentProfile.inventoryApi) {
                        val reqs = enchantReqs.filterValues { it.value > 0 }
                        if (reqs.isNotEmpty()) {
                            val terminator = currentProfile.allItems.filterNotNull().firstOrNull { it.id == "TERMINATOR" }
                            if (terminator == null) kickedReasons.add("No Terminator found")
                            else {
                                val missing = reqs.filter { (key, setting) -> (terminator.enchantments[key] ?: 0) < setting.value }
                                if (missing.isNotEmpty())
                                    kickedReasons.add("Terminator missing: ${missing.values.joinToString(", ") { "${it.name} ${it.value}" }}")
                            }
                        }
                    }

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