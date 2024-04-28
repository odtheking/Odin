package me.odinclient.features.impl.skyblock

import me.odinclient.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.lore
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ChocolateFactory : Module(
    "Chocolate Factory",
    description = "Automatically clicks the cookie in the Chocolate Factory menu.",
    category = Category.SKYBLOCK
) {
    private val clickFactory: Boolean by BooleanSetting("Click Factory", false, description = "Click the cookie in the Chocolate Factory menu.")
    private val autoUpgrade: Boolean by BooleanSetting("Auto Upgrade", false, description = "Automatically upgrade the worker.")
    private val delay: Long by NumberSetting("Delay", 150, 50, 300, 5)
    private val cancelSound: Boolean by BooleanSetting("Cancel Sound")

    private var chocolate = 0
    private var chocoProduction = 0f

    init {
        execute(delay = { delay }) {
            val container = mc.thePlayer?.openContainer as? ContainerChest ?: return@execute
            if (container.name != "Chocolate Factory") return@execute

            if (clickFactory) windowClick(13, 2, 3)
        }

        execute(2000) {
            val container = mc.thePlayer.openContainer as? ContainerChest ?: return@execute
            if (container.name != "Chocolate Factory") return@execute

            val choco = container.getSlot(13)?.stack ?: return@execute

            chocolate = choco.displayName.noControlCodes.replace(Regex("\\D"), "").toInt()
            chocoProduction = choco.lore.find { it.endsWith("§8per second") }?.noControlCodes?.replace(",", "")?.toFloatOrNull() ?: 0f

            findWorker(container)

            if (chocolate > bestCost && autoUpgrade) {
                windowClick(bestWorker, 2, 3)
                modMessage("Upgraded worker to $bestWorker")
            }
        }
    }

    private var bestWorker = 29
    private var bestCost = 0

    private fun findWorker(container: Container) {
        val items = container.inventory ?: return
        val workers = mutableListOf<List<String?>>()
        for (i in 29 until 34) {
            workers.add(items[i]?.lore ?: return)
        }

        var maxValue = 0
        for (i in 0 until 5) {
            val worker = workers[i]
            val index = worker.indexOfFirst { it?.contains("Cost") == true }.takeIf { it != -1} ?: continue
            val cost = worker[index + 1]?.noControlCodes?.replace(Regex("\\D"), "")?.toIntOrNull() ?: continue
            val value = (i + 1).toFloat() / cost
            if (value > maxValue) {
                bestWorker = 29 + i
                maxValue = value.toInt()
                bestCost = cost
            }
        }
    }

    @SubscribeEvent
    fun guiLoad(event: GuiEvent.GuiLoadedEvent) {
        if (event.name == "Chocolate Factory") findWorker(event.gui)
    }


    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundEvent) {
        if (!cancelSound) return

        val container = mc.thePlayer?.openContainer as? ContainerChest ?: return

        if (container.name == "Chocolate Factory") windowClick(13, 2, 3)

        if (event.name == "random.eat") event.result = null // This should cancel the sound event
    }


}