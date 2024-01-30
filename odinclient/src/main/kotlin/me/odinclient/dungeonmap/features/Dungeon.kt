package me.odinclient.dungeonmap.features

import kotlinx.coroutines.launch
import me.odinclient.dungeonmap.core.DungeonPlayer
import me.odinclient.features.impl.dungeon.MapModule
import me.odinclient.utils.skyblock.dungeon.map.MapRenderUtils
import me.odinclient.utils.skyblock.dungeon.map.MapUtils
import me.odinmain.OdinMain.mc
import me.odinmain.OdinMain.scope
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.dungeon.Room
import me.odinmain.utils.skyblock.dungeon.Tile
import me.odinmain.utils.skyblock.dungeon.Unknown
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.image.BufferedImage

object Dungeon {

    val dungeonTeammates = mutableMapOf<String, DungeonPlayer>()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !inDungeons) return

        if (DungeonScan.shouldScan) {
            scope.launch { DungeonScan.scan() }
        }

        if (DungeonScan.isScanning) return

        if (shouldSearchMimic()) {
            MimicDetector.findMimic()?.let {
                if (MapModule.mimicMessage && MapModule.enabled) modMessage("§7Mimic Room: §c$it")
                Info.mimicFound = true
            }
        }

        if (!MapUtils.calibrated) {
            MapUtils.calibrated = MapUtils.calibrateMap()
        }

        MapUpdate.updateRooms()

        MapUtils.getDungeonTabList()?.let {
            MapUpdate.updatePlayers(it)
            RunInformation.updateRunInformation(it)
        }
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatPacketEvent) {
        when (event.message) {
            "Starting in 4 seconds." -> MapUpdate.preloadHeads()
            "[NPC] Mort: Here, I found this map when I first entered the dungeon." -> {
                MapUpdate.getPlayers()
                Info.startTime = System.currentTimeMillis()
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        reset()
    }

    var playerImage = BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB)
    private var hasCreatedImages = false
    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!inDungeons || !MapModule.enabled || hasCreatedImages) return
        playerImage = MapRenderUtils.createBufferedImageFromTexture(mc.textureManager.getTexture(mc.thePlayer.locationSkin).glTextureId)
        dungeonTeammates.forEach { (_, player) ->
            player.bufferedImage = MapRenderUtils.createBufferedImageFromTexture(mc.textureManager.getTexture(player.skin).glTextureId)
        }
        hasCreatedImages = true
    }

    fun reset() {
        Info.reset()
        dungeonTeammates.clear()
        MapUtils.calibrated = false
        DungeonScan.hasScanned = false
    }

    fun shouldSearchMimic() = DungeonScan.hasScanned && !Info.mimicFound && DungeonUtils.isFloor(6, 7)

    object Info {
        // 6 x 6 room grid, 11 x 11 with connections
        val dungeonList = Array<Tile>(121) { Unknown(0, 0) }
        val uniqueRooms = mutableListOf<Room>()
        val puzzles = mutableListOf<String>()

        var trapType = ""
        var witherDoors = 0
        var cryptCount = 0
        var secretCount = 0
        var mimicFound = false

        var startTime = 0L
        fun reset() {
            dungeonList.fill(Unknown(0, 0))
            uniqueRooms.clear()
            puzzles.clear()

            trapType = ""
            witherDoors = 0
            cryptCount = 0
            secretCount = 0
            mimicFound = false

            startTime = 0L
        }
    }
}