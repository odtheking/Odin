package me.odinclient.features.impl.skyblock

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Module
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoExperiments : Module(
    name = "Auto Experiments",
    description = "Automatically click on the Chronomatron and Ultrasequencer experiments."
){
    private val delay by NumberSetting("Click Delay", 200, 0, 1000, 10, unit = "ms", desc = "Time in ms between automatic test clicks.")
    private val autoClose by BooleanSetting("Auto Close", true, desc = "Automatically close the GUI after completing the experiment.")
    private val serumCount by NumberSetting("Serum Count", 0, 0, 3, 1, desc = "Consumed Metaphysical Serum count.")
    private val getMaxXp by BooleanSetting("Get Max XP", false, desc = "Solve Chronomatron to 15 and Ultrasequencer to 20 for max XP.")

    private var ultrasequencerOrder = HashMap<Int, Int>()
    private val chronomatronOrder = ArrayList<Int>(28)
    private var lastClickTime = 0L
    private var hasAdded = false
    private var lastAdded = 0
    private var clicks = 0

    private fun reset() {
        ultrasequencerOrder.clear()
        chronomatronOrder.clear()
        hasAdded = false
        lastAdded = 0
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui == null) reset()
    }

    /**
     * Taken from [SBC](https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/features/EnchantingExperiments.kt)
     *
     * @author Harry282
     */
    @SubscribeEvent
    fun onGuiDraw(event: GuiEvent.DrawGuiBackground) {
        if (!LocationUtils.currentArea.isArea(Island.PrivateIsland)) return
        val gui = ((event.gui as? GuiChest)?.inventorySlots as? ContainerChest) ?: return

        when {
            gui.name.startsWith("Chronomatron (") -> solveChronomatron(gui.inventorySlots)
            gui.name.startsWith("Ultrasequencer (") -> solveUltraSequencer(gui.inventorySlots)
            else -> return
        }
    }

    private fun solveChronomatron(invSlots: List<Slot>) {
        val maxChronomatron = if (getMaxXp) 15 else 11 - serumCount
        if (invSlots[49].stack?.item == Item.getItemFromBlock(Blocks.glowstone) && invSlots[lastAdded].stack?.isItemEnchanted == false) {
            if (autoClose && chronomatronOrder.size > maxChronomatron) mc.thePlayer?.closeScreen()
            hasAdded = false
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
            windowClick(chronomatronOrder[clicks], ClickType.Middle)
            lastClickTime = System.currentTimeMillis()
            clicks++
        }
    }

    private fun solveUltraSequencer(invSlots: List<Slot>) {
        val maxUltraSequencer = if (getMaxXp) 20 else 9 - serumCount
        if (invSlots[49].stack?.item == Items.clock) hasAdded = false

        if (!hasAdded && invSlots[49].stack?.item == Item.getItemFromBlock(Blocks.glowstone)) {
            if (!invSlots[44].hasStack) return
            ultrasequencerOrder.clear()
            invSlots.forEach {
                if (it.slotNumber in 9..44 && it.stack?.item == Items.dye) ultrasequencerOrder[it.stack.stackSize - 1] = it.slotNumber
            }
            hasAdded = true
            clicks = 0
            if (ultrasequencerOrder.size > maxUltraSequencer && autoClose) mc.thePlayer?.closeScreen()
        }
        if (invSlots[49].stack?.item == Items.clock && ultrasequencerOrder.contains(clicks) && System.currentTimeMillis() - lastClickTime > delay) {
            ultrasequencerOrder[clicks]?.let { windowClick(it, ClickType.Middle) }
            lastClickTime = System.currentTimeMillis()
            clicks++
        }
    }
}
