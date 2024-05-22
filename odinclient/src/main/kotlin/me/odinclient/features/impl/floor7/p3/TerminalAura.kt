package me.odinclient.features.impl.floor7.p3

import me.odinmain.events.impl.*
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.features.impl.floor7.p3.TerminalTypes
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
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
    description = "Automatically interacts with inactive terminals in floor 7.",
    tag = TagType.RISKY
) {

    private val onGround: Boolean by BooleanSetting("On Ground", true)

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
    }

    @SubscribeEvent
    fun onPacketSent(event: PacketSentEvent) {
        val packet = event.packet
        if (packet !is C02PacketUseEntity) return
        val entity = packet.getEntityFromWorld(mc.theWorld) ?: return
        if (entity.name.noControlCodes != "Inactive Terminal") return
        if (!interactClock.hasTimePassed() || TerminalSolver.currentTerm != TerminalTypes.NONE) event.isCanceled = true else interactClock.update()
    }

    @SubscribeEvent
    fun onPacketReceived(event: PacketReceivedEvent) {
        if (event.packet !is S2DPacketOpenWindow) return
        val title = (event.packet as S2DPacketOpenWindow).windowTitle.formattedText.noControlCodes
        if (title == "Click the button on time!") interactClock.update()
    }

    @SubscribeEvent
    fun onEntityLoaded(event: PostEntityMetadata) {
        if (DungeonUtils.getPhase() != Island.M7P3) return
        val entity = (mc.theWorld.getEntityByID(event.packet.entityId))
        if (entity !is EntityArmorStand || entity.name.noControlCodes != "Inactive Terminal") return
        terminalEntityList.add(entity)
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (DungeonUtils.getPhase() != Island.M7P3 || mc.thePlayer.openContainer !is ContainerPlayer || (!mc.thePlayer.onGround && onGround) || !clickClock.hasTimePassed()) return
        val terminal = terminalEntityList.firstOrNull {
            mc.thePlayer.positionVector.addVec(y = mc.thePlayer.getEyeHeight())
                .distanceTo(Vec3(it.posX, it.posY + it.height / 2, it.posZ)) < 3.5
        } ?: return
        mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(terminal, C02PacketUseEntity.Action.INTERACT))
        clickClock.update()
    }

}