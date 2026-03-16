package com.odtheking.odin.commands

import com.github.stivais.commodore.Commodore
import com.github.stivais.commodore.utils.GreedyString
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.network.hypixelapi.HypixelData
import com.odtheking.odin.utils.network.hypixelapi.RequestUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import kotlinx.coroutines.launch
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent

val cataCommand = Commodore("cata", "catacombs", "catacomb") {
    runs { playerName: GreedyString? ->
        val name = playerName?.string ?: mc.user.name ?: return@runs modMessage("§cUnable to get player name!")
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
        onFailure = { modMessage("§cFailed to fetch stats: ${it.message}") }
    )
}

private fun displayCataStats(name: String, member: HypixelData.MemberData) {
    with(member.dungeons) {
        val cata = dungeonTypes.catacombs
        val mm = dungeonTypes.mastermode
        val cataLevel = calculateDungeonLevel(cata.experience)
        val classLevels = DungeonClass.entries
            .mapNotNull { if (it != DungeonClass.Unknown) calculateDungeonLevel(classes[it.name.lowercase()]?.experience ?: 0.0) else null }

        Component.literal("§d§m${" ".repeat(11)}§r §b$name §d§m${" ".repeat(11)}§r\n")
            .append(buildCataSecretsWatcherLine(cataLevel, cata.experience, secrets, avrSecrets, totalRuns,
                member.playerStats.bloodMobKills, cata.watcherKills.values.sum().toInt()))
            .append(buildClassLevelsLine(classes, classLevels.average()))
            .append(buildFloorTimesLine(cata, mm, member.assumedMagicalPower, member.tunings))
            .apply {
                getArmorPieces(member).takeIf { it.isNotEmpty() }?.let { append(buildArmorLine(it)) }
                checkMissingItems(member).takeIf { it.isNotEmpty() }?.let { append(buildMissingItemsLine(it)) }
            }
            .append(Component.literal("§d§m${" ".repeat(27)}§r"))
            .let { modMessage(it, "") }
    }
}

private fun buildCataSecretsWatcherLine(cataLevel: Double, cataXp: Double, secrets: Long, secretAvg: Double, totalRuns: Int, watcherKills: Int, bloodMobKills: Int) =
    Component.literal("§7Cata: §e${cataLevel.toFixed()}")
        .withStyle { it.withHoverEvent(HoverEvent.ShowText(
            Component.literal("§7Catacombs Level\n§7XP: §b${formatNumber(cataXp.toString())}")
        ))}
    .append(Component.literal(" §8| §7Secrets: §e${formatNumber(secrets.toString())} §8(§b${secretAvg.toFixed(1)}§8)")
        .withStyle { it.withHoverEvent(HoverEvent.ShowText(
            Component.literal("§7Total Secrets: §e${formatNumber(secrets.toString())}\n§7Total Runs: §b$totalRuns\n§7Average: §a${secretAvg.toFixed()}")
        ))})
    .append(Component.literal(" §8| §7Blood: §c${formatNumber(watcherKills.toString())}")
        .withStyle { it.withHoverEvent(HoverEvent.ShowText(
            Component.literal("§7Total Watcher Kills: §c${formatNumber(watcherKills.toString())}\n§7Blood Mobs Killed: §5${formatNumber(bloodMobKills.toString())}")
        ))})
    .append(Component.literal("\n"))

