package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.MovementInput
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object MoveInGUI : Module(
    name = "Move In GUI",
    category = Category.SKYBLOCK,
    description = "WILL LIMBO IF USED INCORRECTLY! You must change lobbies after disabling. Allows movement while any GUI is open."
) {
    private lateinit var originalMovementInput: MovementInput
    private lateinit var customMovementInput: CustomMovementInput

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onKeyInput(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        val mc = Minecraft.getMinecraft()
        if (!isInValidGUI(mc) || isInChatMenu(mc)) return

        val player = mc.thePlayer as? EntityPlayerSP ?: return

        originalMovementInput = player.movementInput
        if (originalMovementInput !is CustomMovementInput) {
            customMovementInput = CustomMovementInput(player.movementInput, player)
            player.movementInput = customMovementInput
        }
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        val mc = Minecraft.getMinecraft()
        if (isInValidGUI(mc)) {
            val text = ""
            val screenWidth = event.resolution.scaledWidth
            val screenHeight = event.resolution.scaledHeight
            val fontRenderer = mc.fontRendererObj
            val xPos = screenWidth / 2 - fontRenderer.getStringWidth(text) / 2
            val yPos = screenHeight / 2 + 64
            fontRenderer.drawStringWithShadow(text, xPos.toFloat(), yPos.toFloat(), 0xFFFFFF)
        }
    }

    private fun isInValidGUI(mc: Minecraft): Boolean {
        val currentScreen = mc.currentScreen
        return currentScreen != null && currentScreen !is net.minecraft.client.gui.GuiChat
    }

    private fun isInChatMenu(mc: Minecraft): Boolean {
        val currentScreen = mc.currentScreen
        return currentScreen is net.minecraft.client.gui.GuiChat
    }

    class CustomMovementInput(base: MovementInput, private val player: EntityPlayerSP) : MovementInput() {
        private var forwardKeyDown = false
        private var backKeyDown = false
        private var leftKeyDown = false
        private var rightKeyDown = false
        private var jumpKeyDown = false
        private var sneakKeyDown = false

        init {
            moveStrafe = base.moveStrafe
            moveForward = base.moveForward
            sneak = base.sneak
            jump = base.jump
        }

        override fun updatePlayerMoveState() {
            val mc = Minecraft.getMinecraft()
            if (isInChatMenu(mc)) return

            super.updatePlayerMoveState()

            moveStrafe = 0.0f
            moveForward = 0.0f
            sneak = false
            jump = false

            if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
                moveForward += 1.0f
                forwardKeyDown = true
            } else {
                forwardKeyDown = false
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
                moveForward -= 1.0f
                backKeyDown = true
            } else {
                backKeyDown = false
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
                moveStrafe -= 1.0f
                leftKeyDown = true
            } else {
                leftKeyDown = false
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
                moveStrafe += 1.0f
                rightKeyDown = true
            } else {
                rightKeyDown = false
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                jump = true
                jumpKeyDown = true
            } else {
                jumpKeyDown = false
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                sneak = true
                sneakKeyDown = true
                player.capabilities.isFlying = false
                player.motionX *= 0.3
                player.motionZ *= 0.3
            } else {
                sneak = false
                sneakKeyDown = false
            }
        }

        private fun isInChatMenu(mc: Minecraft): Boolean {
            val currentScreen = mc.currentScreen
            return currentScreen is net.minecraft.client.gui.GuiChat
        }
    }
}
