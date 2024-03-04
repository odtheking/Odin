package me.odinmain.config.utils

import me.odinmain.OdinMain.mc
import java.io.File

@Suppress("NOTHING_TO_INLINE")
inline fun ConfigFile(name: String): File = File(mc.mcDataDir, "config/odin/$name.json")