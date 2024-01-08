package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MobSpawn: Module(
    "Mob Spawn",
    category = Category.SKYBLOCK,
    description = "Sends a message whenever a mob spawns",
    tag = TagType.NEW
) {
    private val mobName: String by StringSetting("Mob Name", "MobName", 40, description = "Message sent when mob is detected as spawned")
    private val soundOnly: Boolean by BooleanSetting("Sound Only", false, description = "Only plays sound when mob spawns")
    private val delay: Long by NumberSetting("Time between alerts", 3000, 10.0, 10000.0, 10.0)

    private val time = Clock(delay)

    @SubscribeEvent
    fun postMeta(event: PostEntityMetadata) {
        if (mc.theWorld.getEntityByID(event.packet.entityId).name.contains(mobName)) {
            if (!time.hasTimePassed()) return

            time.update()

            modMessage("§5$mobName has spawned!")
            PlayerUtils.alert("§5$mobName has spawned!", soundOnly)


        }
    }
}