package me.odinmain.utils.skyblock.dungeon

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PacketEvent
import me.odinmain.events.impl.RoomEnterEvent
import me.odinmain.features.impl.dungeon.MapInfo.togglePaul
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.LocationUtils.currentDungeon
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.dungeon.tiles.Room
import me.odinmain.utils.skyblock.getItemSlot
import net.minecraft.block.BlockSkull
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToLong

object DungeonUtils {

    inline val inDungeons: Boolean
        get() = LocationUtils.currentArea.isArea(Island.Dungeon)

    inline val floor: Floor
        get() = currentDungeon?.floor ?: Floor.E

    inline val inBoss: Boolean
        get() = currentDungeon?.inBoss == true

    inline val secretCount: Int
        get() = currentDungeon?.dungeonStats?.secretsFound ?: 0

    inline val knownSecrets: Int
        get() = currentDungeon?.dungeonStats?.knownSecrets ?: 0

    inline val secretPercentage: Float
        get() = currentDungeon?.dungeonStats?.secretsPercent ?: 0f

    inline val totalSecrets: Int
        get() = if (secretCount == 0 || secretPercentage == 0f) 0 else floor(100 / secretPercentage * secretCount + 0.5).toInt()

    inline val deathCount: Int
        get() = currentDungeon?.dungeonStats?.deaths ?: 0

    inline val cryptCount: Int
        get() = currentDungeon?.dungeonStats?.crypts ?: 0

    inline val openRoomCount: Int
        get() = currentDungeon?.dungeonStats?.openedRooms ?: 0

    inline val completedRoomCount: Int
        get() = currentDungeon?.dungeonStats?.completedRooms ?: 0

    inline val percentCleared: Int
        get() = currentDungeon?.dungeonStats?.percentCleared ?: 0

    inline val totalRooms: Int
        get() = if (completedRoomCount == 0 || percentCleared == 0) 0 else floor((completedRoomCount / (percentCleared * 0.01).toFloat()) + 0.4).toInt()

    inline val puzzles: List<Puzzle>
        get() = currentDungeon?.puzzles.orEmpty()

    inline val puzzleCount: Int
        get() = currentDungeon?.dungeonStats?.puzzleCount ?: 0

    inline val dungeonTime: String
        get() = currentDungeon?.dungeonStats?.elapsedTime ?: "00m 00s"

    inline val isGhost: Boolean
        get() = getItemSlot("Haunt", true) != null

    inline val currentRoomName: String
        get() = currentDungeon?.currentRoom?.data?.name ?: "Unknown"

    inline val dungeonTeammates: ArrayList<DungeonPlayer>
        get() = currentDungeon?.dungeonTeammates ?: ArrayList()

    inline val dungeonTeammatesNoSelf: List<DungeonPlayer>
        get() = currentDungeon?.dungeonTeammatesNoSelf ?: ArrayList()

    inline val leapTeammates: List<DungeonPlayer>
        get() = currentDungeon?.leapTeammates ?: ArrayList()

    inline val currentDungeonPlayer: DungeonPlayer
        get() = dungeonTeammates.find { it.name == mc.thePlayer?.name } ?: DungeonPlayer(mc.thePlayer?.name ?: "Unknown", DungeonClass.Unknown, 0, entity = mc.thePlayer)

    inline val doorOpener: String
        get() = currentDungeon?.dungeonStats?.doorOpener ?: "Unknown"

    inline val mimicKilled: Boolean
        get() = currentDungeon?.dungeonStats?.mimicKilled == true

    inline val currentRoom: Room?
        get() = currentDungeon?.currentRoom

    inline val passedRooms: Set<Room>
        get() = currentDungeon?.passedRooms.orEmpty()

    inline val isPaul: Boolean
        get() = currentDungeon?.paul == true

    inline val getBonusScore: Int
        get() {
            var score = cryptCount.coerceAtMost(5)
            if (mimicKilled) score += 2
            if ((isPaul && togglePaul == 0) || togglePaul == 2) score += 10
            return score
        }

    inline val bloodDone: Boolean
        get() = currentDungeon?.dungeonStats?.bloodDone == true

    inline val score: Int
        get() {
            val completed = completedRoomCount + (if (!bloodDone) 1 else 0) + (if (!inBoss) 1 else 0)
            val total = if (totalRooms != 0) totalRooms else 36

            val exploration = floor((secretPercentage / floor.secretPercentage) / 100f * 40f).coerceIn(0f, 40f).toInt() +
                    floor(completed / total * 60f).coerceIn(0f, 60f).toInt()

            val skillRooms = floor(completed / total * 80f).coerceIn(0f, 80f).toInt()
            val puzzlePenalty = (puzzleCount - puzzles.count { it.status == PuzzleStatus.Completed }) * 10

            return exploration + (20 + skillRooms - puzzlePenalty - (deathCount * 2 - 1).coerceAtLeast(0)).coerceIn(20, 100) + getBonusScore + 100
        }

