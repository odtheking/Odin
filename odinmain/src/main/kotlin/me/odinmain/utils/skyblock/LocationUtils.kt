package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.DungeonEvents
import me.odinmain.events.impl.SkyblockJoinIslandEvent
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.*
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.skyblock.dungeon.Dungeon
import me.odinmain.utils.skyblock.dungeon.Floor
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

object LocationUtils {

    private var onHypixel: Boolean = false
    var inSkyblock: Boolean = false

    var currentDungeon: Dungeon? = null
    var currentArea: Island = Island.Unknown
    var kuudraTier: Int = 0

    init {
        Executor(500) {
            if (!inSkyblock)
                inSkyblock = onHypixel && mc.theWorld.scoreboard.getObjectiveInDisplaySlot(1)?.let { cleanSB(it.displayName).contains("SKYBLOCK") } ?: false

            if (currentArea.isArea(Island.Kuudra) && kuudraTier == 0) {
                getLines().find { cleanLine(it).contains("Kuudra's Hollow (") }?.let {
                    kuudraTier = it.substringBefore(")").lastOrNull()?.digitToIntOrNull() ?: 0 }
            }

            if (currentArea.isArea(Island.Unknown)) {
                val previousArea = currentArea
                currentArea = getArea()
                if (!currentArea.isArea(Island.Unknown) && previousArea != currentArea) SkyblockJoinIslandEvent(currentArea).postAndCatch()
            }

        }.register()
    }

    @SubscribeEvent
    fun onSkyblockIslandEnter(event: SkyblockJoinIslandEvent) {
        if ((event.island.isArea(Island.Dungeon) || event.island.isArea(Island.SinglePlayer)))
            createDungeon()
    }

    @SubscribeEvent
    fun onDungeonEnd(event: DungeonEvents.DungeonEndEvent) {
        currentDungeon = null
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        onHypixel = false
        inSkyblock = false
        currentArea = Island.Unknown
        SkyblockJoinIslandEvent(currentArea).postAndCatch()
        currentDungeon = null
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        inSkyblock = false
        currentArea = Island.Unknown
        currentDungeon = null
    }

    /**
     * Taken from [SBC](https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/utils/LocationUtils.kt)
     *
     * @author Harry282
     */
    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        onHypixel = if (ClickGUIModule.forceHypixel) true else mc.runCatching {
            !event.isLocal && ((thePlayer?.clientBrand?.lowercase()?.contains("hypixel")
                ?: currentServerData?.serverIP?.contains("hypixel", true)) == true)
        }.getOrDefault(false)
    }

    /**
     * Returns the current area from the tab list info.
     * If no info can be found, return Island.Unknown.
     *
     * @author Aton
     */
    private fun getArea(): Island {
        if (mc.isSingleplayer) return Island.SinglePlayer
        if (!inSkyblock) return Island.Unknown
        val netHandlerPlayClient: NetHandlerPlayClient = mc.thePlayer?.sendQueue ?: return Island.Unknown
        val list = netHandlerPlayClient.playerInfoMap ?: return Island.Unknown

        val area = list.find {
            it?.displayName?.unformattedText?.startsWith("Area: ") == true ||
                    it?.displayName?.unformattedText?.startsWith("Dungeon: ") == true
        }?.displayName?.formattedText

        return Island.entries.firstOrNull { area?.contains(it.displayName) == true } ?: Island.Unknown
    }

    private fun createDungeon(attempts: Int = 0) {
        val floor = getFloor()
        if (floor == null && attempts < 4) {
            runIn(10) { createDungeon(attempts + 1)}
        } else currentDungeon = Dungeon(floor)
    }

    private fun getFloor(): Floor? {
        for (i in sidebarLines) {
            val line = cleanSB(i)
            if (line.contains("The Catacombs (")) {
                runCatching {
                    return Floor.valueOf(line.substringAfter("(").substringBefore(")"))
                }.onFailure { modMessage("Could not get correct floor. Please report this.") }
            }
        }
        return null
    }
}

