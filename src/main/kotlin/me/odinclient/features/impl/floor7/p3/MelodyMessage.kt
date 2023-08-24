package me.odinclient.features.impl.floor7.p3

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.StringSetting
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.ItemUtils.chest
import me.odinclient.utils.skyblock.ScoreboardUtils.stripControlCodes
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MelodyMessage : Module(
    "Melody Message",
    description = "Announces that the Melody terminal has been opened in party chat",
    category = Category.FLOOR7
) {

    private val melodyAnnouncement: String by StringSetting("Melody terminal announcement", "Meowlody on me!", 40, description = "Announces that the Melody terminal has been opened in party chat; leave blank to disable.")


    private val completedStageRegex = Regex("^[\\w]{2,16} (?:completed|activated) a (?:lever|terminal|device)! \\((?:[07]/7|[08]/8)\\)")
    private var hasSaidMeowlody = false

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        hasSaidMeowlody = false
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (completedStageRegex.matches(unformatted)) {
            hasSaidMeowlody = false
        }
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui == null) return
        if (event.gui.chest?.lowerChestInventory?.name == "Click the button on time!") {
            if (!hasSaidMeowlody && melodyAnnouncement.isNotBlank()) {
                ChatUtils.partyMessage(melodyAnnouncement)
                hasSaidMeowlody = true
            }
        }
    }
}