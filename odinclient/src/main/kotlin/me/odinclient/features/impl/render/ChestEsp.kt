package me.odinclient.features.impl.render

import me.odinmain.events.impl.RenderChestEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.toAABB
import me.odinmain.utils.ui.Colors
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11

object ChestEsp : Module(
    name = "Chest Esp",
    desc = "Displays chests through walls."
) {
    private val onlyDungeon by BooleanSetting("Only Dungeon", desc = "Only show chests in dungeons.")
    private val onlyCH by BooleanSetting("Only Crystal Hollows", desc = "Only show chests in Crystal Hollows.")
    private val hideClicked by BooleanSetting("Hide Clicked", desc = "Hide chests that have been clicked.")
    private val renderMode by SelectorSetting("Render Mode", "Chams", arrayListOf("Chams", "Outline"), desc = "The rendering mode.")
    private val color by ColorSetting("Color", Colors.MINECRAFT_RED, allowAlpha = true, desc = "The color of the chest ESP.")

    private val clickedChests = mutableSetOf<BlockPos>()
    private var chests = mutableSetOf<BlockPos>()

    init {
        onWorldLoad { clickedChests.clear() }

        onPacket<C08PacketPlayerBlockPlacement> {
            if (getBlockAt(it.position).equalsOneOf(Blocks.chest, Blocks.trapped_chest)) clickedChests.add(it.position)
        }

        execute(200) {
            chests = mc.theWorld?.loadedTileEntityList?.mapNotNull { (it as? TileEntityChest)?.pos }?.toMutableSet() ?: mutableSetOf()
        }
    }

    @SubscribeEvent
    fun onRenderChest(event: RenderChestEvent.Pre) {
        if (renderMode != 0 || event.chest != mc.theWorld?.getTileEntity(event.chest.pos)) return
        if (hideClicked && event.chest.pos in clickedChests) return
        if ((onlyDungeon && DungeonUtils.inDungeons) || (onlyCH && LocationUtils.currentArea.isArea(Island.CrystalHollows)) || (!onlyDungeon && !onlyCH)) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL)
            GlStateManager.color(1f, 1f, 1f, color.alphaFloat)
            GlStateManager.enablePolygonOffset()
            GlStateManager.doPolygonOffset(1f, -1000000f)
        }
    }

    @SubscribeEvent
    fun onRenderChest(event: RenderChestEvent.Post) {
        if (!(onlyDungeon && DungeonUtils.inDungeons) && !(onlyCH && LocationUtils.currentArea.isArea(Island.CrystalHollows)) && !(!onlyDungeon && !onlyCH)) return
        if (hideClicked && event.chest.pos in clickedChests) return

        if (renderMode == 1) Renderer.drawBox(event.chest.pos.toAABB(), color, 1f, depth = false, fillAlpha = 0)
        else if (renderMode == 0 && event.chest == mc.theWorld?.getTileEntity(event.chest.pos)) {
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL)
            GlStateManager.doPolygonOffset(1f, 1000000f)
            GlStateManager.disablePolygonOffset()
        }
    }
}