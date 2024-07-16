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
    private val mobName: String by StringSetting("Mob Name", "MobName", 40, description = "Message sent when mob is detected as spawned")
    private val soundOnly: Boolean by BooleanSetting("Sound Only", false, description = "Only plays sound when mob spawns")
    private val delay: Long by NumberSetting("Time between alerts", 3000, 10.0, 10000.0, 10.0, description = "Time between alerts in milliseconds")
    private val ac: Boolean by BooleanSetting("All Chat", false , description = "Send message in all chat")
    private val pc: Boolean by BooleanSetting("Party Chat", false, description = "Send message in party chat")

    private val time = Clock(delay)

    @SubscribeEvent
    fun postMeta(event: PostEntityMetadata) {
        if (!mc.theWorld.getEntityByID(event.packet.entityId).name.contains(mobName) || !time.hasTimePassed()) return
        time.update()

        modMessage("§5$mobName has spawned!")
        PlayerUtils.alert("§5$mobName has spawned!", playSound = !soundOnly)
        if (ac) sendChatMessage("$mobName spawned at: ${PlayerUtils.getPositionString()}")
        if (pc) partyMessage("$mobName spawned at: x: ${PlayerUtils.getPositionString()}")
    }
}