package me.odinclient.features.impl.render

import me.odinmain.events.impl.RenderChestEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.toAABB
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11

object ChestEsp : Module(
    name = "Chest Esp",
    category = Category.RENDER,
    description = "Displays chests through walls."
) {
    private val onlyDungeon by BooleanSetting(name = "Only Dungeon", description = "Only show chests in dungeons.")
    private val onlyCH by BooleanSetting(name = "Only Crystal Hollows", description = "Only show chests in Crystal Hollows.")
    private val hideClicked by BooleanSetting(name = "Hide Clicked", description = "Hide chests that have been clicked.")
    private val renderMode by SelectorSetting(name = "Render Mode", "Chams", arrayListOf("Chams", "Outline"), description = "The rendering mode.")
    private val color by ColorSetting(name = "Color", default = Color.RED, allowAlpha = true, description = "The color of the chest ESP.")

    private val clickedChests = mutableSetOf<BlockPos>()
    private var chests = mutableSetOf<BlockPos>()

    init {
        onWorldLoad { clickedChests.clear() }

        onPacket(C08PacketPlayerBlockPlacement::class.java) { packet ->
            if (getBlockAt(packet.position).equalsOneOf(Blocks.chest, Blocks.trapped_chest)) clickedChests.add(packet.position)
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
            GlStateManager.color(1f, 1f, 1f, color.alpha)
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