    inline val neededSecretsAmount: Int
        get() = ceil((totalSecrets * floor.secretPercentage) * (40 - getBonusScore + (deathCount * 2 - 1).coerceAtLeast(0)) / 40f).toInt()

    /**
     * Checks if the current dungeon floor number matches any of the specified options.
     *
     * @param options The floor number options to compare with the current dungeon floor.
     * @return `true` if the current dungeon floor matches any of the specified options, otherwise `false`.
     */
    fun isFloor(vararg options: Int): Boolean {
        return floor.floorNumber in options
    }

    /**
     * Gets the current phase of floor 7 boss.
     *
     * @return The current phase of floor 7 boss, or `null` if the player is not in the boss room.
     */
    fun getF7Phase(): M7Phases {
        if ((!isFloor(7) || !inBoss) && LocationUtils.isOnHypixel) return M7Phases.Unknown

        return when {
            posY > 210 -> M7Phases.P1
            posY > 155 -> M7Phases.P2
            posY > 100 -> M7Phases.P3
            posY > 45 -> M7Phases.P4
            else -> M7Phases.P5
        }
    }

    private fun getMageCooldownMultiplier(): Double {
        return if (currentDungeonPlayer.clazz != DungeonClass.Mage) 1.0
        else 1 - 0.25 - (floor(currentDungeonPlayer.clazzLvl / 2.0) / 100) * if (dungeonTeammates.count { it.clazz == DungeonClass.Mage } == 1) 2 else 1
    }

    /**
     * Gets the new ability cooldown after mage cooldown reductions.
     * @param baseSeconds The base cooldown of the ability in seconds. Eg 10
     * @return The new time
     */
    fun getAbilityCooldown(baseSeconds: Long): Long {
        return (baseSeconds * getMageCooldownMultiplier()).roundToLong()
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        if (inDungeons) currentDungeon?.onPacket(event)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRoomEnter(event: RoomEnterEvent) {
        if (inDungeons) currentDungeon?.enterDungeonRoom(event)
    }

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (inDungeons) currentDungeon?.onEntityJoin(event)
    }

    private val tablistRegex = Regex("^\\[(\\d+)] (?:\\[\\w+] )*(\\w+) .*?\\((\\w+)(?: (\\w+))*\\)$")
    var customLeapOrder: List<String> = emptyList()

    fun getDungeonTeammates(previousTeammates: ArrayList<DungeonPlayer>, tabList: List<String>): ArrayList<DungeonPlayer> {
        for (line in tabList) {
            val (_, name, clazz, clazzLevel) = tablistRegex.find(line)?.destructured ?: continue

            previousTeammates.find { it.name == name }?.let { player -> player.isDead = clazz == "DEAD" } ?:
            previousTeammates.add(DungeonPlayer(name, DungeonClass.entries.find { it.name == clazz } ?: continue, clazzLvl = romanToInt(clazzLevel), mc.netHandler?.getPlayerInfo(name)?.locationSkin ?: continue, mc.theWorld?.getPlayerEntityByName(name), false))
        }
        return previousTeammates
    }

    const val WITHER_ESSENCE_ID = "e0f3e929-869e-3dca-9504-54c666ee6f23"
    private const val REDSTONE_KEY = "fed95410-aba1-39df-9b95-1d4f361eb66e"

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
        return when {
            state.block.equalsOneOf(Blocks.chest, Blocks.trapped_chest, Blocks.lever) -> true
            state.block is BlockSkull -> (mc.theWorld?.getTileEntity(pos) as? TileEntitySkull)?.playerProfile?.id.toString().equalsOneOf(WITHER_ESSENCE_ID, REDSTONE_KEY)
            else -> false
        }
    }

    fun Room.getRelativeCoords(pos: Vec3) = pos.subtractVec(x = clayPos.x, z = clayPos.z).rotateToNorth(rotation)
    fun Room.getRealCoords(pos: Vec3) = pos.rotateAroundNorth(rotation).addVec(x = clayPos.x, z = clayPos.z)
    fun Room.getRelativeCoords(pos: BlockPos) = getRelativeCoords(Vec3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())).toBlockPos()
    fun Room.getRealCoords(pos: BlockPos) = getRealCoords(Vec3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())).toBlockPos()
    fun Room.getRelativeCoords(x: Int, y: Int, z: Int) = getRelativeCoords(Vec3(x.toDouble(), y.toDouble(), z.toDouble())).toBlockPos()
    fun Room.getRealCoords(x: Int, y: Int, z: Int) = getRealCoords(Vec3(x.toDouble(), y.toDouble(), z.toDouble())).toBlockPos()

    val dungeonItemDrops = listOf(
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion", "Healing Potion VIII Splash Potion", "Healing VIII Splash Potion", "Healing 8 Splash Potion",
        "Decoy", "Inflatable Jerry", "Spirit Leap", "Trap", "Training Weights", "Defuse Kit", "Dungeon Chest Key", "Treasure Talisman", "Revive Stone", "Architect's First Draft"
    )
}