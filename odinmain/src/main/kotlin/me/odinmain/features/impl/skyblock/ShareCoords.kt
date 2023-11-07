package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.floor
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ShareCoords: Module(
    "Share Coords",
    category = Category.SKYBLOCK,
    description = "Sends a message whenever a mob spawns",
    tag = TagType.NEW
) {
    private val mobName: String by StringSetting("Mob Name", "ZZZ??", 40, description = "Message sent when mob is detected as spawned")
    private val ac: Boolean by BooleanSetting("All Chat")
    private val pc: Boolean by BooleanSetting("Party Chat")

    private val time = Clock(3000)

    @SubscribeEvent
    fun postMeta(event: PostEntityMetadata) {
        if (mc.theWorld.getEntityByID(event.packet.entityId).name.contains(mobName)) {
            if (!time.hasTimePassed()) return

            time.update()

            ChatUtils.modMessage("Mob has spawned!")
            PlayerUtils.alert("§5Mob has spawned!")

            if (ac) ChatUtils.sendChatMessage("Mob spawned at: x: ${posX.floor()}, y: ${posY.floor()}, z: ${posZ.floor()}")
            if (pc) ChatUtils.partyMessage("Mob spawned at: x: ${posX.floor()}, y: ${posY.floor()}, z: ${posZ.floor()}")
        }
    }
}