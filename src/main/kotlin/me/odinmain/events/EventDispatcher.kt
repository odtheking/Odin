package me.odinmain.events

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.isSecret
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.entity.item.EntityItem
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EventDispatcher {

    private val drops = listOf(
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion", "Healing Potion VIII Splash Potion", "Healing VIII Splash Potion", "Healing 8 Splash Potion",
        "Decoy", "Inflatable Jerry", "Spirit Leap", "Trap", "Training Weights", "Defuse Kit", "Dungeon Chest Key", "Treasure Talisman", "Revive Stone", "Architect's First Draft"
    )

    /**
     * Dispatches [SecretPickupEvent.Item]
     */
    @SubscribeEvent
    fun onRemoveEntity(event: EntityLeaveWorldEvent) {
        if (!inDungeons || event.entity !is EntityItem || !event.entity.entityItem.displayName.noControlCodes.containsOneOf(drops, true) || mc.thePlayer.getDistanceToEntity(event.entity) > 6) return
        SecretPickupEvent.Item(event.entity).postAndCatch()
    }

    /**
     * Dispatches [SecretPickupEvent.Interact]
     */
    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        with(event.packet) {
            if (inDungeons && this is C08PacketPlayerBlockPlacement && position != null &&
                isSecret(mc.theWorld?.getBlockState(position) ?: return, position))
                    SecretPickupEvent.Interact(position, mc.theWorld?.getBlockState(position) ?: return).postAndCatch()
        }
    }

    /**
     * Dispatches [ChatPacketEvent], [RealServerTick], and [SecretPickupEvent.Bat]
     */
    @SubscribeEvent
    fun onPacket(event: PacketReceivedEvent) {
        if (event.packet is S29PacketSoundEffect && event.packet.soundName == "mob.bat.death") SecretPickupEvent.Bat(event.packet).postAndCatch()

        if (event.packet is S32PacketConfirmTransaction) RealServerTick.postAndCatch()

        if (event.packet !is S02PacketChat || !ChatPacketEvent(event.packet.chatComponent.unformattedText.noControlCodes).postAndCatch()) return
        event.isCanceled = true
    }

    private val nextTime = Clock()

    /**
     * Dispatches [ServerTickEvent]
     */
    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (nextTime.hasTimePassed((1000L / ServerUtils.averageTps).toLong(), setTime = true)) {
            ServerTickEvent.postAndCatch()
        }
    }

    /**
     * Dispatches [GuiEvent.Loaded]
     */
    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) = GlobalScope.launch {
        if (event.gui !is GuiChest) return@launch
        val container = (event.gui as GuiChest).inventorySlots

        if (container !is ContainerChest) return@launch
        val chestName = container.name

        val deferred = waitUntilLastItem(container)
        try { deferred.await() } catch (e: Exception) { return@launch } // Wait until the last item in the chest isn't null

        GuiEvent.Loaded(chestName, container).postAndCatch()
    }
}
