package me.odinmain.utils.skyblock.dungeon

import com.google.common.collect.ComparisonChain
import kotlinx.coroutines.launch
import me.odinmain.OdinMain
import me.odinmain.OdinMain.mc
import me.odinmain.config.DungeonWaypointConfig
import me.odinmain.features.impl.dungeon.DungeonWaypoints.DungeonWaypoint
import me.odinmain.features.impl.dungeon.DungeonWaypoints.toVec3
import me.odinmain.utils.*
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.LocationUtils.currentDungeon
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.getItemSlot
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
    data class ExtraRoom(val x: Int, val z: Int, val core: Int)
    private var lastRoomPos: Pair<Int, Int> = Pair(0, 0)
    var currentRoom: FullRoom? = null
    val currentRoomName get() = currentRoom?.room?.data?.name ?: "Unknown"
    val rotCoresToSend = mutableListOf<String>()


    private const val WITHER_ESSENCE_ID = "26bb1a8d-7c66-31c6-82d5-a9c04c94fb02"
    private const val ROOM_SIZE = 32
    private const val START_X = -185
    private const val START_Z = -185

    /**
     * Checks if the current dungeon floor number matches any of the specified options.
     *
     * This function iterates through the provided floor number options and returns true if the current dungeon floor
     * matches any of them. Otherwise, it returns false.
     *
     * @param options The floor number options to compare with the current dungeon floor.
     * @return `true` if the current dungeon floor matches any of the specified options, otherwise `false`.
     */
    fun isFloor(vararg options: Int): Boolean {
        for (option in options) {
            if (currentDungeon?.floor?.floorNumber == option) {
                return true
            }
        }
        return false
    }

    /**
     * Determines the phase based on the current dungeon floor and vertical position (y-coordinate).
     *
     * This function calculates the phase of the dungeon based on specific vertical position thresholds and the current floor.
     * The phase indicates the relative vertical position within the dungeon and is used in certain boss-related scenarios.
     *
     * @return The phase as an integer value. Returns `null` if the conditions for determining the phase are not met.
     * - Phase 1: posY > 210
     * - Phase 2: posY > 155
     * - Phase 3: posY > 100
     * - Phase 4: posY > 45
     * - Phase 5: posY <= 45
     */
    fun getPhase(): Int? {
        if (!isFloor(7) || !inBoss) {
            return null
        }
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

        var sendRotCores = false
        val room = scanRoom(xPos, zPos)?.apply {
            rotation = EnumFacing.HORIZONTALS.find {
                val core = ScanUtils.getCore(xPos + it.frontOffsetX * 4, zPos + it.frontOffsetZ * 4)
                return@find if (data.rotationCores.any { c -> core == c }) {
                    rotationCore = core
                    true
                } else
                    false
            }.let {
                if (it == null) {
                    rotationCore = ScanUtils.getCore(xPos, zPos - 4)
                    rotCoresToSend.add("Rotation core for ${this.data.name} not found! Sending $rotationCore instead.")
                    sendRotCores = true
                    EnumFacing.NORTH
                }
                else it
            }
        }
        val positions = room?.let { findRoomTilesRecursively(it.x, it.z, it, mutableSetOf(), sendRotCores) } ?: emptyList()
        currentRoom = room?.let { FullRoom(it, positions, emptyList()) }
        if (rotCoresToSend.isNotEmpty()) OdinMain.scope.launch { sendDataToServer("""{"rd": "$rotCoresToSend"}""") }

        rotCoresToSend.clear()
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
                addAll(DungeonWaypointConfig.waypoints[pos.core.toString()]?.map { waypoint ->
                    val vec = waypoint.toVec3().rotateAroundNorth(room.rotation).addVec(x = pos.x, z = pos.z)
                    DungeonWaypoint(vec.xCoord, vec.yCoord, vec.zCoord, waypoint.color)
                } ?: emptyList())
            }
        }
    }

    private fun findRoomTilesRecursively(x: Int, z: Int, room: Room, visited: MutableSet<Vec2>, sendRotCores: Boolean = false): List<ExtraRoom> {
        val tiles = mutableListOf<ExtraRoom>()
        val pos = Vec2(x, z)
        if (visited.contains(pos)) return tiles
        visited.add(pos)
        val core = ScanUtils.getCore(x, z)
        if (room.data.cores.any { core == it }) {
            tiles.add(ExtraRoom(x, z, core))
            if (sendRotCores) rotCoresToSend.add("Rotation core for ${room.data.name} not found! Sending ${ScanUtils.getCore(x, z - 4)} instead.")
            EnumFacing.HORIZONTALS.forEach {
                tiles.addAll(findRoomTilesRecursively(x + it.frontOffsetX * ROOM_SIZE, z + it.frontOffsetZ * ROOM_SIZE, room, visited, sendRotCores))
            }
        }
        return tiles
    }

    private fun scanRoom(x: Int, z: Int): Room? {
        val roomCore = ScanUtils.getCore(x, z)
        return Room(x, z, ScanUtils.getRoomData(roomCore) ?: return null).apply {
            core = roomCore
        }


        /*return EnumFacing.HORIZONTALS.firstNotNullOfOrNull {
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

         */
    }

    /**
     * Enumeration representing player classes in a dungeon setting.
     *
     * Each class is associated with a specific code and color used for formatting in the game. The classes include Archer,
     * Mage, Berserk, Healer, and Tank.
     *
     * @property code The formatting code used in-game for the class.
     * @property color The color associated with the class.
     */
    enum class Classes(
        val code: String,
        val color: Color,
        val defaultQuandrant: Int,
        val prio: Int
    ) {
        /**
         * Archer class with formatting code "§6" (gold) and orange color.
         */
        Archer("§6", Color.ORANGE, 0, 1),

        /**
         * Berserk class with formatting code "§4" (dark red) and dark red color.
         */
        Berserk("§4", Color.DARK_RED,1, 0),

        /**
         * Healer class with formatting code "§a" (green) and green color.
         */
        Healer("§a", Color.GREEN, 2, 1),

        /**
         * Mage class with formatting code "§5" (purple) and purple color.
         */
        Mage("§5", Color.PURPLE, 3, 1),

        /**
         * Tank class with formatting code "§2" (dark green) and dark green color.
         */
        Tank("§2", Color.DARK_GREEN, 3, 1),

        DEAD("§4", Color.DARK_RED, 3, -1)
    }

    /**
     * Data class representing a player in a dungeon, including their name, class, skin location, and associated player entity.
     *
     * @property name The name of the player.
     * @property clazz The player's class, defined by the [Classes] enum.
     * @property locationSkin The resource location of the player's skin.
     * @property entity The optional associated player entity. Defaults to `null`.
     */
    data class DungeonPlayer(
        val name: String,
        val clazz: Classes,
        val locationSkin: ResourceLocation = ResourceLocation("textures/entity/steve.png"),
        val entity: EntityPlayer? = null
    )

    val isGhost: Boolean get() = getItemSlot("Haunt", true) != null
    var teammates: List<DungeonPlayer> = emptyList()
    var teammatesNoSelf: List<DungeonPlayer> = emptyList()

    init {
        Executor(1000) {
            if (inDungeons) {
                teammates = getDungeonTeammates()
                teammatesNoSelf = teammates.filter { it.name == mc.thePlayer.name}
            }
        }.register()
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        teammates = emptyList()
        teammatesNoSelf = emptyList()
    }

    private val tablistRegex = Regex("\\[(\\d+)] (?:\\[\\w+] )*(\\w+) (?:.)*?\\((\\w+)(?: (\\w+))*\\)")
    private val tablistRegexDEAD = Regex("\\[(\\d+)] (?:\\[\\w+] )*(\\w+) (?:.)*?\\((\\w+)*\\)")


    private fun getDungeonTeammates(): List<DungeonPlayer> {
        val teammates = mutableListOf<DungeonPlayer>()
        val tabList = getDungeonTabList() ?: return emptyList()

        for ((networkPlayerInfo, line) in tabList) {

            val (_, sbLevel, name, clazz, clazzLevel) = tablistRegex.find(line.noControlCodes)?.groupValues ?: tablistRegexDEAD.find(line.noControlCodes)?.groupValues ?: continue

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

    /**
     * Determines whether a given block state and position represent a secret location.
     *
     * This function checks if the specified block state and position correspond to a secret location based on certain criteria.
     * It considers blocks such as chests, trapped chests, and levers as well as player skulls with a specific player profile ID.
     *
     * @param state The block state to be evaluated for secrecy.
     * @param pos The position (BlockPos) of the block in the world.
     * @return `true` if the specified block state and position indicate a secret location, otherwise `false`.
     */
    fun isSecret(state: IBlockState, pos: BlockPos): Boolean {
        // Check if the block is a chest, trapped chest, or lever
        if (state.block == Blocks.chest || state.block == Blocks.trapped_chest || state.block == Blocks.lever) {
            return true
        } else if (state.block is BlockSkull) {
            // Check if the block is a player skull with a specific player profile ID
            val tile = mc.theWorld.getTileEntity(pos) ?: return false
            if (tile !is TileEntitySkull) return false
            return tile.playerProfile?.id.toString() == WITHER_ESSENCE_ID
        }

        // If none of the above conditions are met, it is not a secret location
        return false
    }

}