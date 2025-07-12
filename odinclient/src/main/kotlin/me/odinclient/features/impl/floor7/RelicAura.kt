package me.odinclient.features.impl.floor7

import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.features.Module
import me.odinmain.utils.component1
import me.odinmain.utils.component2
import me.odinmain.utils.component3
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object RelicAura : Module(
    name = "Relic Aura",
    description = "Automatically picks up relics in the Wither King boss."
){
    private val distance by NumberSetting("Distance", 3f, 1.0, 6.0, 0.1, desc = "The distance to the relic to pick it up.")

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P5) return
        val armorStand = mc.theWorld?.loadedEntityList?.firstOrNull {
            it is EntityArmorStand && it.inventory?.get(4)?.displayName?.contains("Relic") == true && mc.thePlayer.getDistanceToEntity(it) < distance } ?: return
        interactWithEntity(armorStand)
    }

    private fun interactWithEntity(entity: Entity) {
        val (x, y, z) = mc.objectMouseOver?.hitVec ?: return
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, Vec3(x - entity.posX, y - entity.posY, z - entity.posZ)))
    }
}