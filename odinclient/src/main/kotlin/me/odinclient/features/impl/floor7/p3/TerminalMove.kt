package me.odinclient.features.impl.floor7.p3

import me.odinmain.events.impl.DrawGuiContainerScreenEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.font.OdinFont
import me.odinmain.utils.render.TextAlign
import me.odinmain.utils.render.TextPos
import me.odinmain.utils.render.scaleFactor
import me.odinmain.utils.render.text
import net.minecraft.client.settings.KeyBinding
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object TerminalMove : Module(
    name = "Terminal Move",
    category = Category.FLOOR7,
    description = ""
) {

    private val allowJump: Boolean by BooleanSetting("Allow Jumping", false)
    private val allowSneak: Boolean by BooleanSetting("Allow Sneaking", false)

    private val keyBindingList = arrayListOf(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindJump)

    @SubscribeEvent
    fun onDrawContainerGui(event: DrawGuiContainerScreenEvent) {
        if (event.container !is ContainerChest) return
        val containerName = (event.container as ContainerChest).lowerChestInventory.name
        if (!TerminalSolver.terminalNames.contains((containerName))) return
        for (keyBinding in keyBindingList) {
            if (((keyBinding == mc.gameSettings.keyBindJump) && !allowJump) || ((keyBinding == mc.gameSettings.keyBindSneak) && !allowSneak)) continue
            KeyBinding.setKeyBindState(keyBinding.keyCode, Keyboard.isKeyDown(keyBinding.keyCode))
        }
        mc.inGameHasFocus = true
        mc.mouseHelper.grabMouseCursor()
        text("In ${(event.container as ContainerChest).lowerChestInventory.name} Terminal", mc.displayWidth / 2 / scaleFactor, (mc.displayHeight / 2 - 24) / scaleFactor, ClickGUIModule.color, 8f, OdinFont.REGULAR, TextAlign.Middle, TextPos.Middle, true)
        event.isCanceled = true
    }

}