package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderX
import me.odinmain.utils.render.RenderUtils.renderY
import me.odinmain.utils.render.RenderUtils.renderZ
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonClass
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.*
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.input.Keyboard
import kotlin.math.sqrt

object ArrowsDevice : Module(
    name = "Arrows Device",
    description = "Different features for the Sharp Shooter puzzle in floor 7.",
    category = Category.FLOOR7,
    tag = TagType.RISKY,
) {
    private val solverDropdown by DropdownSetting("Solver")
    private val solver by BooleanSetting("Solver Enabled", default = true, description = "Automatically solve the puzzle.").withDependency { solverDropdown }
    private val markedPositionColor by ColorSetting("Marked Position", Color.RED, description = "Color of the marked position.").withDependency { solver && solverDropdown }
    private val targetPositionColor by ColorSetting("Target Position", Color.GREEN, description = "Color of the target position.").withDependency { solver && solverDropdown }
    private val resetKey by KeybindSetting("Reset", Keyboard.KEY_NONE, description = "Resets the solver.").onPress {
        markedPositions.clear()
        autoState = AutoState.Stopped
        actionQueue.clear()
    }.withDependency { solver && solverDropdown }
    private val depthCheck by BooleanSetting("Depth check", true, description = "Marked positions show through walls.").withDependency { solver && solverDropdown }
    private val reset by ActionSetting("Reset", description = "Resets the solver.") {
        markedPositions.clear()
        autoState = AutoState.Stopped
        actionQueue.clear()
    }.withDependency { solver && solverDropdown }
    private val alertOnDeviceComplete by BooleanSetting("Device complete alert", default = true, description = "Send an alert when device is complete.").withDependency { solverDropdown }

    private val autoDropdown by DropdownSetting("Auto Device")
    private val auto by BooleanSetting("Auto Enabled", description = "Automatically complete device.").withDependency { autoDropdown }
    private val autoShoot by BooleanSetting("Auto Shoot", description = "Automatically aim and shoot at targets.").withDependency { auto && autoDropdown }
    private val autoPhoenix by BooleanSetting("Auto Phoenix", default = true, description = "Automatically swap to phoenix pet using cast rod pet rules, must be set up correctly.").withDependency { auto && autoDropdown }
    private val autoLeap by BooleanSetting("Auto Leap", default = true, description = "Automatically leap once device is done.").withDependency { auto && autoDropdown }
    private val autoLeapClass by SelectorSetting("Leap to", defaultSelected = "Mage", arrayListOf("Archer", "Berserk", "Healer", "Mage", "Tank"), description = "Who to leap to.").withDependency { autoLeap && auto && autoDropdown }
    private val autoLeapOnlyPre by BooleanSetting("Only leap on pre", default = true, description = "Only auto leap when doing i4.").withDependency { autoLeap && auto && autoDropdown }
    private val delay by NumberSetting("Auto Delay", 150L, 80, 300, description = "Delay between actions.").withDependency { auto && autoDropdown }
    private val aimingTime by NumberSetting("Aiming Duration", 100L, 80, 200, description = "Time taken to aim at a target.").withDependency { auto && autoDropdown }

    private val triggerBotDropdown by DropdownSetting("Trigger Bot")
    private val triggerBot by BooleanSetting("Trigger Bot Enabled", description = "Automatically shoot targets.").withDependency { triggerBotDropdown }
    private val triggerBotDelay by NumberSetting("Trigger Bot Delay", 250L, 50L, 1000L, 10L, unit = "ms", description = "The delay between each click.").withDependency { triggerBotDropdown }
    private val triggerBotClock = Clock(triggerBotDelay)

    private val markedPositions = mutableSetOf<BlockPos>()
    private var targetPosition: BlockPos? = null

    private var autoState = AutoState.Stopped
    private var isPhoenixSwapping = false
    private var isDeviceComplete = false
    private var bowSlot = 0

    // ArmorStand showing the status of the device
    private var activeArmorStand: EntityArmorStand? = null

    // Clock used for action delay (mostly for phoenix swapping and leaping)
    private val clock = Clock(delay)
    private val actionQueue = ArrayDeque<() -> Unit>()

    // Number of server ticks since the last target disappeared, or null if there is a target
    private var serverTicksSinceLastTargetDisappeared: Int? = null

    init {
        onMessage(
            Regex("^Your (?:. )?Bonzo's Mask saved your life!$"), { enabled && auto && autoPhoenix && isPlayerOnStand }) {
            phoenixSwap()
        }

        onMessage(Regex("^[a-zA-Z0-9_]{3,} completed a device! \\([1-7]/7\\)"), { enabled && isPlayerInRoom }) {
            onComplete()
        }

        onMessage(Regex("^ ☠ You died and became a ghost\\.$"), { enabled && isPlayerOnStand }) {
            // Died while on device
            autoState = AutoState.Stopped
            actionQueue.clear()
            // Prevent the tick count from continuing and registering a false positive
            // This does mean we could get a false negative but since the player is already
            // dead knowing if the device is complete has less importance (no leaping)
            serverTicksSinceLastTargetDisappeared = 11
        }

        execute(1000) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@execute

            activeArmorStand = mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()?.find {
                it.name.equalsOneOf(INACTIVE_DEVICE_STRING, ACTIVE_DEVICE_STRING) && it.distanceSquaredTo(standPosition.toVec3()) <= 4.0
            }
        }

        onWorldLoad {
            markedPositions.clear()
            autoState = AutoState.Stopped
            actionQueue.clear()
            // Reset is called when leaving the device room, but device remains complete across an entire run, so this doesn't belong in reset
            isDeviceComplete = false
        }

        execute(10) {
            if (!triggerBot || !triggerBotClock.hasTimePassed(triggerBotDelay) || mc.thePlayer?.heldItem?.isShortbow == false || DungeonUtils.getF7Phase() != M7Phases.P3) return@execute
            setBowTrajectoryHeading(0f)
            if (!isHolding("TERMINATOR")) return@execute
            setBowTrajectoryHeading(-5f)
            setBowTrajectoryHeading(5f)
        }
    }

    private fun setBowTrajectoryHeading(yawOffset: Float) {
        val yawRadians = ((mc.thePlayer.rotationYaw + yawOffset) / 180) * Math.PI.toFloat()
        val pitchRadians = (mc.thePlayer.rotationPitch / 180) * Math.PI.toFloat()

        var posX = mc.thePlayer.renderX
        var posY = mc.thePlayer.renderY + mc.thePlayer.eyeHeight
        var posZ = mc.thePlayer.renderZ
        posX -= (MathHelper.cos(mc.thePlayer.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16f).toDouble()
        posY -= 0.1
        posZ -= (MathHelper.sin(mc.thePlayer.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16f).toDouble()

        var motionX = (-MathHelper.sin(yawRadians) * MathHelper.cos(pitchRadians)).toDouble()
        var motionY = -MathHelper.sin(pitchRadians).toDouble()
        var motionZ = (MathHelper.cos(yawRadians) * MathHelper.cos(pitchRadians)).toDouble()

        val lengthOffset = sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ)
        motionX = motionX / lengthOffset * 3
        motionY = motionY / lengthOffset * 3
        motionZ = motionZ / lengthOffset * 3

        calculateBowTrajectory(Vec3(motionX,motionY,motionZ),Vec3(posX,posY,posZ))
    }

    private fun calculateBowTrajectory(mV: Vec3, pV: Vec3) {
        var motionVec = mV
        var posVec = pV
        repeat(21) {
            val vec = motionVec.add(posVec)
            val rayTrace = mc.theWorld?.rayTraceBlocks(posVec, vec, false, true, false)
            if (rayTrace?.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (getBlockIdAt(rayTrace.blockPos) == 133) {
                    if (rayTrace.blockPos.x !in 64..68 || rayTrace.blockPos.y !in 126..130) return // not on device
                    rightClick()
                    triggerBotClock.update()
                }
                return@repeat
            }
            posVec = posVec.add(motionVec)
            motionVec = Vec3(motionVec.xCoord * 0.99, motionVec.yCoord * 0.99 - 0.05, motionVec.zCoord * 0.99)
        }
    }

    private val isDeviceRoomOpen: Boolean
        get() = mc.theWorld?.getBlockState(lastGateBlock) == Blocks.air.defaultState

    private val isPlayerOnStand: Boolean
        get() = (mc.thePlayer?.distanceSquaredTo(standPosition.toVec3()) ?: Double.MAX_VALUE) <= 1.0

    private val isPlayerInRoom: Boolean
        get() = mc.thePlayer?.let { roomBoundingBox.isVecInside(it.positionVector) } == true

    private fun holdClick() {
        if (!mc.gameSettings.keyBindUseItem.isPressed)
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
    }

    private fun releaseClick() {
        if (mc.gameSettings.keyBindUseItem.isPressed)
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
    }

    private fun setCurrentSlot(slot: Int) {
        mc.thePlayer.inventory.currentItem = slot
    }

    private fun holdBow() {
        if ((mc.thePlayer?.inventory?.currentItem ?: -1) != bowSlot)
            mc.thePlayer.inventory.currentItem = bowSlot
    }

    private fun phoenixSwap() {
        val rodSlot = mc.thePlayer?.inventory?.mainInventory?.indexOfFirst { it.isFishingRod } ?: -1

        if (rodSlot < 0 || rodSlot >= 9) return modMessage("§cCouldn't find rod for phoenix swap")
        releaseClick()

        isPhoenixSwapping = true
        modMessage("§6Phoenix swapping")
        clock.update()

        actionQueue.addAll(listOf(
            {
                // I would use swapToIndex, but for some reason it doesn't work, so this is it
                setCurrentSlot(rodSlot)
            },
            {
                rightClick()
            },
            {
                setCurrentSlot(bowSlot)
                isPhoenixSwapping = false
            }
        ))
    }

    private fun leap() {
        val leapSlot = mc.thePlayer?.inventory?.mainInventory?.indexOfFirst { it.isLeap } ?: -1

        if (leapSlot < 0 || leapSlot >= 9) return modMessage("§cCouldn't find leap for auto leap")

        if (DungeonUtils.dungeonTeammatesNoSelf.isEmpty()) modMessage("§cNo leap teammates found")

        modMessage("§bLeaping")
        autoState = AutoState.Leaping
        clock.update()

        actionQueue.addAll(listOf(
            {
                setCurrentSlot(leapSlot)
            },
            {
                autoState = AutoState.WaitingOnLeapingGui
                rightClick()
            }
        ))
    }

    private fun aimAtTarget() {
        targetPosition?.let {

            if (isPhoenixSwapping) {
                // We are busy with swapping to phoenix, so wait until that's done
                actionQueue.add(::aimAtTarget)
                return
            }

            val index = positions.indexOf(it)
            // Position of the target in the grid
            val x = index % 3
            val y = index / 3

            // Choose a correct target to hit as many blocks as possible
            val target = Vec3(
                if (x == 0 || (x == 1 && markedPositions.contains(positions[x + 1 + y * 3]))) 67.5 else 65.5,
                131.3 - 2 * y, 50.0
            )

            holdBow()
            holdClick()
            val (_, yaw, pitch) = getDirectionToVec3(target)
            smoothRotateTo(yaw, pitch, aimingTime) {
                autoState = AutoState.Shooting
                holdBow()
            }
        }
    }

    // There are 3 detection methods for device completion:
    //  - Message: If the '... completed a device! (1/7)' message is sent, this is usually the fastest way to detect,
    //    but doesn't always work (sometimes the message simply doesn't get sent, especially in i4)
    //  - ArmorStand: If the 'Device Active' text is displayed, this always works but is the slowest
    //  - Ticks: If the next emerald block doesn't appear for 10 (or more) server ticks, this is faster than the checking
    //    for the text most of the time (but not always) also can fail if the player leaves the device before those 10
    //    ticks are up
    // We use all three here since we want to detect as soon as possible (since we might die if we wait too long).
    private fun onComplete() {
        if (isDeviceComplete) return

        isDeviceComplete = true
        if (autoShoot)
            releaseClick()

        if (alertOnDeviceComplete) {
            modMessage("§aSharp shooter device complete")
            PlayerUtils.alert("Device Complete", color = Color.GREEN)
        }

        autoState = AutoState.Stopped

        if (auto && autoLeap && (!autoLeapOnlyPre || !isDeviceRoomOpen)) leap()
    }

    @SubscribeEvent
    fun guiOpen(event: GuiOpenEvent) {
        if (autoState != AutoState.WaitingOnLeapingGui) return
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap" || DungeonUtils.leapTeammates.isEmpty()) return

        val leapTo = DungeonUtils.leapTeammates.firstOrNull { it.clazz == DungeonClass.entries[autoLeapClass] }
            ?: DungeonUtils.leapTeammates.first()

        clock.update()

        var attempts = 0

        fun tryClick () {
            if (attempts > 2) return
            getItemIndexInContainerChest(chest, leapTo.name, 11..16)?.also {
                PlayerUtils.windowClick(it, PlayerUtils.ClickType.Middle, instant = false)
                autoState = AutoState.Stopped
            } ?: run {
                attempts += 1
                actionQueue.add(::tryClick)
            }
        }

        actionQueue.add(::tryClick)
    }

    @SubscribeEvent
    fun onTick(tickEvent: ClientTickEvent) {
        if (!isPlayerInRoom) {
            markedPositions.clear()
            autoState = AutoState.Stopped
            actionQueue.clear()
            return
        }

        if (!isDeviceComplete && activeArmorStand?.name == ACTIVE_DEVICE_STRING) onComplete()

        if (autoState != AutoState.Stopped) {
            if (!isPlayerOnStand) {
                autoState = AutoState.Stopped
                if(autoShoot)
                    releaseClick()
                return
            }

            if (autoShoot && autoState == AutoState.Shooting && !isPhoenixSwapping) holdClick()

            if (clock.hasTimePassed(delay) && actionQueue.isNotEmpty()) {
                actionQueue.removeFirst()()
                clock.update()
            }
        }
    }


    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        serverTicksSinceLastTargetDisappeared = serverTicksSinceLastTargetDisappeared?.let {
            // There was no target last tick (or the count would be null)

            if (targetPosition != null) return@let null // A target appeared
            else if (it < 10)  return@let it + 1 // No target yet, count the ticks
            else if (it == 10) {
                // We reached 10 ticks, device is either done, or the player left the stand
                if (isPlayerOnStand) onComplete()
                return@let 11
            } else return@let 11
        } ?: run {
            // There was a target last tick (or one appeared this tick
            // Check if target disappeared, set count accordingly
            return@run if (targetPosition == null) 0 else null
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (!DungeonUtils.inDungeons || DungeonUtils.getF7Phase() != M7Phases.P3 || !positions.contains(event.pos)) return

        // Target was hit
        if (event.old.block == Blocks.emerald_block && event.update.block == Blocks.stained_hardened_clay) {
            markedPositions.add(event.pos)
            // This condition should always be true but im never sure with Hypixel
            if (targetPosition == event.pos) {
                targetPosition = null

                if (autoState != AutoState.Stopped) autoState = AutoState.Aiming
            }
        }

        // New target appeared
        if (event.old.block == Blocks.stained_hardened_clay && event.update.block == Blocks.emerald_block) {
            // Can happen with resets
            markedPositions.remove(event.pos)
            targetPosition = event.pos

            if (isPlayerOnStand && auto) {
                if (autoState == AutoState.Stopped) {
                    bowSlot = mc.thePlayer?.inventory?.mainInventory?.indexOfFirst { it.isShortbow } ?: -1

                    if (bowSlot < 0 || bowSlot >= 9) return modMessage("§cCouldn't find bow for auto sharp shooter")

                    modMessage("§aStarting sharp shooter")
                }

                if (autoShoot) {
                    autoState = AutoState.Aiming
                    aimAtTarget()
                } else {
                    autoState = AutoState.Shooting
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons || DungeonUtils.getF7Phase() != M7Phases.P3 || !solver) return
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
    private val roomBoundingBox = AxisAlignedBB(20.0, 100.0, 30.0, 89.0, 151.0, 51.0)
    private val lastGateBlock = BlockPos(8, 118, 50)

    private const val ACTIVE_DEVICE_STRING = "§aDevice"
    private const val INACTIVE_DEVICE_STRING = "§cInactive"

    private enum class AutoState {
        Stopped,
        Aiming,
        Shooting,
        Leaping,
        WaitingOnLeapingGui,
    }
}