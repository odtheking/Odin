package me.odinclient.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.getPositionEyes
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import kotlin.math.cos
import kotlin.math.sin

object Camera : Module(
    name = "Camera",
    category = Category.RENDER,
    description = "Various camera improvements and settings."
) {
    private val frontCamera by BooleanSetting("No Front Camera", false, description = "Disables front camera.")
    private val cameraClip by BooleanSetting("Camera Clip", false, description = "Allows the camera to clip through blocks.")
    private val cameraDist by NumberSetting("Distance", 4f, 3.0, 12.0, 0.1, description = "The distance of the camera from the player.")
    private val customFOV by BooleanSetting("Custom FOV", description = "Allows you to change the FOV.")
    private val fov by NumberSetting("FOV", mc.gameSettings.fovSetting, 1f, 180f, 1f, description = "The field of view of the camera.").withDependency { customFOV }
    private val freelookDropdown by DropdownSetting("Freelook")
    private val toggle by DualSetting("Type", "Hold", "Toggle", false, description = "The type of freelook (Hold/Toggle).").withDependency { freelookDropdown }
    private val freelookKeybind by KeybindSetting("Freelook Key", Keyboard.KEY_NONE, description = "Keybind to toggle/ hold for freelook.")
        .withDependency { freelookDropdown }
        .onPress {
            if (!freelookToggled && enabled) enable()
            else if ((toggle || !enabled) && freelookToggled) disable()
    }
    @JvmStatic
    var freelookToggled = false
    private var cameraYaw = 0f
    private var cameraPitch = 0f
    private var lastPerspective = -1

    private var previousFov = mc.gameSettings.fovSetting

    override fun onEnable() {
        previousFov = mc.gameSettings.fovSetting
        super.onEnable()
    }

    override fun onDisable() {
        mc.gameSettings.fovSetting = previousFov
        super.onDisable()
    }

    @JvmStatic
    fun getCameraDistance(): Float {
        return if (enabled) cameraDist else 4f
    }

    @JvmStatic
    fun getCameraClipEnabled(): Boolean {
        return if (enabled) cameraClip else false
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        if (customFOV && mc.gameSettings.fovSetting != fov)
            mc.gameSettings.fovSetting = fov

        if (frontCamera && mc.gameSettings.thirdPersonView == 2)
            mc.gameSettings.thirdPersonView = 0

        if (!freelookKeybind.isDown() && freelookToggled && !toggle) disable()
    }

    private fun enable() {
        cameraYaw = mc.thePlayer.rotationYaw + 180
        cameraPitch = mc.thePlayer.rotationPitch
        freelookToggled = true
        lastPerspective = mc.gameSettings.thirdPersonView
        mc.gameSettings.thirdPersonView = 1
    }

    private fun disable() {
        freelookToggled = false
        mc.gameSettings.thirdPersonView = if (lastPerspective != -1) lastPerspective else 0
        lastPerspective = -1
    }

    @SubscribeEvent
    fun cameraSetup(e: EntityViewRenderEvent.CameraSetup) {
        if (!freelookToggled) return
        e.yaw = cameraYaw
        e.pitch = cameraPitch
    }

    @JvmStatic
    fun updateCameraAndRender(f2: Float, f3: Float) {
        if (!freelookToggled) return
        cameraYaw += f2 / 7
        cameraPitch = MathHelper.clamp_float((cameraPitch + f3 / 7), -90f, 90f)
    }

    @JvmStatic
    fun calculateCameraDistance(): Float {
        val eyes = getPositionEyes()
        var dist = getCameraDistance()
        var f2 = cameraPitch

        if (mc.gameSettings.thirdPersonView == 2) f2 += 180.0f

        val d4 = (sin(cameraYaw / 180.0f * Math.PI.toFloat()) * cos(f2 / 180.0f * Math.PI.toFloat())).toDouble() * dist
        val d5 = (-cos(cameraYaw / 180.0f * Math.PI.toFloat()) * cos(f2 / 180.0f * Math.PI.toFloat())).toDouble() * dist
        val d6 = (-sin(f2 / 180.0f * Math.PI.toFloat())).toDouble() * dist

        if (!cameraClip) repeat(8) {
            var f3 = ((it and 1) * 2 - 1).toFloat()
            var f4 = ((it shr 1 and 1) * 2 - 1).toFloat()
            var f5 = ((it shr 2 and 1) * 2 - 1).toFloat()
            f3 *= .1f
            f4 *= .1f
            f5 *= .1f
            val movingObjectPosition = mc.theWorld?.rayTraceBlocks(
                Vec3(eyes.xCoord + f3.toDouble(), eyes.yCoord + f4.toDouble(), eyes.zCoord + f5.toDouble()),
                Vec3(eyes.xCoord - d4 + f3.toDouble() + f5.toDouble(), eyes.yCoord - d6 + f4.toDouble(), eyes.zCoord - d5 + f5.toDouble())
            )

            if (movingObjectPosition != null) {
                val d7 = movingObjectPosition.hitVec.distanceTo(Vec3(eyes.xCoord, eyes.yCoord, eyes.zCoord))
                if (d7 < dist) dist = d7.toFloat()
            }
        }
        return dist
    }
}