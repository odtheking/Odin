package me.odinclient.features.impl.floor7.p3

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver.terminalNames
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.font.OdinFont
import me.odinmain.utils.render.TextAlign
import me.odinmain.utils.render.TextPos
import me.odinmain.utils.render.scaleFactor
import me.odinmain.utils.render.text
import net.minecraft.client.settings.KeyBinding
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

object TerminalMove : Module(
    name = "Terminal Move",
    category = Category.FLOOR7,
    description = ""
) {

    private val keyBindingList: ArrayList<KeyBinding> = arrayListOf(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSprint)

    @SubscribeEvent
    fun onDrawOverlay(event: RenderGameOverlayEvent.Post) {
        if (!isInTerminal() || event.type != RenderGameOverlayEvent.ElementType.ALL) return
        val containerName = (mc.thePlayer.openContainer as ContainerChest).lowerChestInventory.name
        text(containerName, mc.displayWidth / 2 / scaleFactor, (mc.displayHeight / 2 + 32) / scaleFactor, ClickGUIModule.color, 8f, OdinFont.REGULAR, TextAlign.Middle, TextPos.Middle, true)
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!isInTerminal()) return
        mc.currentScreen = null
        mc.inGameHasFocus = true
        mc.mouseHelper.grabMouseCursor()
    }

    private fun isInTerminal(): Boolean {
        if (mc.thePlayer == null || mc.thePlayer.openContainer !is ContainerChest) return false
        return terminalNames.stream().anyMatch { prefix: String? -> (mc.thePlayer.openContainer as ContainerChest).lowerChestInventory.name.startsWith(prefix!!) }
    }

    fun disableGuiHotbarKeysHook(cir: CallbackInfoReturnable<Boolean>) {
        if (isInTerminal()) cir.returnValue = false
    }

}