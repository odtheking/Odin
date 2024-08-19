package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.SecretPickupEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.toAABB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CopyOnWriteArrayList

object ClickedSecrets : Module(
    name = "Clicked Secrets",
    category = Category.DUNGEON,
    description = "Marks all the secrets you have clicked."
) {
    private val style: Int by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION)
    private val color: Color by ColorSetting("Color", Color.ORANGE.withAlpha(.4f), allowAlpha = true, description = "The color of the box.")
    private val lineWidth: Float by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.")
    private val depthCheck: Boolean by BooleanSetting("Depth check", false, description = "Boxes show through walls.")
    private val lockedColor: Color by ColorSetting("Locked Color", Color.RED.withAlpha(.4f), allowAlpha = true, description = "The color of the box when the chest is locked.")
    private val timeToStay: Long by NumberSetting("Time To Stay (seconds)", 7L, 1L, 60L, 1L, description = "The time the chests should remain highlighted.")
    private val useRealSize: Boolean by BooleanSetting("Use Real Size", true, description = "Whether or not to use the real size of the block.")
    private val disableInBoss: Boolean by BooleanSetting("Disable In Boss", false, description = "Highlight clicks in boss.")

    private data class Chest(val pos: BlockPos, val timeAdded: Long, var locked: Boolean = false)
    private val clickedSecretsList = CopyOnWriteArrayList<Chest>()

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons || (DungeonUtils.inBoss && disableInBoss) || clickedSecretsList.isEmpty()) return

        clickedSecretsList.forEach {
            if (useRealSize) Renderer.drawStyledBlock(it.pos, if (it.locked) lockedColor else color, style, lineWidth, depthCheck)
            else Renderer.drawStyledBox(it.pos.toAABB(), if (it.locked) lockedColor else color, style, lineWidth, depthCheck)
        }
    }

    @SubscribeEvent
    fun onSecret(event: SecretPickupEvent.Interact) {
        if ((DungeonUtils.inBoss && disableInBoss) || clickedSecretsList.any { it.pos == event.blockPos }) return
        clickedSecretsList.add(Chest(event.blockPos, System.currentTimeMillis()))

        runIn(timeToStay.toInt() * 20) {
            clickedSecretsList.removeFirstOrNull()
        }
    }

    init {
        onWorldLoad {
            clickedSecretsList.clear()
        }

        onMessage("That chest is locked!", true) {
            if (clickedSecretsList.isEmpty()) return@onMessage
            clickedSecretsList.lastOrNull()?.let { it.locked = true }
        }
    }
}
