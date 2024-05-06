package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.config.PosMessagesConfig.PosMessages
import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.concurrent.schedule

object PosMessages : Module(
    name = "Positional Messages",
    category = Category.DUNGEON,
    description = "Sends a message when you're near a certain position. /posmsg"
) {
    val onlyDungeons: Boolean by BooleanSetting("Only in Dungeons", true, description = "Only sends messages when you're in a dungeon.")


    @SubscribeEvent
    fun posMessageSend(event: PacketSentEvent) {
        if (event.packet !is C04PacketPlayerPosition || (onlyDungeons && DungeonUtils.inDungeons) || !LocationUtils.inSkyblock ) return
        PosMessages.forEach {
            if (mc.thePlayer.getDistance(it.x, it.y, it.z) <= 1) {
                if (!it.sent) Timer().schedule(it.delay) {
                    if (mc.thePlayer.getDistance(it.x, it.y, it.z) <= 1) {
                        partyMessage(it.message)
                    }
                }
                it.sent = true
            } else it.sent = false
        }
    }

    /**init {
        execute(50) {
            PosMessages.forEach {
                if (mc.thePlayer.getDistance(it.x, it.y, it.z) <= 1) {
                    if (!it.sent) Timer().schedule(it.delay) {
                        if (mc.thePlayer.getDistance(it.x, it.y, it.z) <= 1) {
                            modMessage(it.message)
                        }
                    }
                    it.sent = true
                } else it.sent = false
            }
        }
    } */
}