private fun buildClassLevelsLine(classes: Map<String, HypixelData.ClassData>, classAvg: Double) =
    Component.literal("§7Classes: ").apply {
        var totalClassXp = 0.0

        DungeonClass.entries.forEach { dungeonClass ->
            if (dungeonClass == DungeonClass.Unknown) return@forEach
            val className = dungeonClass.name.lowercase()
            val level = calculateDungeonLevel(classes[className]?.experience ?: 0.0).toFixed()
            val xp = classes[className]?.experience ?: 0.0
            totalClassXp += xp
            val colorCode = "§${dungeonClass.colorCode}"

            append(Component.literal("$colorCode$level")
                .withStyle { it.withHoverEvent(HoverEvent.ShowText(
                    Component.literal("$colorCode${dungeonClass.name} ${colorCode}Level\n§7XP: §b${formatNumber(xp.toString())}")
                ))})
            if (dungeonClass != DungeonClass.Tank) append(Component.literal("§8/"))
        }
        append(Component.literal(" §8(§7Avg: §a${classAvg.toFixed(1)}§8)")
            .withStyle { it.withHoverEvent(HoverEvent.ShowText(
                Component.literal("§7Class Average\n§7Total Class XP: §b${formatNumber(totalClassXp.toString())}")
            )) })
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

private fun buildFloorTimesLine(cata: HypixelData.DungeonTypeData, mm: HypixelData.DungeonTypeData, magicalPower: Int, tunings: List<String>) =
    Component.literal("§7Floors: ")
        .append(Component.literal("§6Normal")
            .withStyle { it.withHoverEvent(HoverEvent.ShowText(buildFloorHover(cata, "§6§lNormal Floors", "§eF"))) })
        .append(Component.literal(" §8| "))
        .append(Component.literal("§cMaster")
            .withStyle { it.withHoverEvent(HoverEvent.ShowText(buildFloorHover(mm, "§c§lMaster Floors", "§cM"))) })
        .append(Component.literal(" §8| "))
        .append(Component.literal("§7MP: §d${formatNumber(magicalPower.toString())}")
            .withStyle { it.withHoverEvent(HoverEvent.ShowText(
                Component.literal("§bTunings").apply {
                    tunings.forEach {
                        append(Component.literal("\n§7- §e$it"))
                    }
                }
            )) })
        .append(Component.literal("\n"))

private data class ArmorPiece(val slot: String, val itemStack: HypixelData.ItemData?)
private data class MissingItem(val name: String, val shortName: String)

private fun getArmorPieces(member: HypixelData.MemberData) = member.inventory?.invArmor?.itemStacks
    ?.takeIf { it.size >= 4 }
    ?.let { listOfNotNull(
        it[3]?.let { stack -> ArmorPiece("⛑", stack) },
        it[2]?.let { stack -> ArmorPiece("\uD83D\uDEE1", stack) },
        it[1]?.let { stack -> ArmorPiece("\uD83D\uDC56", stack) },
        it[0]?.let { stack -> ArmorPiece("\uD83D\uDC62", stack) }
    )} ?: emptyList()

private fun buildArmorLine(armorPieces: List<ArmorPiece>) = Component.literal("§7Armor: ").apply {
    armorPieces.forEachIndexed { index, (slot, itemStack) ->
        val displayName = itemStack?.name ?: "§8Empty"

        append(Component.literal(slot).withStyle { style ->
            itemStack?.let {
                val hover = Component.empty().append(Component.literal("$displayName\n"))
                it.lore.forEach { loreLine -> hover.append(loreLine).append(Component.literal("\n")) }
                style.withHoverEvent(HoverEvent.ShowText(hover))
            } ?: style.withHoverEvent(HoverEvent.ShowText(Component.literal("§8Empty Slot")))
        })

        if (index < armorPieces.lastIndex) append(Component.literal(" §8| "))
    }
    append(Component.literal("\n"))
}

private fun hasItem(member: HypixelData.MemberData, vararg itemId: String) =
    member.allItems.any { item -> itemId.any { item?.id?.contains(it) == true } }

private fun checkMissingItems(member: HypixelData.MemberData) = buildList {
    if (!hasItem(member, "HYPERION", "ASTRAEA", "SCYLLA", "VALKYRIE")) add(MissingItem("Blade", "§5Wither Blade"))
    if (!hasItem(member, "TERMINATOR")) add(MissingItem("Terminator", "§cTerm"))
    if (!member.pets.pets.any { it.type.equals("GOLDEN_DRAGON", true) })
        add(MissingItem("Golden Dragon", "§6GDrag"))
    if (!member.pets.pets.any { it.type.equals("ENDER_DRAGON", true) })
        add(MissingItem("Ender Dragon", "§5EDrag"))
}

private fun buildMissingItemsLine(missing: List<MissingItem>) = Component.literal("§7Missing: ").apply {
    missing.forEachIndexed { index, item ->
        append(Component.literal("§c✖ ${item.shortName}")
            .withStyle { it.withHoverEvent(HoverEvent.ShowText(Component.literal("§cMissing ${item.name}"))) })
        if (index < missing.lastIndex) append(Component.literal(" §8| "))
    }
    append(Component.literal("\n"))
}