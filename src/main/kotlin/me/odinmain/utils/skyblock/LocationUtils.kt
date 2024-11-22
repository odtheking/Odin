package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.*
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.skyblock.dungeon.Dungeon
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.Floor
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

object LocationUtils {

    private var isOnHypixel: Boolean = false
    var isInSkyblock: Boolean = false

    var currentDungeon: Dungeon? = null
        private set
    var currentArea: Island = Island.Unknown
    var kuudraTier: Int = 0

    init {
        Executor(500, "LocationUtils") {
            if (!isInSkyblock)
                isInSkyblock = isOnHypixel && mc.theWorld?.scoreboard?.getObjectiveInDisplaySlot(1)?.let { cleanSB(it.displayName).contains("SKYBLOCK") } == true

            if (currentArea.isArea(Island.Kuudra) && kuudraTier == 0)
                sidebarLines.find { cleanLine(it).contains("Kuudra's Hollow (") }?.let {
                    kuudraTier = it.substringBefore(")").lastOrNull()?.digitToIntOrNull() ?: 0 }

            if (currentArea.isArea(Island.Unknown)) currentArea = getArea()

            if ((DungeonUtils.inDungeons || currentArea.isArea(Island.SinglePlayer)) && currentDungeon == null) currentDungeon = Dungeon(getFloor() ?: return@Executor)

        }.register()
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        isOnHypixel = false
        isInSkyblock = false
        currentArea = Island.Unknown
        kuudraTier = 0
        currentDungeon = null
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        currentDungeon = null
        isInSkyblock = false
        kuudraTier = 0
        currentArea = Island.Unknown
    }

    /**
     * Taken from [SBC](https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/utils/LocationUtils.kt)
     *
     * @author Harry282
     */
    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        isOnHypixel = if (ClickGUIModule.forceHypixel) true else mc.runCatching {
            !event.isLocal && ((thePlayer?.clientBrand?.contains("hypixel", true) ?: currentServerData?.serverIP?.contains("hypixel", true)) == true)
        }.getOrDefault(false)
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        if (isOnHypixel || event.packet !is S3FPacketCustomPayload || event.packet.channelName != "MC|Brand") return
        if (event.packet.bufferData?.readStringFromBuffer(Short.MAX_VALUE.toInt())?.contains("hypixel", true) == true) isOnHypixel = true
    }

    /**
     * Returns the current area from the tab list info.
     * If no info can be found, return Island.Unknown.
     *
     * @author Aton
     */
    private fun getArea(): Island {
        if (mc.isSingleplayer) return Island.SinglePlayer
        if (!isInSkyblock) return Island.Unknown
        val netHandlerPlayClient: NetHandlerPlayClient = mc.thePlayer?.sendQueue ?: return Island.Unknown
        val list = netHandlerPlayClient.playerInfoMap ?: return Island.Unknown

        val area = list.find {
            it?.displayName?.unformattedText?.startsWith("Area: ") == true ||
                    it?.displayName?.unformattedText?.startsWith("Dungeon: ") == true
        }?.displayName?.formattedText

        return Island.entries.firstOrNull { area?.contains(it.displayName, true) == true } ?: Island.Unknown
    }

    fun getFloor(): Floor? {
        if (currentArea.isArea(Island.SinglePlayer)) return Floor.E
        for (i in sidebarLines) {
            return Floor.valueOf(Regex("The Catacombs \\((\\w+)\\)\$").find(cleanSB(i))?.groupValues?.get(1) ?: continue)
        }
        return null
    }
}