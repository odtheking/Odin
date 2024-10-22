package me.odinclient.features.impl.floor7.p3

import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.inventory.ContainerPlayer
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object TerminalAura : Module(
    name = "Terminal Aura",
    category = Category.FLOOR7,
    description = "Automatically interacts with inactive terminals.",
    tag = TagType.RISKY
) {
    private val onGround by BooleanSetting("On Ground", true, description = "Only click when on the ground.")
    private val distance by NumberSetting("Distance", 3.5, 1.0, 4.5, 0.1, description = "The distance to click the terminal.")

    private val clickClock = Clock(1000)
    private val interactClock = Clock(500)
    private val terminalEntityList = mutableListOf<EntityArmorStand>()

    init {
        onWorldLoad {
            terminalEntityList.clear()
        }

        onMessage(Regex("This Terminal doesn't seem to be responsive at the moment.")) {
            interactClock.update()
        }

        onPacket(S2DPacketOpenWindow::class.java) {
            if (it.windowTitle.formattedText.noControlCodes == "Click the button on time!") interactClock.update()
        }
    }

    @SubscribeEvent
    fun onPacketSent(event: PacketSentEvent) {
        (event.packet as? C02PacketUseEntity)?.getEntityFromWorld(mc.theWorld)?.let {
            if (it.name.noControlCodes != "Inactive Terminal") return
            if (!interactClock.hasTimePassed() || TerminalSolver.currentTerm.type != TerminalTypes.NONE) event.isCanceled = true else interactClock.update()
        }
    }

    @SubscribeEvent
    fun onEntityLoaded(event: PostEntityMetadata) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return
        val entity = mc.theWorld?.getEntityByID(event.packet.entityId) as? EntityArmorStand ?: return
        if (entity.name.noControlCodes == "Inactive Terminal") terminalEntityList.add(entity)
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3 || mc.thePlayer?.openContainer !is ContainerPlayer || (!mc.thePlayer.onGround && onGround) || !clickClock.hasTimePassed()) return
        val terminal = terminalEntityList.firstOrNull {
            mc.thePlayer.positionVector.addVec(y = mc.thePlayer.getEyeHeight()).distanceTo(Vec3(it.posX, it.posY + it.height / 2, it.posZ)) < distance
        } ?: return
        mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(terminal, C02PacketUseEntity.Action.INTERACT))
        clickClock.update()
    }
}