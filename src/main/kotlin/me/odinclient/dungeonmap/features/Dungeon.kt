package me.odinclient.dungeonmap.features

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.dungeonmap.core.DungeonPlayer
import me.odinclient.dungeonmap.core.map.Door
import me.odinclient.dungeonmap.core.map.Room
import me.odinclient.dungeonmap.core.map.Tile
import me.odinclient.events.ReceivePacketEvent
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinclient.utils.skyblock.dungeon.map.MapUtils
import me.odinclient.utils.skyblock.dungeon.map.MapUtils.equalsOneOf
import me.odinclient.utils.skyblock.dungeon.map.RenderUtils
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.StringUtils
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
    var inBoss = false
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

    private val entryMessages = listOf(
        "[BOSS] Bonzo: Gratz for making it this far, but I’m basically unbeatable.",
        "[BOSS] Scarf: This is where the journey ends for you, Adventurers.",
        "[BOSS] The Professor: I was burdened with terrible news recently...",
        "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!",
        "[BOSS] Livid: Welcome, you arrive right on time. I am Livid, the Master of Shadows.",
        "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!",
        "[BOSS] Maxor: WELL WELL WELL LOOK WHO’S HERE!"
    )

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
        if (hasScanned) {
            launch {
                if (!mimicFound && DungeonUtils.isFloor(6,7)) {
                    MimicDetector.findMimic()
                }
                MapUpdate.updateRooms()
                MapUpdate.updateDoors()
                getDungeonTabList()?.let {
                    MapUpdate.updatePlayers(it)
                    RunInformation.updateRunInformation(it)
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChatPacket(event: ReceivePacketEvent) {
        if (event.packet !is S02PacketChat || event.packet.type.toInt() == 2 || !inDungeons) return
        val text = StringUtils.stripControlCodes(event.packet.chatComponent.unformattedText)
        when {
            text.equalsOneOf(
                "Dungeon starts in 4 seconds.",
                "Dungeon starts in 4 seconds. Get ready!"
            ) -> MapUpdate.preloadHeads()
            text == "[NPC] Mort: Here, I found this map when I first entered the dungeon." -> MapUpdate.getPlayers()
            entryMessages.any { it == text } -> inBoss = true
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        reset()
        hasScanned = false
        inBoss = false
    }

    var playerImage = BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB)
    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!inDungeons || !config.mapEnabled) return
        playerImage = RenderUtils.createBufferedImageFromTexture(mc.textureManager.getTexture(mc.thePlayer.locationSkin).glTextureId)
        dungeonTeammates.values.forEach {
            it.bufferedImage = RenderUtils.createBufferedImageFromTexture(mc.textureManager.getTexture(it.skin).glTextureId)
        }
    }

    private val shouldScan get() =
        config.autoScan && !isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250 && inDungeons

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