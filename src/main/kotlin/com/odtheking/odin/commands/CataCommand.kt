package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.network.hypixelapi.HypixelData
import com.odtheking.odin.utils.network.hypixelapi.RequestUtils
import kotlinx.coroutines.launch
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent

val cataCommand = Commodore("cata", "catacombs", "catacomb") {
    runs { playerName: GreedyString? ->
        val name = playerName?.string ?: mc.user.name
        modMessage("§aFetching dungeon stats for §6$name§a...")
        scope.launch {
            fetchAndDisplayCataStats(RequestUtils.getProfile(name))
        }
    }
}

fun fetchAndDisplayCataStats(result: Result<HypixelData.PlayerInfo>) {
    result.fold(
        onSuccess = { playerInfo ->
            playerInfo.memberData?.let { displayCataStats(playerInfo.name, it) }
                ?: modMessage("§cNo profile found for §6${playerInfo.name}§c!")
        },
        onFailure = { modMessage("§c${it.message}") }
    )
}

private fun displayCataStats(name: String, member: HypixelData.MemberData) {
    val dungeons = member.dungeons

    Component.literal("§d§m${" ".repeat(11)}§r §b$name §d§m${" ".repeat(11)}§r\n")
        .append(buildCataSecretsWatcherLine(dungeons, member.playerStats.bloodMobKills))
        .append(buildClassLevelsLine(dungeons))
        .append(buildFloorTimesLine(dungeons.dungeonTypes, member))
        .append(Component.literal("§d§m${" ".repeat(27)}§r"))
        .let { modMessage(it, "") }
}

private fun hoverText(text: String, hover: Component) =
    Component.literal(text).withStyle { it.withHoverEvent(HoverEvent.ShowText(hover)) }

private fun buildCataSecretsWatcherLine(dungeons: HypixelData.DungeonsData, bloodMobKills: Int): Component {
    val cata = dungeons.dungeonTypes.catacombs
    val secrets = formatNumber(dungeons.secrets.toString())
    val watcherKills = formatNumber(cata.watcherKillCount.toString())

    return hoverText("§7Cata: §e${cata.level.toFixed()}",
        Component.literal("§7Catacombs Level\n§7XP: §b${formatNumber(cata.experience.toString())}"))
        .append(hoverText(" §8| §7Secrets: §e$secrets §8(§b${dungeons.avrSecrets.toFixed(1)}§8)",
            Component.literal("§7Total Secrets: §e$secrets\n§7Total Runs: §b${dungeons.totalRuns}\n§7Average: §a${dungeons.avrSecrets.toFixed()}")))
        .append(hoverText(" §8| §7Blood: §c$watcherKills",
            Component.literal("§7Total Watcher Kills: §c$watcherKills\n§7Blood Mobs Killed: §5${formatNumber(bloodMobKills.toString())}")))
        .append(Component.literal("\n"))
}

private fun buildClassLevelsLine(dungeons: HypixelData.DungeonsData) =
    Component.literal("§7Classes: ").apply {
        dungeons.classLevels.forEachIndexed { index, (dungeonClass, xp, level) ->
            val colorCode = "§${dungeonClass.colorCode}"
            if (index > 0) append(Component.literal("§8/"))
            append(hoverText("$colorCode${level.toFixed()}",
                Component.literal("$colorCode${dungeonClass.name} ${colorCode}Level\n§7XP: §b${formatNumber(xp.toString())}")))
        }
        append(hoverText(" §8(§7Avg: §a${dungeons.classAverage.toFixed(1)}§8)",
            Component.literal("§7Class Average\n§7Total Class XP: §b${formatNumber(dungeons.totalClassXp.toString())}")))
        append(Component.literal("\n"))
    }

private fun buildFloorHover(dungeonType: HypixelData.DungeonTypeData, title: String, floorPrefix: String) =
    Component.literal(title).apply {
        (1..7).forEach { floor ->
            val key = "$floor"
            val sPlusMs = dungeonType.fastestTimeSPlus[key]?.toLong() ?: 0
            val bestMs = dungeonType.fastestTimes[key]?.toLong() ?: 0
            val comps = dungeonType.tierComps[key]?.toInt() ?: 0
            val timeStr = when {
                sPlusMs > 0 -> "§a${formatTime(sPlusMs, 2)}"
                bestMs > 0 -> "§7${formatTime(bestMs, 2)}"
                else -> "§8None"
            }
            append(Component.literal("\n$floorPrefix$floor: $timeStr §8(§b$comps§8)"))
        }
    }

private fun buildFloorTimesLine(types: HypixelData.DungeonTypes, member: HypixelData.MemberData) =
    Component.literal("§7Floors: ")
        .append(hoverText("§6Normal", buildFloorHover(types.catacombs, "§6§lNormal Floors", "§eF")))
        .append(Component.literal(" §8| "))
        .append(hoverText("§cMaster", buildFloorHover(types.mastermode, "§c§lMaster Floors", "§cM")))
        .append(Component.literal(" §8| "))
        .append(hoverText("§7MP: §d${formatNumber(member.assumedMagicalPower.toString())}",
            Component.literal("§bTunings").apply {
                member.currentTunings.forEach { (tuning, value) ->
                    append(Component.literal("\n§7- §e${tuning.replace("_", " ").capitalizeWords()}§7: $value"))
                }
            }))
        .append(Component.literal("\n"))