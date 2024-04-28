package me.odinclient.features.impl.skyblock

import me.odinclient.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.name
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ChocolateFactory : Module(
    "Chocolate Factory",
    description = "Automatically clicks the cookie in the Chocolate Factory menu.",
    category = Category.SKYBLOCK
) {
    private val delay: Long by NumberSetting("Delay", 150, 50, 300, 5)
    private val cancelSound: Boolean by BooleanSetting("Cancel Sound")

    init {
        execute(delay = { delay }) {
            val container = mc.thePlayer.openContainer as? ContainerChest ?: return@execute

            if (container.name == "Chocolate Factory") windowClick(13, 2, 3)
        }
    }

    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundEvent) {
        if (!cancelSound) return

        val container = mc.thePlayer.openContainer as? ContainerChest ?: return

        if (container.name == "Chocolate Factory") windowClick(13, 2, 3)

        if (event.name == "random.eat") event.result = null // This should cancel the sound event
    }
}