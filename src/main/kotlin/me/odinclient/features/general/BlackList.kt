package me.odinclient.features.general

import me.odinclient.OdinClient.Companion.miscConfig

object BlackList {

    private val blackList
        get() = miscConfig.blacklist

    fun isInBlacklist(name: String) : Boolean = blackList.contains(name.lowercase())
}