package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.StringSetting
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.entity.monster.EntityZombie
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MimicMessage : Module(
    "Mimic Message",
    description = "Send message in party chat when mimic is killed.",
    category = Category.DUNGEON
) {
    private val mimicMessage: String by StringSetting("Mimic Message", "Mimic Killed!", 128, description = "Message sent when mimic is detected as killed")
    val reset: () -> Unit by ActionSetting("Send message", description = "Sends Mimic killed message in party chat.") {
        partyMessage(mimicMessage)
    }
    private var mimicKilled = false

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        if (!DungeonUtils.inDungeons || mimicKilled) return
        val entity = event.entity as? EntityZombie ?: return
        if (entity.isChild && (0..3).all { entity.getCurrentArmor(it) == null }) {
            mimicKilled = true
            partyMessage(mimicMessage)
        }
    }

    override fun onKeybind() {
        reset()
    }
    init {
        onWorldLoad {
            mimicKilled = false
        }
    }
}