package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.skyblock.getBlockStateAt
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LividSolver : Module(
    name = "Livid Solver",
    description = "Automatically solves the Livid puzzle in dungeons.",
    category = Category.DUNGEON
) {
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, description = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val thickness by NumberSetting("Line Width", 1f, .1f, 4f, .1f, description = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }

    private val woolLocation = BlockPos(5, 108, 43)

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode] }) {
            if (!enabled || mc.thePlayer.isPotionActive(Potion.blindness)) return@addEntityGetter emptyList()
            currentLivid.entity?.let { listOf(HighlightRenderer.HighlightEntity(it, currentLivid.color, thickness, OdinMain.isLegitVersion, style)) } ?: emptyList()
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (event.pos != woolLocation) return
        val block = getBlockAt(event.pos)
        if (block != Blocks.wool) {
            currentLivid.entity = null
            return
        }
        devMessage("metadata: ${block.getMetaFromState(event.updated)} at ${event.pos}")
        currentLivid = livids.find { livid -> livid.woolMetadata == block.getMetaFromState(event.updated) } ?: return
        modMessage("Found Livid: §${currentLivid.colorCode}${currentLivid.name}")
    }

    @SubscribeEvent
    fun onPostMetaData(event: PostEntityMetadata) {
        val entity = (mc.theWorld?.getEntityByID(event.packet.entityId) as? EntityOtherPlayerMP)?.takeIf { it.name == "${currentLivid.name} Livid" } ?: return
        currentLivid.entity = entity
    }


    private data class Livid(val name: String, val colorCode: Char, val color: Color, val woolMetadata: Int, var entity: EntityOtherPlayerMP? = null)

    private val livids = listOf(
        Livid("Vendetta", 'f', Color.WHITE, 0),
        Livid("Crossed",  'd', Color.PURPLE, 2),
        Livid("Arcade",   'e', Color.YELLOW, 4),
        Livid("Smile",    'a', Color.GREEN, 5),
        Livid("Doctor",   '7', Color.GRAY, 7),
        Livid("Purple",   '5', Color.PURPLE, 10),
        Livid("Scream",   '9', Color.BLUE, 11),
        Livid("Frog",     '2', Color.DARK_GREEN, 13),
        Livid("Hockey",   'c', Color.RED, 14)
    )

    private var currentLivid = livids[8]
}