package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
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

    private val time = Clock(3000)

    @SubscribeEvent
    fun postMeta(event: PostEntityMetadata) {
        if (mc.theWorld.getEntityByID(event.packet.entityId).name.contains(mobName)) {
            if (!time.hasTimePassed()) return

            time.update()

            modMessage("§5$mobName has spawned!")
            PlayerUtils.alert("§5$mobName has spawned!")


        }
    }
}