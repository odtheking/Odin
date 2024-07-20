package me.odinclient.features.impl.floor7.p3

import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TerminalMove : Module(
    name = "Terminal Move",
    description = "Move in terminals without GUI hotbar keys.",
    tag = TagType.RISKY
) {

    @SubscribeEvent
    fun onDrawOverlay(event: RenderGameOverlayEvent.Post) {
        if (DungeonUtils.getPhase() != M7Phases.P3 || !isInTerminal() || event.type != RenderGameOverlayEvent.ElementType.ALL) return
        val containerName = (mc.thePlayer.openContainer as ContainerChest).lowerChestInventory.name
        //text(containerName, mc.displayWidth / 2 / scaleFactor, (mc.displayHeight / 2 + 32) / scaleFactor, ClickGUI.oldColor, 8f, 0, TextAlign.Middle, TextPos.Middle, true)
        //text("Clicks left: ${TerminalSolver.clicksNeeded}", mc.displayWidth / 2 / scaleFactor, (mc.displayHeight / 2 + 64) / scaleFactor, ClickGUI.oldColor, 8f, 0, TextAlign.Middle, TextPos.Middle, true)
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (DungeonUtils.getPhase() != M7Phases.P3 || !isInTerminal()) return
        mc.displayGuiScreen(null)
    }

    private fun isInTerminal(): Boolean {
        if (mc.thePlayer == null || mc.thePlayer.openContainer !is ContainerChest) return false
        return TerminalTypes.entries.stream().anyMatch { prefix: TerminalTypes? -> (mc.thePlayer.openContainer as ContainerChest).lowerChestInventory.name.startsWith(prefix?.guiName!!) }
    }
}