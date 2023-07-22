package me.odinclient.features.impl.qol

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.Utils.name
import me.odinclient.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CookieClicker : Module(
    "Cookie Clicker",
    category = Category.QOL
) {
    private val delay: Long by NumberSetting("Delay", 150, 50.0, 300.0, 5.0)
    private val cancelSound: Boolean by BooleanSetting("Cancel Sound")

    init {
        executor(delay = { delay }) {
            val container = mc.thePlayer.openContainer ?: return@executor
            if (container !is ContainerChest) return@executor

            val chestName = container.name
            if (chestName.startsWith("Cookie Clicker")) {
                windowClick(container.windowId, 13, 2, 3)
            }
        }
    }

    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundEvent) {
        if (!cancelSound) return

        val container = mc.thePlayer.openContainer ?: return
        if (container !is ContainerChest) return

        val chestName = container.lowerChestInventory.displayName.unformattedText
        if (!chestName.startsWith("Cookie Clicker")) return
        if (event.name == "random.eat" && event.sound.volume.toInt() == 1) {
            event.result = null // This should cancel the sound event
        }
    }
}