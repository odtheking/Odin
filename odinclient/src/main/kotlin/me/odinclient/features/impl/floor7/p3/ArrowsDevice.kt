package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.distanceSquaredTo
import me.odinmain.utils.getDirectionToVec3
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.isFishingRod
import me.odinmain.utils.skyblock.isShortbow
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.smoothRotateTo
import me.odinmain.utils.toVec3
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard

object ArrowsDevice : Module(
    name = "Arrows Device",
    description = "Different features for the Sharp Shooter puzzle in floor 7.",
    category = Category.FLOOR7,
    tag = TagType.RISKY,
    key = null
) {
    private val solver: Boolean by BooleanSetting("Solver")
    private val markedPositionColor: Color by ColorSetting("Marked Position", Color.RED, description = "Color of the marked position.").withDependency { solver }
    private val targetPositionColor: Color by ColorSetting("Target Position", Color.GREEN, description = "Color of the target position.").withDependency { solver }
    private val resetKey: Keybinding by KeybindSetting("Reset", Keyboard.KEY_NONE, description = "Resets the solver.").onPress {
        reset()
        mc.thePlayer?.inventory?.currentItem = 2
    }.withDependency { solver }
    private val depthCheck: Boolean by BooleanSetting("Depth check", true, description = "Marked positions show through walls.").withDependency { solver }
    private val reset: () -> Unit by ActionSetting("Reset") {
        markedPositions.clear()
        autoState = AutoState.Stopped
    }.withDependency { solver }
    private val auto: Boolean by BooleanSetting("Auto", description = "Automatically complete device")
    private val autoPhoenix: Boolean by BooleanSetting("Auto phoenix", default = true, description = "Automatically swap to phoenix pet using cast rod pet rules, must be set up correctly").withDependency { auto }
    private val delay: Long by NumberSetting("Delay", 100L, 30, 300, description = "Delay between actions").withDependency { auto }

    init {
        onMessage(
            Regex("^Your (?:. )?Bonzo's Mask saved your life!$"),
            { enabled && autoPhoenix && isPlayerOnStand() }) {
            rodSlot = mc.thePlayer?.inventory?.mainInventory?.indexOfFirst { it.isFishingRod } ?: -1;

            if (rodSlot < 0 || rodSlot >= 9) {
                modMessage("Couldn't find rod for phoenix swap")
                return@onMessage
            }

            releaseClick()

            isPhoenixSwapping = true

            modMessage("Phoenix swapping")

            clock.update()

            actionQueue.addAll(listOf(
                {
                    // I would use swapToIndex, but for some reason it doesn't work, so this is it
                    mc.thePlayer.inventory.currentItem = rodSlot
                },
                {
                    rightClick()
                },
                {
                    mc.thePlayer.inventory.currentItem = shortbowSlot
                    isPhoenixSwapping = false
                }
            ))
        }

        onWorldLoad {
            reset()
        }
    }

    private fun isPlayerOnStand(): Boolean {
        return (mc.thePlayer?.distanceSquaredTo(standPosition.toVec3()) ?: Double.MAX_VALUE) <= 4.0
    }

    private fun aimAtTarget() {
        targetPosition?.let {

            if(isPhoenixSwapping) {
                // We are busy with swapping to phoenix, so wait until that's done
                actionQueue.add {
                    aimAtTarget()
                }
                return
            }

            val index = positions.indexOf(it)
            // Position of the target in the grid
            val x = index % 3
            val y = index / 3

            // Choose a correct target to hit as many blocks as possible
            val target = Vec3(
                if (x == 0 || (x == 1 && markedPositions.contains(positions[x + 1 + y * 3]))) { 67.5 } else { 65.5 },
                (131.3 - 2 * y).toDouble(),
                50.0
            )

            holdShortbow()
            holdClick()
            val (_, yaw, pitch) = getDirectionToVec3(target)
            smoothRotateTo(yaw, pitch, delay) {
                autoState = AutoState.Shooting
                holdShortbow()
            }
        }
    }

    @SubscribeEvent
    fun onTick(tickEvent: TickEvent) {
        if (tickEvent.phase != TickEvent.Phase.START) return

        if (autoState != AutoState.Stopped) {
            if (!isPlayerOnStand()) {
                autoState = AutoState.Stopped
                releaseClick()
                return
            }

            if (autoState == AutoState.Shooting && !isPhoenixSwapping) {
                holdClick()
            }

            if (clock.hasTimePassed(delay) && actionQueue.isNotEmpty()) {
                actionQueue.removeFirst()()
                clock.update()
            }
        }
    }

    private fun holdClick() {
        if(!mc.gameSettings.keyBindUseItem.isPressed) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
        }
    }

    private fun releaseClick() {
        if(mc.gameSettings.keyBindUseItem.isPressed) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
        }
    }

    private fun holdShortbow() {
        if((mc.thePlayer?.inventory?.currentItem ?: -1) != shortbowSlot) {
            mc.thePlayer.inventory.currentItem = shortbowSlot
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (!DungeonUtils.inDungeons || DungeonUtils.getPhase() != M7Phases.P3 || !positions.contains(event.pos)) return

        // Target was hit
        if (event.old.block == Blocks.emerald_block && event.update.block == Blocks.stained_hardened_clay) {
            markedPositions.add(event.pos)
            // This condition should always be true but im never sure with Hypixel
            if (targetPosition == event.pos) {
                targetPosition = null

                if(autoState != AutoState.Stopped) {
                    //releaseClick()
                    autoState = AutoState.Aiming
                }
            }
        }

        // New target appeared
        if (event.old.block == Blocks.stained_hardened_clay && event.update.block == Blocks.emerald_block) {
            // Can happen with resets
            markedPositions.remove(event.pos)
            targetPosition = event.pos

            if (isPlayerOnStand() && auto) {
                if (autoState == AutoState.Stopped) {
                    shortbowSlot = mc.thePlayer?.inventory?.mainInventory?.indexOfFirst { it.isShortbow } ?: -1;

                    if (shortbowSlot < 0 || shortbowSlot >= 9) {
                        modMessage("Couldn't find shortbow for auto sharp shooter")
                        return
                    }

                    modMessage("Starting sharp shooter")
                }

                autoState = AutoState.Aiming
                aimAtTarget()
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (PlayerUtils.posZ > 55 || (PlayerUtils.posX > 95 || PlayerUtils.posX < 15)) reset()
        if (!DungeonUtils.inDungeons || DungeonUtils.getPhase() != M7Phases.P3 || !solver) return
        markedPositions.forEach {
            Renderer.drawBlock(it, markedPositionColor, depth = depthCheck)
        }
        targetPosition?.let {
            Renderer.drawBlock(it, targetPositionColor, depth = depthCheck)
        }
    }

    // This is order dependent
    private val positions = listOf(
        BlockPos(68, 130, 50), BlockPos(66, 130, 50), BlockPos(64, 130, 50),
        BlockPos(68, 128, 50), BlockPos(66, 128, 50), BlockPos(64, 128, 50),
        BlockPos(68, 126, 50), BlockPos(66, 126, 50), BlockPos(64, 126, 50)
    )

    // Position of the pressure plate for auto
    private val standPosition = BlockPos(63.5, 127.0, 35.5)

    private val markedPositions = mutableSetOf<BlockPos>()
    private var targetPosition: BlockPos? = null

    private var shortbowSlot = 0
    private var rodSlot = 0
    private var autoState = AutoState.Stopped

    private val clock = Clock(delay)

    private val actionQueue = ArrayDeque<() -> Unit>()

    private var isPhoenixSwapping = false

    private enum class AutoState {
        Stopped,
        Aiming,
        Shooting,
    }
}