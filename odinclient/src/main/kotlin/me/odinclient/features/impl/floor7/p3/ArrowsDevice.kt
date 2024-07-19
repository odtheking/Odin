package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Color
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
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.input.Keyboard

object ArrowsDevice : Module(
    name = "Arrows Device",
    description = "Different features for the Sharp Shooter puzzle in floor 7.",
    category = Category.FLOOR7,
    tag = TagType.RISKY,
) {
    private val solverDropdown: Boolean by DropdownSetting("Solver")
    private val solver: Boolean by BooleanSetting("Enabled", default = true).withDependency { solverDropdown }
    private val markedPositionColor: Color by ColorSetting("Marked Position", Color.RED, description = "Color of the marked position.").withDependency { solver && solverDropdown }
    private val targetPositionColor: Color by ColorSetting("Target Position", Color.GREEN, description = "Color of the target position.").withDependency { solver && solverDropdown }
    private val resetKey: Keybinding by KeybindSetting("Reset", Keyboard.KEY_NONE, description = "Resets the solver.").onPress {
        reset()
    }.withDependency { solver && solverDropdown }
    private val depthCheck: Boolean by BooleanSetting("Depth check", true, description = "Marked positions show through walls.").withDependency { solver && solverDropdown }
    private val reset: () -> Unit by ActionSetting("Reset") {
        markedPositions.clear()
        autoState = AutoState.Stopped
        actionQueue.clear()
    }.withDependency { solver && solverDropdown }
    private val alertOnDeviceComplete: Boolean by BooleanSetting("Device complete alert", default = true, description = "Send an alert when device is complete").withDependency { solverDropdown }

    private val autoDropdown: Boolean by DropdownSetting("Auto")
    private val auto: Boolean by BooleanSetting("Enabled", description = "Automatically complete device").withDependency { autoDropdown }
    private val autoPhoenix: Boolean by BooleanSetting("Auto phoenix", default = true, description = "Automatically swap to phoenix pet using cast rod pet rules, must be set up correctly").withDependency { auto && autoDropdown }
    private val autoLeap: Boolean by BooleanSetting("Auto leap", default = true, description = "Automatically leap once device is done").withDependency { auto && autoDropdown }
    private val autoLeapClass: Int by SelectorSetting("Leap to", defaultSelected = "Mage", arrayListOf("Archer", "Berserk", "Healer", "Mage", "Tank"), description = "Who to leap to").withDependency { autoLeap && auto && autoDropdown }
    private val autoLeapOnlyPre: Boolean by BooleanSetting("Only leap on pre", default = true, description = "Only auto leap when doing i4").withDependency { autoLeap && auto && autoDropdown }
    private val delay: Long by NumberSetting("Delay", 150L, 80, 300, description = "Delay between actions").withDependency { auto && autoDropdown }
    private val aimingTime: Long by NumberSetting("Aiming duration", 100L, 80, 200, description = "Time taken to aim at a target").withDependency { auto && autoDropdown }

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
            Regex("^Your (?:. )?Bonzo's Mask saved your life!$"),
            { enabled && auto && autoPhoenix && isPlayerOnStand }) {
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
            if (DungeonUtils.getPhase() != M7Phases.P3) return@execute

            // Cast is safe since we won't return an entity that isn't an armor stand
            activeArmorStand = mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()?.find {
                it.name.equalsOneOf(
                    INACTIVE_DEVICE_STRING,
                    ACTIVE_DEVICE_STRING
                ) && it.distanceSquaredTo(
                    standPosition.toVec3()
                ) <= 4.0
            }
        }

        onWorldLoad {
            reset()
            // Reset is called when leaving the device room, but device remains complete across an entire run, so this doesn't belong in reset
            isDeviceComplete = false
        }
    }

    private val isDeviceRoomOpen: Boolean
        get() = mc.theWorld.getBlockState(lastGateBlock) == Blocks.air.defaultState

    private val isPlayerOnStand: Boolean
        get() = (mc.thePlayer?.distanceSquaredTo(standPosition.toVec3()) ?: Double.MAX_VALUE) <= 1.0

    private val isPlayerInRoom: Boolean
        get() = mc.thePlayer?.let { roomBoundingBox.isVecInside(it.positionVector) } == true

    private fun holdClick() {
        if (!mc.gameSettings.keyBindUseItem.isPressed) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true)
        }
    }

    private fun releaseClick() {
        if (mc.gameSettings.keyBindUseItem.isPressed) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false)
        }
    }

    private fun setCurrentSlot(slot: Int) {
        mc.thePlayer.inventory.currentItem = slot
    }

    private fun holdBow() {
        if ((mc.thePlayer?.inventory?.currentItem ?: -1) != bowSlot) {
            mc.thePlayer.inventory.currentItem = bowSlot
        }
    }

    private fun phoenixSwap() {
        val rodSlot = mc.thePlayer?.inventory?.mainInventory?.indexOfFirst { it.isFishingRod } ?: -1

        if (rodSlot < 0 || rodSlot >= 9) {
            modMessage("Couldn't find rod for phoenix swap")
            return
        }

        releaseClick()

        isPhoenixSwapping = true

        modMessage("Phoenix swapping")

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

        if (leapSlot < 0 || leapSlot >= 9) {
            modMessage("Couldn't find leaps for auto leap")
            return
        }

        if (DungeonUtils.leapTeammates.isEmpty()) {
            modMessage("Can't leap, there are no teammates")
            return
        }

        modMessage("Leaping")

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
                if (x == 0 || (x == 1 && markedPositions.contains(positions[x + 1 + y * 3]))) {
                    67.5
                } else {
                    65.5
                },
                131.3 - 2 * y,
                50.0
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
        releaseClick()

        if (alertOnDeviceComplete) {
            modMessage("Sharp shooter device complete")
            PlayerUtils.alert("Device Complete", color = Color.GREEN)
        }

        autoState = AutoState.Stopped

        if (auto && autoLeap && (!autoLeapOnlyPre || !isDeviceRoomOpen)) {
            leap()
        }
    }

    @SubscribeEvent
    fun guiOpen(event: GuiOpenEvent) {
        if (autoState != AutoState.WaitingOnLeapingGui) return
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap" || DungeonUtils.leapTeammates.isEmpty()) return

        val leapTo = DungeonUtils.leapTeammates.firstOrNull { it.clazz == DungeonClass.entries[autoLeapClass] }
            ?: DungeonUtils.leapTeammates.first()

        clock.update()

        actionQueue.add {
            getItemIndexInContainerChest(chest, leapTo.name, 11..16)?.let {
                PlayerUtils.windowClick(it, PlayerUtils.ClickType.Middle, instant = false)
            }
            autoState = AutoState.Stopped
        }
    }

    @SubscribeEvent
    fun onTick(tickEvent: ClientTickEvent) {
        if (!isPlayerInRoom) {
            reset()
            return
        }

        if (!isDeviceComplete && activeArmorStand?.name == ACTIVE_DEVICE_STRING) {
            onComplete()
        }

        if (autoState != AutoState.Stopped) {
            if (!isPlayerOnStand) {
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


    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        serverTicksSinceLastTargetDisappeared = serverTicksSinceLastTargetDisappeared?.let {
            // There was no target last tick (or the count would be null)

            if (targetPosition != null) {
                // A target appeared
                return@let null
            } else if (it < 10) {
                // No target yet, count the ticks
                return@let it + 1
            } else if (it == 10) {
                // We reached 10 ticks, device is either done, or the player left the stand
                if (isPlayerOnStand) onComplete()
                return@let 11
            } else {
                return@let 11
            }
        } ?: run {
            // There was a target last tick (or one appeared this tick

            // Check if target disappeared, set count accordingly
            return@run if (targetPosition == null) 0 else null
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

                if (autoState != AutoState.Stopped) {
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

            if (isPlayerOnStand && auto) {
                if (autoState == AutoState.Stopped) {
                    bowSlot = mc.thePlayer?.inventory?.mainInventory?.indexOfFirst { it.isShortbow } ?: -1

                    if (bowSlot < 0 || bowSlot >= 9) {
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