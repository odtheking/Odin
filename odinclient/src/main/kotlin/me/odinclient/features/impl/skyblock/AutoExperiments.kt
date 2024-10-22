package me.odinclient.features.impl.skyblock

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.LocationUtils.inSkyblock
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoExperiments : Module(
    name = "Auto Experiments",
    category = Category.SKYBLOCK,
    description = "Automatically click on the Chronomatron and Ultrasequencer experiments."
){
    private val delay by NumberSetting("Click Delay", 200, 0, 1000, 10, unit = "ms", description = "Time in ms between automatic test clicks.")
    private val autoClose by BooleanSetting("Auto Close", true, description = "Automatically close the GUI after completing the experiment.")
    private val serumCount by NumberSetting("Serum Count", 0, 0, 3, 1, description = "Consumed Metaphysical Serum count.")

    private var currentExperiment = ExperimentType.NONE
    private var hasAdded = false
    private var clicks = 0
    private var lastClickTime: Long = 0
    private val chronomatronOrder = ArrayList<Int>(28)
    private var lastAdded = 0
    private var ultrasequencerOrder = HashMap<Int, Int>()

    private fun reset() {
        currentExperiment = ExperimentType.NONE
        hasAdded = false
        chronomatronOrder.clear()
        lastAdded = 0
        ultrasequencerOrder.clear()
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiEvent.GuiLoadedEvent) {
        reset()
        if (!LocationUtils.currentArea.isArea(Island.PrivateIsland)) return
        val chestName = event.name

        currentExperiment = when {
            chestName.startsWith("Chronomatron") -> ExperimentType.CHRONOMATRON
            chestName.startsWith("Ultrasequencer") -> ExperimentType.ULTRASEQUENCER
            else -> ExperimentType.NONE
        }
    }

    /**
     * Taken from [SBC](https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/features/EnchantingExperiments.kt)
     *
     * @author Harry282
     */
    @SubscribeEvent
    fun onGuiDraw(event: GuiEvent.DrawGuiContainerScreenEvent) {
        if (!inSkyblock) return
        val invSlots = ((event.gui as? GuiChest)?.inventorySlots as? ContainerChest)?.inventorySlots?.takeIf { it.size >= 54 } ?: return
        when (currentExperiment) {
            ExperimentType.CHRONOMATRON -> solveChronomatron(invSlots)
            ExperimentType.ULTRASEQUENCER -> solveUltraSequencer(invSlots)
            else -> return
        }
    }

    private fun solveChronomatron(invSlots: List<Slot>) {
        if (invSlots[49].stack?.item == Item.getItemFromBlock(Blocks.glowstone) && invSlots[lastAdded].stack?.isItemEnchanted == false) {
            hasAdded = false
            if (chronomatronOrder.size > 11 - serumCount && autoClose) mc.thePlayer?.closeScreen()
        }
        if (!hasAdded && invSlots[49].stack?.item == Items.clock) {
            invSlots.find { it.slotNumber in 10..43 && it.stack?.isItemEnchanted == true }?.let {
                chronomatronOrder.add(it.slotNumber)
                lastAdded = it.slotNumber
                hasAdded = true
                clicks = 0
            }
        }
        if (hasAdded && invSlots[49].stack?.item == Items.clock && chronomatronOrder.size > clicks && System.currentTimeMillis() - lastClickTime > delay) {
            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, chronomatronOrder[clicks], 2, 3, mc.thePlayer)
            lastClickTime = System.currentTimeMillis()
            clicks++
        }
    }

    private fun solveUltraSequencer(invSlots: List<Slot>) {
        if (invSlots[49].stack?.item == Items.clock) hasAdded = false

        if (!hasAdded && invSlots[49].stack?.item == Item.getItemFromBlock(Blocks.glowstone)) {
            if (!invSlots[44].hasStack) return
            ultrasequencerOrder.clear()
            invSlots.forEach {
                if (it.slotNumber in 9..44 && it.stack?.item == Items.dye) ultrasequencerOrder[it.stack.stackSize - 1] = it.slotNumber
            }
            hasAdded = true
            clicks = 0
            if (ultrasequencerOrder.size > 9 - serumCount && autoClose) mc.thePlayer?.closeScreen()
        }
        if (invSlots[49].stack?.item == Items.clock && ultrasequencerOrder.contains(clicks) && System.currentTimeMillis() - lastClickTime > delay) {
            ultrasequencerOrder[clicks]?.let {
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, it, 2, 3, mc.thePlayer)
            }
            lastClickTime = System.currentTimeMillis()
            clicks++
        }
    }

    private enum class ExperimentType {
        CHRONOMATRON,
        ULTRASEQUENCER,
        NONE
    }
}
