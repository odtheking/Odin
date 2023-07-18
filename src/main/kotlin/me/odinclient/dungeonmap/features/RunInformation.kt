package me.odinclient.dungeonmap.features

import net.minecraft.client.network.NetworkPlayerInfo

object RunInformation {

    var deathCount = 0
    var secretCount = 0
    var cryptsCount = 0

    private val deathsPattern = Regex("§r§a§lDeaths: §r§f\\((?<deaths>\\d+)\\)§r")
    private val secretsFoundPattern = Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r")
    private val cryptsPattern = Regex("§r Crypts: §r§6(?<crypts>\\d+)§r")

    fun updateRunInformation(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        tabEntries.forEach {
            val text = it.second
            when {
                text.contains("Deaths: ") -> {
                    val matcher = deathsPattern.find(text) ?: return@forEach
                    deathCount = matcher.groups["deaths"]?.value?.toIntOrNull() ?: deathCount
                }
                text.contains("Secrets Found: ") && !text.contains("%") -> {
                    val matcher = secretsFoundPattern.find(text) ?: return@forEach
                    secretCount = matcher.groups["secrets"]?.value?.toIntOrNull() ?: secretCount
                }
                text.contains("Crypts: ") -> {
                    val matcher = cryptsPattern.find(text) ?: return@forEach
                    cryptsCount = matcher.groups["crypts"]?.value?.toIntOrNull() ?: cryptsCount
                }
            }
        }
    }
}