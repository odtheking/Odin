package me.odinmain.features.impl.dungeon

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.multiplyAlpha
import me.odinmain.events.impl.SecretPickupEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.toAABB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ClickedSecrets : Module(
    name = "Clicked Secrets",
    category = Category.DUNGEON,
    description = "Marks all the secrets you have clicked."
) {
    private val style: Int by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION)
    private val color: Color by ColorSetting("Color", Color.MINECRAFT_GOLD.multiplyAlpha(.4f), allowAlpha = true, description = "The color of the box.")
    private val lineWidth: Float by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.")
    private val depthCheck: Boolean by BooleanSetting("Depth check", false, description = "Boxes show through walls.")
    private val lockedColor: Color by ColorSetting("Locked Color", Color.RED.multiplyAlpha(.4f), allowAlpha = true, description = "The color of the box when the chest is locked.")
    private val timeToStay: Long by NumberSetting("Time To Stay (seconds)", 7L, 1L, 60L, 1L, description = "The time the chests should remain highlighted.")
    private val useRealSize: Boolean by BooleanSetting("Use Real Size", true, description = "Whether or not to use the real size of the block.")
    private val disableInBoss: Boolean by BooleanSetting("Disable In Boss", false, description = "Highlight clicks in boss")

    private data class Chest(val pos: BlockPos, val timeAdded: Long, var locked: Boolean = false)
    private val secrets = mutableListOf<Chest>()

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons || (DungeonUtils.inBoss && disableInBoss) || secrets.isEmpty()) return

        val tempList = secrets.toList()
        tempList.forEach {
            val size = if (useRealSize) getBlockAt(it.pos).getSelectedBoundingBox(mc.theWorld, BlockPos(it.pos)) else it.pos.toAABB()
            Renderer.drawStyledBox(size, if (it.locked) lockedColor else color, style, lineWidth, depthCheck)
        }
    }

    @SubscribeEvent
    fun onSecret(event: SecretPickupEvent.Interact) {
        if ((DungeonUtils.inBoss && disableInBoss) || secrets.any { it.pos == event.blockPos }) return
        secrets.add(Chest(event.blockPos, System.currentTimeMillis()))

        runIn(timeToStay.toInt() * 20) {
            secrets.removeFirstOrNull()
        }
    }

    init {
        onWorldLoad {
            secrets.clear()
        }

        onMessage("That chest is locked!", true) {
            if (secrets.isEmpty()) return@onMessage
            secrets.lastOrNull()?.let { it.locked = true }
        }
    }
}
