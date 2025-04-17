package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.skyblock.sendChatMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MobSpawn: Module(
    name = "Mob Spawn",
    desc = "Sends a message whenever a mob spawns."
) {
    private val mobName by StringSetting("Mob Name", "MobName", 40, desc = "Message sent when mob is detected as spawned.")
    private val soundOnly by BooleanSetting("Sound Only", false, desc = "Only plays sound when mob spawns.")
    private val delay by NumberSetting("Time between alerts", 3000L, 10, 10000, 10, desc = "Time between alerts.", unit = "ms")
    private val ac by BooleanSetting("All Chat", false , desc = "Send message in all chat.")
    private val pc by BooleanSetting("Party Chat", false, desc = "Send message in party chat.")

    private val time = Clock(delay)

    @SubscribeEvent
    fun postMeta(event: PostEntityMetadata) {
        val entity = mc.theWorld?.getEntityByID(event.packet.entityId) ?: return
        if (!entity.name.contains(mobName, true) || !time.hasTimePassed(delay)) return
        time.update()

        modMessage("§5$mobName has spawned!")
        PlayerUtils.alert("§5$mobName has spawned!", playSound = !soundOnly)
        if (ac) sendChatMessage("$mobName spawned at: ${PlayerUtils.getPositionString()}")
        if (pc) partyMessage("$mobName spawned at: x: ${PlayerUtils.getPositionString()}")
    }
}