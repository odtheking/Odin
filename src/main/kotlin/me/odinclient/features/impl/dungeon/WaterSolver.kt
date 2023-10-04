package me.odinclient.features.impl.dungeon


import me.odinclient.features.Category
import me.odinclient.features.Module
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.odinclient.ModCore
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.*
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import tv.twitch.chat.Chat
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object WaterSolver : Module(
    name = "Water solver",
    description = "Solves the water puzzle with a single flow.",
    category = Category.DUNGEON,
) {
    private var waterSolutions: JsonObject

    init {
        val isr = WaterSolver::class.java.getResourceAsStream("/watertimes.json")
            ?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
        waterSolutions = JsonParser().parse(isr).asJsonObject
    }

    private var chestPos: BlockPos? = null
    private var roomFacing: EnumFacing? = null
    private var prevInWaterRoom = false
    private var inWaterRoom = false
    private var variant = -1
    private var extendedSlots = ""
    private var ticks = 0
    private var solutions = mutableMapOf<LeverBlock, Array<Double>>()
    private var openedWater = -1L
    private var job: Job? = null

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END || !DungeonUtils.inDungeons) return
        val player = Minecraft.getMinecraft().thePlayer ?: return
        val world = Minecraft.getMinecraft().theWorld ?: return

        if (ticks % 20 == 0) {
            if (variant == -1 && (job == null || job?.isCancelled == true || job?.isCompleted == true)) {
                job = ModCore.scope.launch {
                    prevInWaterRoom = inWaterRoom
                    inWaterRoom = false
                    if (BlockPos.getAllInBox(
                            BlockPos(player.posX.toInt() - 13, 54, player.posZ.toInt() - 13),
                            BlockPos(player.posX.toInt() + 13, 54, player.posZ.toInt() + 13))
                            .any { world.getBlockState(it).block == Blocks.sticky_piston }) {
                        val xRange = player.posX.toInt() - 25..player.posX.toInt() + 25
                        val zRange = player.posZ.toInt() - 25..player.posZ.toInt() + 25
                        roomRotation@
                        for (te in world.loadedTileEntityList) {
                            if (te.pos.y == 56 && te is TileEntityChest && te.numPlayersUsing == 0 && te.pos.x in xRange && te.pos.z in zRange) {
                                if (world.getBlockState(te.pos.down()).block == Blocks.stone && world.getBlockState(te.pos.up(2)).block == Blocks.stained_glass) {
                                    for (horizontal in EnumFacing.HORIZONTALS) {
                                        if (world.getBlockState(te.pos.offset(horizontal.opposite, 3).down(2)).block == Blocks.sticky_piston) {
                                            if (world.getBlockState(te.pos.offset(horizontal, 2)).block == Blocks.stone) {
                                                chestPos = te.pos
                                                roomFacing = horizontal
                                                break@roomRotation
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (chestPos == null) return@launch

                        findWaterRoom()

                    } else {
                        solutions.clear()
                        variant = -1
                    }
                }
            }
            ticks = 0
        }
        ticks++
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        for (solution in solutions) {
            for (i in solution.key.i until solution.value.size) {
                val time = solution.value[i]
                val displayText = if (openedWater == -1L) {
                    if (time == 0.0) {
                        EnumChatFormatting.GREEN.toString() + EnumChatFormatting.BOLD.toString() + "CLICK ME!"
                    } else {
                        EnumChatFormatting.YELLOW.toString() + time + "s"
                    }
                } else {
                    val remainingTime = openedWater + time * 1000L - System.currentTimeMillis()
                    if (remainingTime > 0) {
                        EnumChatFormatting.YELLOW.toString() + (remainingTime / 1000).toString() + "s"
                    } else {
                        EnumChatFormatting.GREEN.toString() + EnumChatFormatting.BOLD.toString() + "CLICK ME!"
                    }
                }
                RenderUtils.drawStringInWorld(displayText, Vec3(solution.key.leverPos).addVector(0.5, (i - solution.key.i) * 0.5 + 1.5, 0.5), 0, false, increase = false, depthTest = true, 0.05f )
            }
        }
    }

    @SubscribeEvent
    fun onBlockInteract(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return
        if (solutions.isEmpty()) return
        for (value in LeverBlock.entries) {
            if (value.leverPos == event.pos) {
                value.i++
                if (value == LeverBlock.WATER) {
                    if (openedWater == -1L) {
                        openedWater = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    private fun findWaterRoom() {
        val playerPos = mc.thePlayer.position
        val box = BlockPos.getAllInBox(
            BlockPos(playerPos.x - 25, 82, playerPos.z - 25),
            BlockPos(playerPos.x + 25, 82, playerPos.z + 25))

        // Find the piston head block.
        val pistonHeadPos = box.find { mc.theWorld.getBlockState(it).block == Blocks.piston_head }

        // If the piston head block is found, then we are in the water room.
        if (pistonHeadPos != null) {
            inWaterRoom = true
            if (!prevInWaterRoom) {
                // Find the blocks in the box around the piston head block.
                val blockList = BlockPos.getAllInBox(BlockPos(pistonHeadPos.x + 1, 78, pistonHeadPos.z + 1), BlockPos(pistonHeadPos.x - 1, 77, pistonHeadPos.z - 1))

                // Check for the required blocks in the box.
                var foundGold = false
                var foundClay = false
                var foundEmerald = false
                var foundQuartz = false
                var foundDiamond = false
                for (blockPos in blockList) {
                    when (mc.theWorld.getBlockState(blockPos).block) {
                        Blocks.gold_block -> foundGold = true
                        Blocks.hardened_clay -> foundClay = true
                        Blocks.emerald_block -> foundEmerald = true
                        Blocks.quartz_block -> foundQuartz = true
                        Blocks.diamond_block -> foundDiamond = true
                    }
                }

                // If the required blocks are found, then set the variant and extendedSlots.
                variant = when {
                    foundGold && foundClay -> 0
                    foundEmerald && foundQuartz -> 1
                    foundQuartz && foundDiamond -> 2
                    foundGold && foundQuartz -> 3
                    else -> -1
                }

                extendedSlots = ""
                for (value in WoolColor.entries) {
                    if (value.isExtended) {
                        extendedSlots += value.ordinal.toString()
                    }
                }

                // If the extendedSlots length is not 3, then retry.
                if (extendedSlots.length != 3) {
                    println("Didn't find the solution! Retrying")
                    println("Slots: $extendedSlots")
                    println("Water: $inWaterRoom ($prevInWaterRoom)")
                    println("Chest: $chestPos")
                    println("Rotation: $roomFacing")
                    extendedSlots = ""
                    inWaterRoom = false
                    prevInWaterRoom = false
                    variant = -1
                    return
                }

                // Print the variant and extendedSlots.
                ChatUtils.modMessage("Variant: $variant:$extendedSlots:${roomFacing?.name}")

                // Clear the solutions and add the new solutions.
                solutions.clear()
                val solutionObj = waterSolutions[variant.toString()].asJsonObject[extendedSlots].asJsonObject
                for (mutableEntry in solutionObj.entrySet()) {
                    val lever = when (mutableEntry.key) {
                        "minecraft:quartz_block" -> LeverBlock.QUARTZ
                        "minecraft:gold_block" -> LeverBlock.GOLD
                        "minecraft:coal_block" -> LeverBlock.COAL
                        "minecraft:diamond_block" -> LeverBlock.DIAMOND
                        "minecraft:emerald_block" -> LeverBlock.EMERALD
                        "minecraft:hardened_clay" -> LeverBlock.CLAY
                        "minecraft:water" -> LeverBlock.WATER
                        else -> LeverBlock.NONE
                    }
                    solutions[lever] = mutableEntry.value.asJsonArray.map { it.asDouble }.toTypedArray()
                }
            }
        }
    }

    @SubscribeEvent
    fun reset(event: WorldEvent.Load) {
        chestPos = null
        roomFacing = null
        prevInWaterRoom = false
        inWaterRoom = false
        variant = -1
        extendedSlots = ""
        ticks = 0
        solutions.clear()
        openedWater = -1L
        LeverBlock.entries.forEach { it.i = 0 }
    }

    enum class WoolColor(var dyeColor: EnumDyeColor) {
        PURPLE(EnumDyeColor.PURPLE),
        ORANGE(EnumDyeColor.ORANGE),
        BLUE(EnumDyeColor.BLUE),
        GREEN(EnumDyeColor.GREEN),
        RED(EnumDyeColor.RED);

        val isExtended: Boolean
            get() = if (chestPos == null || roomFacing == null) false else Minecraft.getMinecraft().theWorld.getBlockState(
                chestPos!!.offset(roomFacing!!.opposite, 3 + ordinal)).block === Blocks.wool
    }

    enum class LeverBlock(var i: Int = 0) {
        QUARTZ,
        GOLD,
        COAL,
        DIAMOND,
        EMERALD,
        CLAY,
        WATER,
        NONE;

        val leverPos: BlockPos?
            get() {
                if (chestPos == null || roomFacing == null) return null
                return if (this == WATER) {
                    chestPos!!.offset(roomFacing!!.opposite, 17).up(4)
                } else {
                    val shiftBy = ordinal % 3 * 5
                    val leverSide = if (ordinal < 3) roomFacing!!.rotateY() else roomFacing!!.rotateYCCW()
                    chestPos!!.up(5).offset(leverSide.opposite, 6).offset(roomFacing!!.opposite, 2 + shiftBy)
                        .offset(leverSide)
                }
            }
    }


}