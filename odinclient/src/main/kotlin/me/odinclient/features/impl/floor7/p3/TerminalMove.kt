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

    private val moveinguiduringterms: Boolean by BooleanSetting("Show GUI", default = false, description = "Only allows movement in GUIs while in terminals and no other GUIs")
    private val moveinanygui: Boolean by BooleanSetting("GUI move anywhere", default = false, description = "Allows movement in every single GUI (DO NOT USE WITH THE OTHER SETTING)")

    private var originalMovementInput: MovementInput? = null
    private var customMovementInput: CustomMovementInput? = null

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onKeyInput(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        val mc = Minecraft.getMinecraft()
        if (!isInValidGUI(mc) || isInChatMenu(mc)) return

        val player = mc.thePlayer as? EntityPlayerSP ?: return

        if (originalMovementInput == null) {
            originalMovementInput = player.movementInput
        }
        if (player.movementInput !is CustomMovementInput) {
            customMovementInput = CustomMovementInput(originalMovementInput!!, player)
            player.movementInput = customMovementInput
        }
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Post) {
        if (DungeonUtils.getPhase() != Island.M7P3 || !isInTerminal() || event.type != RenderGameOverlayEvent.ElementType.ALL || moveinguiduringterms || moveinanygui) return
        val containerName = (mc.thePlayer.openContainer as ContainerChest).lowerChestInventory.name
        text(containerName, mc.displayWidth / 2 / scaleFactor, (mc.displayHeight / 2 + 32) / scaleFactor, ClickGUIModule.color, 8f, OdinFont.REGULAR, TextAlign.Middle, TextPos.Middle, true)
        text("Clicks left: ${TerminalSolver.clicksNeeded}", mc.displayWidth / 2 / scaleFactor, (mc.displayHeight / 2 + 64) / scaleFactor, ClickGUIModule.color, 8f, OdinFont.REGULAR, TextAlign.Middle, TextPos.Middle, true)
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (DungeonUtils.getPhase() != Island.M7P3 || !isInTerminal() || moveinguiduringterms || moveinanygui) return
        mc.displayGuiScreen(null)
    }

    private fun isInTerminal(): Boolean {
        if (mc.thePlayer == null || mc.thePlayer.openContainer !is ContainerChest) return false
        return TerminalTypes.entries.stream().anyMatch { prefix: TerminalTypes? -> (mc.thePlayer.openContainer as ContainerChest).lowerChestInventory.name.startsWith(prefix?.guiName!!) }
    }

    private fun isInValidGUI(mc: Minecraft): Boolean {
        val currentScreen = mc.currentScreen
        return (currentScreen != null && currentScreen !is net.minecraft.client.gui.GuiChat && moveinanygui) ||
                (isInTerminal() && moveinguiduringterms)
    }

    private fun isInChatMenu(mc: Minecraft): Boolean {
        val currentScreen = mc.currentScreen
        return currentScreen is net.minecraft.client.gui.GuiChat
    }

    class CustomMovementInput(base: MovementInput, private val player: EntityPlayerSP) : MovementInput() {
        init {
            moveStrafe = base.moveStrafe
            moveForward = base.moveForward
            sneak = base.sneak
            jump = base.jump
        }

        override fun updatePlayerMoveState() {
            val mc = Minecraft.getMinecraft()
            if (isInChatMenu(mc)) return

            moveStrafe = 0.0f
            moveForward = 0.0f
            sneak = false
            jump = false

            if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
                moveForward += 1.0f
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
                moveForward -= 1.0f
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
                moveStrafe += 1.0f
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
                moveStrafe -= 1.0f
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                jump = true
            }

            // Handle sneak (SHIFT)
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                sneak = true
                moveForward *= 0.3f
            } else {
                sneak = false
            }
        }

        private fun isInChatMenu(mc: Minecraft): Boolean {
            val currentScreen = mc.currentScreen
            return currentScreen is net.minecraft.client.gui.GuiChat
        }
    }
}
