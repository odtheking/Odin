package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
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
    var currentArea: String? = null


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

            currentDungeon?.setBoss()

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
    private fun getArea(): String? {
        if (mc.isSingleplayer) return "Singleplayer" // debugging
        if (!inSkyblock) return null
        val netHandlerPlayClient: NetHandlerPlayClient = mc.thePlayer?.sendQueue ?: return null
        val list = netHandlerPlayClient.playerInfoMap ?: return null

        if (currentDungeon != null)
            return if (getPhase() != null) "P${getPhase()}" else if (currentDungeon!!.inBoss) "Dungeon Boss" else "Catacombs"

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
        return if (area == null) null else area + (extraInfo ?: "")
    }

    enum class Island(val displayName: String) {
        PrivateIsland("Private Island"),
        Garden("The Garden"),
        SpiderDen("Spider's Den"),
        CrimsonIsle("Crimson Isle"),
        TheEnd("The End"),
        GoldMine("Gold Mine"),
        DeepCaverns("Deep Caverns"),
        DwarvenMines("Dwarven Mines"),
        CrystalHollows("Crystal Hollows"),
        FarmingIsland("The Farming Islands"),
        ThePark("The Park"),
        Dungeon("Catacombs"),
        DungeonHub("Dungeon Hub"),
        Hub("Hub"),
        DarkAuction("Dark Auction"),
        JerryWorkshop("Jerry's Workshop"),
        Kuudra("Kuudra"),
        Unknown("(Unknown)");
    }
}