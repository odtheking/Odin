package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.HighlightRenderer
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.ui.Colors
import net.minecraft.block.BlockStainedGlass
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LividSolver : Module(
    name = "Livid Solver",
    desc = "Provides a visual cue for the correct Livid's location in the boss fight."
) {
    private val mode by SelectorSetting("Mode", HighlightRenderer.HIGHLIGHT_MODE_DEFAULT, HighlightRenderer.highlightModeList, desc = HighlightRenderer.HIGHLIGHT_MODE_DESCRIPTION)
    private val thickness by NumberSetting("Line Width", 1f, .1f, 4f, .1f, desc = "The line width of Outline / Boxes/ 2D Boxes.").withDependency { mode != HighlightRenderer.HighlightType.Overlay.ordinal }
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION).withDependency { mode == HighlightRenderer.HighlightType.Boxes.ordinal }

    private val woolLocation = BlockPos(5, 108, 43)
    private var currentLivid = Livid.HOCKEY

    init {
        HighlightRenderer.addEntityGetter({ HighlightRenderer.HighlightType.entries[mode] }) {
            if (!enabled || mc.thePlayer.isPotionActive(Potion.blindness)) return@addEntityGetter emptyList()
            currentLivid.entity?.let { listOf(HighlightRenderer.HighlightEntity(it, currentLivid.color, thickness, OdinMain.isLegitVersion, style)) } ?: emptyList()
        }

        onWorldLoad {
            currentLivid = Livid.HOCKEY
            currentLivid.entity = null
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5) || event.updated.block != Blocks.wool || event.pos != woolLocation) return
        currentLivid = Livid.entries.find { livid -> livid.woolMetadata == event.updated.getValue(BlockStainedGlass.COLOR).metadata } ?: return
        runIn((mc.thePlayer?.getActivePotionEffect(Potion.blindness)?.duration ?: 0) - 20) {
            modMessage("Found Livid: §${currentLivid.colorCode}${currentLivid.entityName}")
        }
    }

    @SubscribeEvent
    fun onPostMetaData(event: PostEntityMetadata) {
        if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(5)) return
        runIn((mc.thePlayer?.getActivePotionEffect(Potion.blindness)?.duration ?: 0) - 20) {
            currentLivid.entity = (mc.theWorld?.getEntityByID(event.packet.entityId) as? EntityOtherPlayerMP)?.takeIf { it.name == "${currentLivid.entityName} Livid" } ?: return@runIn
        }
    }

    private enum class Livid(val entityName: String, val colorCode: Char, val color: Color, val woolMetadata: Int) {
        VENDETTA("Vendetta", 'f', Colors.WHITE, 0),
        CROSSED("Crossed", 'd', Colors.MINECRAFT_DARK_PURPLE, 2),
        ARCADE("Arcade", 'e', Colors.MINECRAFT_YELLOW, 4),
        SMILE("Smile", 'a', Colors.MINECRAFT_GREEN, 5),
        DOCTOR("Doctor", '7', Colors.MINECRAFT_GRAY, 7),
        PURPLE("Purple", '5', Colors.MINECRAFT_DARK_PURPLE, 10),
        SCREAM("Scream", '9', Colors.MINECRAFT_BLUE, 11),
        FROG("Frog", '2', Colors.MINECRAFT_DARK_GREEN, 13),
        HOCKEY("Hockey", 'c', Colors.MINECRAFT_RED, 14);

        var entity: EntityOtherPlayerMP? = null
    }
}