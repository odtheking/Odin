package me.odinmain.features.impl.dungeon

import com.github.stivais.commodore.utils.GreedyString
import com.sun.xml.internal.ws.api.message.Message
import kotlinx.coroutines.Delay
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.ListSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.util.Vec3

object PosMessages : Module(
    name = "Positional Messages",
    category = Category.DUNGEON,
    description = "Sends a message when youre near a certain position. /posmsg add"
) {
    val posMessages: MutableList<posMessagesData> by ListSetting("Pos Messages", mutableListOf())

    data class posMessagesData(val x: Double, val y: Double, val z: Double, val delay: Long, val message: GreedyString)

    var atPos = false

    init {
        execute(50) {
            posMessages.forEach {
                if (mc.thePlayer.getDistance(it.x, it.y, it.z,) <= 1) {
                    val timer = Clock(it.delay)
                    if (!atPos) {
                        timer.update()
                        atPos = true
                    }
                    if (timer.hasTimePassed()) partyMessage(it.message)
                } else {
                    atPos = false
                }
            }
        }
    }
}