package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.Floor
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.skyblock.getBlockStateAt
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.event.RenderLivingEvent

object LividSolver : Module(
    name = "Livid Solver",
    description = "Automatically solves the Livid puzzle in dungeons.",
    category = Category.DUNGEON
) {

    private data class Livid(val woolMeta: Int, val name: String, val color: String, val entity: EntityOtherPlayerMP? = null, val armorStand: EntityArmorStand? = null)

    private val livids = listOf(
        Livid(0,  "Vendetta", "§f"),
        Livid(2,  "Crossed",  "§d"),
        Livid(4,  "Arcade",   "§e"),
        Livid(5,  "Smile",    "§a"),
        Livid(7,  "Doctor",   "§7"),
        Livid(10, "Purple",   "§5"),
        Livid(11, "Scream",   "§9"),
        Livid(13, "Frog",     "§2"),
        Livid(14, "Hockey",   "§c")
    )

    private var currentLivid: Livid? = null

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5)) {
            currentLivid = null
            return
        }

        val lividData = livids.find { livid -> livid.woolMeta == getBlockAt(BlockPos(4, 108, 43)).takeIf { it == Blocks.wool }?.getMetaFromState(getBlockStateAt(BlockPos(4, 108, 43))) } ?: return

        val lividEntity = mc.theWorld?.loadedEntityList?.find { it is EntityOtherPlayerMP && it.name == "${lividData.name} Livid" } as? EntityOtherPlayerMP ?: return
        val nametagArmorStand = mc.theWorld?.loadedEntityList?.find { it is EntityArmorStand && it.name.startsWith(lividData.color) } as? EntityArmorStand ?: return

        currentLivid = lividData.copy(entity = lividEntity, armorStand = nametagArmorStand)
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        currentLivid?.entity?.let { RenderUtils.drawOutlinedAABB(it.renderBoundingBox, Color.WHITE, 2f, true) }
    }

    @SubscribeEvent
    fun onRenderEntity(event: RenderLivingEvent.Pre<*>) {
        if (currentLivid == null || !event.entity.name.contains("Livid") || event.entity.name == currentLivid?.entity?.name || event.entity.name == currentLivid?.armorStand?.name) return
        event.isCanceled = true
    }
}