package me.odinclient.features.impl.render

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.LocationUtils.inSkyblock
import me.odinclient.utils.skyblock.ScoreboardUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object HideServerID : Module(
    "Hide Server ID",
    category = Category.RENDER
) {

    private val dateRegex = Regex("§7(\\d{2}/\\d{2}/\\d{2}) §8.+")

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!inSkyblock || event.phase != TickEvent.Phase.START) return
        mc.theWorld?.scoreboard?.teams?.find { team ->
            team.teamName == "team_${ScoreboardUtils.sidebarLines.indexOfFirst { dateRegex.matches(it) } + 1}"
        }?.run {
            setNamePrefix(colorPrefix.substringBefore(" §8"))
            setNameSuffix("")
        }
    }
}
