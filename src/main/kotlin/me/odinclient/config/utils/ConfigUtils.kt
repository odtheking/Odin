package me.odinclient.config.utils

import me.odinclient.ModCore.Companion.mc
import java.io.File
import java.io.IOException

@Suppress("NOTHING_TO_INLINE")
inline fun ConfigFile(name: String): File =
    File(mc.mcDataDir, "config/odin/$name.json").apply {
        try {
            createNewFile()
        } catch (e: IOException) {
            println("Error creating config file.\n${e.message}")
            e.printStackTrace()
        }
    }