package me.odinmain

import kotlinx.coroutines.CoroutineScope
import me.odinmain.utils.render.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import kotlin.coroutines.EmptyCoroutineContext

object OdinMain {
    val mc: Minecraft = Minecraft.getMinecraft()

    const val VERSION = "1.2.0"
    const val NAME: String = "Odin"
    val scope = CoroutineScope(EmptyCoroutineContext)

    var display: GuiScreen? = null
    var onLegitVersion = true

    object MapColors {
        var bloodColor = Color.WHITE
        var miniBossColor = Color.WHITE
        var entranceColor = Color.WHITE
        var fairyColor = Color.WHITE
        var puzzleColor = Color.WHITE
        var rareColor = Color.WHITE
        var trapColor = Color.WHITE
        var mimicRoomColor = Color.WHITE
        var roomColor = Color.WHITE
        var bloodDoorColor = Color.WHITE
        var entranceDoorColor = Color.WHITE
        var openWitherDoorColor = Color.WHITE
        var witherDoorColor = Color.WHITE
        var roomDoorColor = Color.WHITE
    }
}