package me.odinmain.utils.skyblock.dungeon

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.*
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.isSecret
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.passive.EntityBat
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SecretUtils {

    private val drops = listOf(
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion", "Healing Potion VIII Splash Potion",
        "Decoy", "Inflatable Jerry", "Spirit Leap", "Trap", "Training Weights", "Defuse Kit", "Dungeon Chest Key", "Treasure Talisman", "Revive Stone",
    )

    @SubscribeEvent
    fun onRemoveEntity(event: EntityLeaveWorldEvent) {
        //if (!inDungeons) return
        if (event.entity is EntityItem && event.entity.entityItem.displayName.noControlCodes.containsOneOf(drops, true)) SecretPickupEvent(SecretItem.Item(event.entity)).postAndCatch()
        if (event.entity is EntityBat) SecretPickupEvent(SecretItem.Bat(event.entity)).postAndCatch()
    }

    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        //if (!inDungeons) return
        val packet = event.packet
        if (packet is C08PacketPlayerBlockPlacement && packet.position != null && isSecret(mc.theWorld?.getBlockState(packet.position) ?: return, packet.position)) {
            SecretPickupEvent(SecretItem.Interact(packet.position, mc.theWorld?.getBlockState(packet.position)  ?: return)).postAndCatch()
        }
    }
}

sealed class SecretItem {
    data class Interact(val blockPos: BlockPos, val blockState: IBlockState) : SecretItem()
    data class Item(val entity: EntityItem) : SecretItem()
    data class Bat(val entity: EntityBat) : SecretItem()
}