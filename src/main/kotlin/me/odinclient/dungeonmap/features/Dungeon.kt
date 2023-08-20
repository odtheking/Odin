package me.odinclient.dungeonmap.features

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.dungeonmap.core.DungeonPlayer
import me.odinclient.dungeonmap.core.map.Door
import me.odinclient.dungeonmap.core.map.Room
import me.odinclient.dungeonmap.core.map.Tile
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.features.impl.dungeon.MapModule
import me.odinclient.utils.Utils.equalsOneOf
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinclient.utils.skyblock.dungeon.map.MapRenderUtils
import me.odinclient.utils.skyblock.dungeon.map.MapUtils
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.image.BufferedImage

object Dungeon {

    const val roomSize = 32
    const val startX = -185
    const val startZ = -185

    private var lastScanTime: Long = 0
    private var isScanning = false
    var hasScanned = false

    // 6 x 6 room grid, 11 x 11 with connections
    val dungeonList = Array<Tile>(121) { Door(0, 0) }
    val uniqueRooms = mutableListOf<Room>()
    val rooms = mutableListOf<Room>()
    val doors = mutableMapOf<Door, Pair<Int, Int>>()
    var mimicFound = false

    val dungeonTeammates = mutableMapOf<String, DungeonPlayer>()

    // Used for chat info
    val puzzles = mutableListOf<String>()
    var trapType = ""
    var witherDoors = 0
    var secretCount = 0

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) = runBlocking {
        if (event.phase != TickEvent.Phase.START || !inDungeons) return@runBlocking
        if (shouldScan) {
            lastScanTime = System.currentTimeMillis()
            launch {
                isScanning = true
                DungeonScan.scanDungeon()
                isScanning = false
            }
        }
        getDungeonTabList()?.let {
            MapUpdate.updatePlayers(it)
            RunInformation.updateRunInformation(it)
        }
        if (hasScanned) {
            launch {
                if (!mimicFound && DungeonUtils.isFloor(6,7)) {
                    MimicDetector.findMimic()
                }
                MapUpdate.updateRooms()
                MapUpdate.updateDoors()
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChatPacket(event: ChatPacketEvent) {
        if (!inDungeons) return
        when {
            event.message.equalsOneOf(
                "Dungeon starts in 4 seconds.",
                "Dungeon starts in 4 seconds. Get ready!"
            ) -> MapUpdate.preloadHeads()
            event.message == "[NPC] Mort: Here, I found this map when I first entered the dungeon." -> {
                MapUpdate.getPlayers()
                MapUtils.startCorner = when {
                    DungeonUtils.isFloor(1) -> Pair(22, 11)
                    DungeonUtils.isFloor(2, 3) -> Pair(11, 11)
                    DungeonUtils.isFloor(4) -> Pair(5, 16)
                    else -> Pair(5, 5)
                }

                MapUtils.roomSize = if (DungeonUtils.isFloor(1, 2, 3)) 18 else 16

                MapUtils.coordMultiplier = (MapUtils.roomSize + 4.0) / roomSize
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        reset()
        hasScanned = false
        hasCreatedImages = false
    }

    var playerImage = BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB)
    private var hasCreatedImages = false
    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!inDungeons || !MapModule.enabled || hasCreatedImages) return
        playerImage = MapRenderUtils.createBufferedImageFromTexture(mc.textureManager.getTexture(mc.thePlayer.locationSkin).glTextureId)
        dungeonTeammates.values.forEach {
            it.bufferedImage = MapRenderUtils.createBufferedImageFromTexture(mc.textureManager.getTexture(it.skin).glTextureId)
        }
        hasCreatedImages = true
    }

    private val shouldScan get() =
        MapModule.autoScan && !isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250 && inDungeons

    fun getDungeonTabList(): List<Pair<NetworkPlayerInfo, String>>? {
        val tabEntries = MapUtils.tabList
        if (tabEntries.size < 18 || !tabEntries[0].second.contains("§r§b§lParty §r§f(")) {
            return null
        }
        return tabEntries
    }


    fun reset() {
        dungeonTeammates.clear()

        dungeonList.fill(Door(0, 0))
        uniqueRooms.clear()
        rooms.clear()
        doors.clear()
        mimicFound = false

        puzzles.clear()
        trapType = ""
        witherDoors = 0
        secretCount = 0
    }
}