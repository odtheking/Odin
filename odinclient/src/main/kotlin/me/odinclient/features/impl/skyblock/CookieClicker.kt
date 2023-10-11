package me.odinclient.features.impl.skyblock

import me.odinclient.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.Utils.name
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CookieClicker : Module(
    "Cookie Clicker",
    description = "Automatically clicks the cookie in the Cookie Clicker menu.",
    category = Category.SKYBLOCK
) {
    private val delay: Long by NumberSetting("Delay", 150, 50, 300, 5)
    private val cancelSound: Boolean by BooleanSetting("Cancel Sound")

    init {
        execute(delay = { delay }) {
            val container = mc.thePlayer.openContainer ?: return@execute
            if (container !is ContainerChest) return@execute

            val chestName = container.name
            if (chestName.startsWith("Cookie Clicker")) {
                windowClick(13, 2, 3)
            }
        }
    }

    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundEvent) {
        if (!cancelSound) return

        val container = mc.thePlayer?.openContainer ?: return
        if (container !is ContainerChest) return

        val chestName = container.lowerChestInventory.displayName.unformattedText
        if (!chestName.startsWith("Cookie Clicker")) return
        if (event.name == "random.eat" && event.sound.volume.toInt() == 1) {
            event.result = null // This should cancel the sound event
        }
    }
}