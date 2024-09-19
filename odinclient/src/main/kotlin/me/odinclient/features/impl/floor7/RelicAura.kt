package me.odinclient.features.impl.floor7

import me.odinmain.features.Category
import me.odinmain.features.Module
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
    category = Category.FLOOR7,
    description = "Automatically picks up relics in the Wither King boss.",
    tag = TagType.RISKY
){
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P5) return
        val armorStands = mc.theWorld?.loadedEntityList?.filterIsInstance<EntityArmorStand>()
            ?.firstOrNull { it.inventory?.get(4)?.displayName?.contains("Relic") == true && mc.thePlayer.getDistanceToEntity(it) < 3 } ?: return
        interactWithEntity(armorStands)
    }

    private fun interactWithEntity(entity: Entity) {
        val objectMouseOver = mc.objectMouseOver?.hitVec ?: return
        val dx = objectMouseOver.xCoord - entity.posX
        val dy = objectMouseOver.yCoord - entity.posY
        val dz = objectMouseOver.zCoord - entity.posX
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, Vec3(dx, dy, dz)))
    }
}