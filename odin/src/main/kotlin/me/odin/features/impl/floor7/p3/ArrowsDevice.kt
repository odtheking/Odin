package me.odin.features.impl.floor7.p3

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toVec3
import me.odinmain.utils.ui.Colors
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.input.Keyboard

object ArrowsDevice : Module(
    name = "Arrows Device",
    desc = "Shows a solution for the Sharp Shooter puzzle in floor 7."
) {
    private val solver by BooleanSetting("Solver", desc = "Enables the solver.")
    private val markedPositionColor by ColorSetting("Marked Position", Colors.MINECRAFT_RED, desc = "Color of the marked position.").withDependency { solver }
    private val targetPositionColor by ColorSetting("Target Position", Colors.MINECRAFT_GREEN, desc = "Color of the target position.").withDependency { solver }
    private val resetKey by KeybindSetting("Reset", Keyboard.KEY_NONE, description = "Resets the solver.").onPress {
        markedPositions.clear()
    }.withDependency { solver }
    private val depthCheck by BooleanSetting("Depth check", true, desc = "Marked positions show through walls.").withDependency { solver }
    private val reset by ActionSetting("Reset", desc = "Resets the solver.") {
        markedPositions.clear()
    }.withDependency { solver }
    private val alertOnDeviceComplete by BooleanSetting("Device complete alert", true, desc = "Send an alert when device is complete.")

    private val markedPositions = mutableSetOf<BlockPos>()
    private var targetPosition: BlockPos? = null

    private var isDeviceComplete = false

    // ArmorStand showing the status of the device
    private var activeArmorStand: EntityArmorStand? = null

    // Number of server ticks since the last target disappeared, or null if there is a target
    private var serverTicksSinceLastTargetDisappeared: Int? = null

    init {
        onMessage(Regex("^[a-zA-Z0-9_]{3,} completed a device! \\([1-7]/7\\)"), { enabled && isPlayerInRoom }) {
            onComplete()
        }

        onMessage(Regex("^ ☠ You died and became a ghost\\.$"), { enabled && isPlayerOnStand }) {
            // Prevent the tick count from continuing and registering a false positive
            // This does mean we could get a false negative but since the player is already
            // dead knowing if the device is complete has less importance (no leaping)
            serverTicksSinceLastTargetDisappeared = 11
        }

        execute(1000) {
            if (DungeonUtils.getF7Phase() != M7Phases.P3) return@execute

            // Cast is safe since we won't return an entity that isn't an armor stand
            activeArmorStand = mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()?.find {
                it.name.equalsOneOf(INACTIVE_DEVICE_STRING, ACTIVE_DEVICE_STRING) && standPosition.toVec3().distanceTo(it.positionVector) <= 4.0
            }
        }

        onWorldLoad {
            markedPositions.clear()
            // Reset is called when leaving the device room, but device remains complete across an entire run, so this doesn't belong in reset
            isDeviceComplete = false
        }
    }

    private val isPlayerOnStand: Boolean
        get() = (mc.thePlayer?.positionVector?.distanceTo(standPosition.toVec3()) ?: Double.MAX_VALUE) <= 1.0

    private val isPlayerInRoom: Boolean
        get() = mc.thePlayer?.let { roomBoundingBox.isVecInside(it.positionVector) } == true

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

        if (alertOnDeviceComplete) {
            modMessage("§aSharp shooter device complete")
            PlayerUtils.alert("§aDevice Complete", color = Colors.MINECRAFT_GREEN)
        }
    }

    @SubscribeEvent
    fun onTick(tickEvent: ClientTickEvent) {
        if (!isPlayerInRoom) return markedPositions.clear()

        if (!isDeviceComplete && activeArmorStand?.name == ACTIVE_DEVICE_STRING) onComplete()
    }


    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        serverTicksSinceLastTargetDisappeared = serverTicksSinceLastTargetDisappeared?.let {
            // There was no target last tick (or the count would be null)

            when {
                targetPosition != null -> return@let null // A target appeared
                it < 10 -> return@let it + 1 // No target yet, count the ticks
                it == 10 -> {
                    // We reached 10 ticks, device is either done, or the player left the stand
                    if (isPlayerOnStand) onComplete()
                    return@let 11
                }
                else -> return@let 11
            }
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
        if (event.old.block == Blocks.emerald_block && event.updated.block == Blocks.stained_hardened_clay) {
            markedPositions.add(event.pos)
            // This condition should always be true but im never sure with Hypixel
            if (targetPosition == event.pos) targetPosition = null
        }

        // New target appeared
        if (event.old.block == Blocks.stained_hardened_clay && event.updated.block == Blocks.emerald_block) {
            // Can happen with resets
            markedPositions.remove(event.pos)
            targetPosition = event.pos
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

    private val positions = listOf(
        BlockPos(68, 130, 50), BlockPos(66, 130, 50), BlockPos(64, 130, 50),
        BlockPos(68, 128, 50), BlockPos(66, 128, 50), BlockPos(64, 128, 50),
        BlockPos(68, 126, 50), BlockPos(66, 126, 50), BlockPos(64, 126, 50)
    )

    // Position of the pressure plate for auto
    private val standPosition = BlockPos(63.5, 127.0, 35.5)
    private val roomBoundingBox = AxisAlignedBB(20.0, 100.0, 30.0, 89.0, 151.0, 51.0)

    private const val ACTIVE_DEVICE_STRING = "§aDevice"
    private const val INACTIVE_DEVICE_STRING = "§cInactive"
}
