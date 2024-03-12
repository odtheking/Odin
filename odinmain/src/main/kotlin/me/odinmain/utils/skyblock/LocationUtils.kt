package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.cleanLine
import me.odinmain.utils.cleanSB
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.getLines
import me.odinmain.utils.sidebarLines
import me.odinmain.utils.skyblock.dungeon.Dungeon
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getPhase
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

object LocationUtils {

    private var onHypixel: Boolean = false
    var inSkyblock: Boolean = false

    var currentDungeon: Dungeon? = null
    var currentArea: Island? = null
    var kuudraTier: Int = 0

    init {
        Executor(500) {
            if (!inSkyblock) {
                inSkyblock = onHypixel && mc.theWorld.scoreboard.getObjectiveInDisplaySlot(1)
                    ?.let { cleanSB(it.displayName).contains("SKYBLOCK") } ?: false
            }

            if (currentDungeon == null) {
                if (inSkyblock && sidebarLines.any {
                        cleanSB(it).run {
                            (contains("The Catacombs") && !contains("Queue")) || contains("Dungeon Cleared:")
                        }
                    }
                ) currentDungeon = Dungeon()
            }

            if (currentArea == null || currentDungeon != null) {
                currentArea = getArea()
            }

            if (currentArea == Island.Kuudra && kuudraTier == 0) {
                getLines().find {
                    cleanLine(it).contains("Kuudra's Hollow (")
                }?.let {
                    val line = it.substringBefore(")")
                    kuudraTier = line.lastOrNull()?.digitToIntOrNull() ?: 0
                }
            }

        }.register()
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        onHypixel = false
        inSkyblock = false
        currentArea = null
        currentDungeon = null
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        inSkyblock = false
        currentArea = null
        currentDungeon = null
    }

    /**
     * Taken from [SBC](https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/utils/LocationUtils.kt)
     *
     * @author Harry282
     */
    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        if (ClickGUIModule.forceHypixel) {
            onHypixel = true
            return
        }
        onHypixel = mc.runCatching {
            !event.isLocal && ((thePlayer?.clientBrand?.lowercase()?.contains("hypixel")
                ?: currentServerData?.serverIP?.contains("hypixel", true)) == true)
        }.getOrDefault(false)
    }


    /**
     * Returns the current area from the tab list info.
     * If no info can be found return null.
     *
     * @author Aton
     */
    private fun getArea(): Island? {
        if (mc.isSingleplayer) return Island.SinglePlayer // debugging
        if (!inSkyblock) return null
        val netHandlerPlayClient: NetHandlerPlayClient = mc.thePlayer?.sendQueue ?: return null
        val list = netHandlerPlayClient.playerInfoMap ?: return null

        if (currentDungeon != null)
            return if (getPhase() != null) getPhase() else if (currentDungeon!!.inBoss) Island.DungeonBoss else Island.Dungeon

        var area: String? = null
        var extraInfo: String? = null

        for (entry in list) {
            val areaText = entry?.displayName?.unformattedText ?: continue

            if (areaText.startsWith("Area: ")) {
                area = areaText.substringAfter("Area: ")
                if (!area.contains("Private Island")) break
            }
            if (areaText.contains("Owner:")) extraInfo = areaText.substringAfter("Owner:")
        }
        return if (area == null) null else Island.entries.firstOrNull { area.contains(it.displayName) } ?: Island.Unknown.also { println("Unknown area: $area") }
    }
}