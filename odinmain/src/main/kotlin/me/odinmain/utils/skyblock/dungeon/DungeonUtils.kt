package me.odinmain.utils.skyblock.dungeon

import com.google.common.collect.ComparisonChain
import me.odinmain.OdinMain.mc
import me.odinmain.config.DungeonWaypointConfig
import me.odinmain.features.impl.dungeon.DungeonWaypoints.DungeonWaypoint
import me.odinmain.features.impl.dungeon.DungeonWaypoints.toVec3
import me.odinmain.utils.*
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.ItemUtils
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.LocationUtils.currentDungeon
import me.odinmain.utils.skyblock.PlayerUtils.posY
import net.minecraft.block.BlockSkull
import net.minecraft.block.state.IBlockState
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.world.WorldSettings
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DungeonUtils {

    inline val inDungeons get() =
        LocationUtils.inSkyblock && currentDungeon != null

    inline val inBoss get() =
        currentDungeon?.inBoss ?: false

    data class Vec2(val x: Int, val z: Int)
    data class FullRoom(val room: Room, val positions: List<ExtraRoom>, var waypoints: List<DungeonWaypoint>)
    data class ExtraRoom(val x: Int, val z: Int, val rotationCore: Int)
    private var lastRoomPos: Pair<Int, Int> = Pair(0, 0)
    var currentRoom: FullRoom? = null
    val currentRoomName get() = currentRoom?.room?.data?.name ?: "Unknown"


    private const val WITHER_ESSENCE_ID = "26bb1a8d-7c66-31c6-82d5-a9c04c94fb02"
    private const val ROOM_SIZE = 32
    private const val START_X = -185
    private const val START_Z = -185

    fun isFloor(vararg options: Int): Boolean {
        for (option in options) {
            if (currentDungeon?.floor?.floorNumber == option) return true
        }
        return false
    }

    fun getPhase(): Int? {
        if (!isFloor(7) || !inBoss) return null

        return when {
            posY > 210 -> 1
            posY > 155 -> 2
            posY > 100 -> 3
            posY > 45 -> 4
            else -> 5
        }
    }

    @SubscribeEvent
    fun onMove(event: LivingEvent.LivingUpdateEvent) {
        if (mc.theWorld == null/* || !inDungeons ||  inBoss */|| !event.entity.equals(mc.thePlayer)) return
        val xPos = START_X + ((mc.thePlayer.posX + 200) / 32).toInt() * ROOM_SIZE
        val zPos = START_Z + ((mc.thePlayer.posZ + 200) / 32).toInt() * ROOM_SIZE
        if (lastRoomPos.equal(xPos, zPos) && currentRoom != null) return
        lastRoomPos = Pair(xPos, zPos)

        val room = scanRoom(xPos, zPos)
        val positions = room?.let { findRoomTilesRecursively(it.x, it.z, it, mutableSetOf()) } ?: emptyList()
        currentRoom = room?.let { FullRoom(it, positions, emptyList()) }
        setWaypoints()
    }

    /**
     * Sets the waypoints for the current room.
     * this code is way too much list manipulation, but it works
     */
    fun setWaypoints() {
        val curRoom = currentRoom ?: return
        val room = curRoom.room
        curRoom.waypoints = mutableListOf<DungeonWaypoint>().apply {
            DungeonWaypointConfig.waypoints[room.data.name]?.let { waypoints ->
                addAll(waypoints.map { waypoint ->
                    val vec = waypoint.toVec3().rotateAroundNorth(room.rotation).addVec(x = room.x, z = room.z)
                    DungeonWaypoint(vec.xCoord, vec.yCoord, vec.zCoord, waypoint.color)
                })
            }
            curRoom.positions.forEach { pos ->
                addAll(DungeonWaypointConfig.waypoints[pos.rotationCore.toString()]?.map { waypoint ->
                    val vec = waypoint.toVec3().rotateAroundNorth(room.rotation).addVec(x = pos.x, z = pos.z)
                    DungeonWaypoint(vec.xCoord, vec.yCoord, vec.zCoord, waypoint.color)
                } ?: emptyList())
            }
        }
    }

    private fun findRoomTilesRecursively(x: Int, z: Int, room: Room, visited: MutableSet<Vec2>): List<ExtraRoom> {
        val tiles = mutableListOf<ExtraRoom>()
        val pos = Vec2(x, z)
        if (visited.contains(pos)) return tiles
        visited.add(pos)
        val rotCore = ScanUtils.getCore(pos.addRotationCoords(room.rotation))
        if (room.data.rotationCores.any { rotCore == it }) {
            tiles.add(ExtraRoom(x, z, rotCore))
            EnumFacing.HORIZONTALS.forEach {
                tiles.addAll(findRoomTilesRecursively(x + it.frontOffsetX * ROOM_SIZE, z + it.frontOffsetZ * ROOM_SIZE, room, visited))
            }
        }
        return tiles
    }

    private fun scanRoom(x: Int, z: Int): Room? {
        return EnumFacing.HORIZONTALS.firstNotNullOfOrNull {
            val rotCore = ScanUtils.getCore(x + it.frontOffsetX * 4, z + it.frontOffsetZ * 4)
            Room(
                x, z,
                data = ScanUtils.getRoomDataFromRotationCore(rotCore) ?: return@firstNotNullOfOrNull null
            ).apply {
                rotationCore = rotCore
                rotation = it
                core = ScanUtils.getCore(x, z)
            }
        }
    }

    enum class Classes(
        val code: String,
        val color: Color
    ) {
        Archer("§6", Color.ORANGE),
        Mage("§5", Color.PURPLE),
        Berserk("§4", Color.DARK_RED),
        Healer("§a", Color.GREEN),
        Tank("§2", Color.DARK_GREEN)
    }
    data class DungeonPlayer(val name: String, val clazz: Classes, val locationSkin: ResourceLocation, val entity: EntityPlayer? = null)
    val isGhost: Boolean get() = ItemUtils.getItemSlot("Haunt", true) != null
    var teammates: List<DungeonPlayer> = emptyList()

    init {
        Executor(1000) {
            if (inDungeons) teammates = getDungeonTeammates()
        }.register()
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        teammates = emptyList()
    }

    private val tablistRegex = Regex("\\[(\\d+)] (?:\\[\\w+] )*(\\w+) (?:.)*?\\((\\w+)(?: (\\w+))*\\)")

    private fun getDungeonTeammates(): List<DungeonPlayer> {
        val teammates = mutableListOf<DungeonPlayer>()
        val tabList = getDungeonTabList() ?: return emptyList()

        for ((networkPlayerInfo, line) in tabList) {
            val (_, sbLevel, name, clazz, level) = tablistRegex.find(line.noControlCodes)?.groupValues ?: continue

            Classes.entries.find { it.name == clazz }?.let { foundClass ->
                mc.theWorld.getPlayerEntityByName(name)?.let { player ->
                    teammates.add(DungeonPlayer(name, foundClass, networkPlayerInfo.locationSkin, player))
                } ?: teammates.add(DungeonPlayer(name, foundClass, networkPlayerInfo.locationSkin))
            }

        }
        return teammates
    }


    private fun getDungeonTabList(): List<Pair<NetworkPlayerInfo, String>>? {
        val tabEntries = tabList
        if (tabEntries.size < 18 || !tabEntries[0].second.contains("§r§b§lParty §r§f(")) {
            return null
        }
        return tabEntries
    }

    private val tabListOrder = Comparator<NetworkPlayerInfo> { o1, o2 ->
        if (o1 == null) return@Comparator -1
        if (o2 == null) return@Comparator 0
        return@Comparator ComparisonChain.start().compareTrueFirst(
            o1.gameType != WorldSettings.GameType.SPECTATOR,
            o2.gameType != WorldSettings.GameType.SPECTATOR
        ).compare(
            o1.playerTeam?.registeredName ?: "",
            o2.playerTeam?.registeredName ?: ""
        ).compare(o1.gameProfile.name, o2.gameProfile.name).result()
    }

    private val tabList: List<Pair<NetworkPlayerInfo, String>>
        get() = (mc.thePlayer?.sendQueue?.playerInfoMap?.sortedWith(tabListOrder) ?: emptyList())
            .map { Pair(it, mc.ingameGUI.tabList.getPlayerName(it)) }

    fun isSecret(state: IBlockState, pos: BlockPos): Boolean {
        if (state.block == Blocks.chest || state.block == Blocks.trapped_chest || state.block == Blocks.lever) return true
        else if (state.block is BlockSkull) {
            val tile = mc.theWorld.getTileEntity(pos) ?: return false
            if (tile !is TileEntitySkull) return false
            return tile.playerProfile?.id.toString() == WITHER_ESSENCE_ID
        }
        return false
    }
}