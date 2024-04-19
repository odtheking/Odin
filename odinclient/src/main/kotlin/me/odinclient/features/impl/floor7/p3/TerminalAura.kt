package me.odinclient.features.impl.floor7.p3

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.inventory.ContainerPlayer
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object TerminalAura : Module(
    name = "Terminal Aura",
    category = Category.FLOOR7,
    description = "Automatically interacts with inactive terminals in M7P3.",
    tag = TagType.RISKY
) {
    private val onGround: Boolean by BooleanSetting("On Ground", true)

    private val clickClock = Clock(1000)

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (DungeonUtils.getPhase() != Island.M7P3 || mc.thePlayer.openContainer !is ContainerPlayer || (!mc.thePlayer.onGround && onGround) || !clickClock.hasTimePassed()) return
        val terminal = mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()
            .filter { it.name.noControlCodes == "Inactive Terminal" }.firstOrNull {
                mc.thePlayer.positionVector.addVec(y = mc.thePlayer.getEyeHeight())
                    .distanceTo(Vec3(it.posX, it.posY + it.height / 2, it.posZ)) < 3.5
            } ?: return
        mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(terminal, C02PacketUseEntity.Action.INTERACT))
        clickClock.update()
    }
}