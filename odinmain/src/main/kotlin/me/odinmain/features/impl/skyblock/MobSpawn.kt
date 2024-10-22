package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MobSpawn: Module(
    name = "Mob Spawn",
    category = Category.SKYBLOCK,
    description = "Sends a message whenever a mob spawns."
) {
    private val mobName by StringSetting("Mob Name", "MobName", 40, description = "Message sent when mob is detected as spawned.")
    private val soundOnly by BooleanSetting("Sound Only", false, description = "Only plays sound when mob spawns.")
    private val delay by NumberSetting("Time between alerts", 3000L, 10, 10000, 10, description = "Time between alerts.", unit = "ms")
    private val ac by BooleanSetting("All Chat", false , description = "Send message in all chat.")
    private val pc by BooleanSetting("Party Chat", false, description = "Send message in party chat.")

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