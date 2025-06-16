package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PacketEvent
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.startsWithOneOf
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3BPacketScoreboardObjective
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

object LocationUtils {
    var isOnHypixel: Boolean = false
        private set
    var isInSkyblock: Boolean = false
        private set
    var currentArea: Island = Island.Unknown
        private set

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        currentArea = Island.Unknown
        isInSkyblock = false
        isOnHypixel = false
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        currentArea = Island.Unknown
        isInSkyblock = false
    }

    /**
     * Taken from [SBC](https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/utils/LocationUtils.kt)
     *
     * @author Harry282
     */
    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        if (mc.isSingleplayer) currentArea = Island.SinglePlayer

        isOnHypixel = mc.runCatching {
            !event.isLocal && ((thePlayer?.clientBrand?.contains("hypixel", true) ?: currentServerData?.serverIP?.contains("hypixel", true)) == true)
        }.getOrDefault(false)
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onPacket(event: PacketEvent.Receive) {
        when (event.packet) {
            is S3FPacketCustomPayload -> {
                if (isOnHypixel || event.packet.channelName != "MC|Brand") return
                if (event.packet.bufferData?.readStringFromBuffer(Short.MAX_VALUE.toInt())?.contains("hypixel", true) == true) isOnHypixel = true
            }

            is S38PacketPlayerListItem -> {
                if (!currentArea.isArea(Island.Unknown) || !event.packet.action.equalsOneOf(S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME, S38PacketPlayerListItem.Action.ADD_PLAYER)) return
                val area = event.packet.entries?.find { it?.displayName?.unformattedText?.startsWithOneOf("Area: ", "Dungeon: ") == true }?.displayName?.formattedText ?: return

                currentArea = Island.entries.firstOrNull { area.contains(it.displayName, true) } ?: Island.Unknown
            }

            is S3BPacketScoreboardObjective ->
                if (!isInSkyblock)
                    isInSkyblock = isOnHypixel && event.packet.func_149339_c() == "SBScoreboard"
        }
    }
}