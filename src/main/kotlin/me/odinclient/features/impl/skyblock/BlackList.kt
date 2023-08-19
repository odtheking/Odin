package me.odinclient.features.impl.skyblock

import me.odinclient.OdinClient.Companion.miscConfig

object BlackList {

    private val blackList
        get() = miscConfig.blacklist

    fun isInBlacklist(name: String) : Boolean = blackList.contains(name.lowercase())
}