package me.odinmain

import kotlinx.coroutines.CoroutineScope
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import kotlin.coroutines.EmptyCoroutineContext

object OdinMain {
    val mc: Minecraft = Minecraft.getMinecraft()

    const val VERSION = "1.1.3"
    var NAME: String = "Odin"
    val scope = CoroutineScope(EmptyCoroutineContext)

    var display: GuiScreen? = null

}