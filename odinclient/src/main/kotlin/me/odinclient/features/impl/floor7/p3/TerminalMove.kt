package me.odinclient.features.impl.floor7.p3

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.font.OdinFont
import me.odinmain.utils.render.*
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.Island
import me.odinmain.features.settings.Setting.Companion.withDependency
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.MovementInput
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.input.Keyboard

object TerminalMove : Module(
    name = "Terminal Move",
    category = Category.FLOOR7,
    description = "Move in terminals without GUI hotbar keys.",
    tag = TagType.RISKY
) {

    private val moveinguiduringterms: Boolean by BooleanSetting("Show GUI while still allowing movement", default = true, description = "Only allows movement in GUIs while in terminals and no other GUIs")

    @SubscribeEvent
    fun onDrawOverlay(event: RenderGameOverlayEvent.Post) {
        if (DungeonUtils.getPhase() != Island.M7P3 || moveinguiduringterms || !isInTerminal() || event.type != RenderGameOverlayEvent.ElementType.ALL) return
        val containerName = (mc.thePlayer.openContainer as ContainerChest).lowerChestInventory.name
        text(containerName, mc.displayWidth / 2 / scaleFactor, (mc.displayHeight / 2 + 32) / scaleFactor, ClickGUIModule.color, 8f, OdinFont.REGULAR, TextAlign.Middle, TextPos.Middle, true)
        text("Clicks left: ${TerminalSolver.clicksNeeded}", mc.displayWidth / 2 / scaleFactor, (mc.displayHeight / 2 + 64) / scaleFactor, ClickGUIModule.color, 8f, OdinFont.REGULAR, TextAlign.Middle, TextPos.Middle, true)
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (DungeonUtils.getPhase() != Island.M7P3 || moveinguiduringterms || !isInTerminal()) return
        mc.displayGuiScreen(null)
    }

    private fun isInTerminal(): Boolean {
        if (mc.thePlayer == null || mc.thePlayer.openContainer !is ContainerChest) return false
        return TerminalTypes.entries.stream().anyMatch { prefix: TerminalTypes? -> (mc.thePlayer.openContainer as ContainerChest).lowerChestInventory.name.startsWith(prefix?.guiName!!) }
    }

    private fun executeIfInTerminal() {
        if (isInTerminal() && moveinguiduringterms) {
            CustomEventHandler().register()
        }
    }

    class CustomEventHandler {

        private var originalMovementInput: MovementInput? = null
        private var customMovementInput: CustomMovementInput? = null

        fun register() {
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

                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
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
}
