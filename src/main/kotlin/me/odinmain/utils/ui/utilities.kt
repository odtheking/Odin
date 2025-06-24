package me.odinmain.utils.ui

import com.github.stivais.aurora.renderer.data.Font
import com.github.stivais.aurora.renderer.data.Image
/**
 * Default font used in Odin.
 */
val regularFont = Font("Regular", "/assets/odinmain/fonts/Regular.otf")

/**
 * Minecraft's font.
 */
val mcFont = Font("Minecraft", "/assets/odinmain/fonts/Minecraft-Regular.otf")

/**
 * Utility function to get an image from a string representing a path inside /assets/odinmain/
 */
fun String.image() = Image("/assets/odinmain/$this")
