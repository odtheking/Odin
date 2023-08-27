package me.odinclient.features.impl.render

import cc.polyfrost.oneconfig.libs.universal.UChat
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.impl.fishing.CrimsonFishing
import me.odinclient.utils.skyblock.LocationUtils
import me.odinclient.utils.skyblock.LocationUtils.inSkyblock
import me.odinclient.utils.skyblock.ScoreboardUtils
import me.odinclient.utils.skyblock.ScoreboardUtils.cleanSB
import me.odinclient.utils.skyblock.ScoreboardUtils.sidebarLines
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.awt.Color

object MonolithESP : Module(
    name = "Monolith ESP",
    category = Category.RENDER,
    new = true
) {

    private val locations = listOf(
        "Barracks Of Heroes",
        "Cliffside Veins",
        "Divan's Gateway",
        "Dwarven Tavern",
        "Dwarven Village",
        "Far Reserve",
        "Forge Basin",
        "Goblin Burrows",
        "Grand Library",
        "Hanging Court",
        "Lava Springs",
        "Palace Bridge",
        "Rampart's Quarry",
        "Royal Mines",
        "Royal Palace",
        "The Forge",
        "The Lift",
        "Upper Mines"
    )
    private val monolithBlockCoords = listOf(
        Vec3(-15.5,236.0,-92.5),
        Vec3(49.5,202.0,-162.5),
        Vec3(56.5,214.0,-25.5),
        Vec3(128.5,187.0,58.5),
        Vec3(150.5,196.0,190.5),
        Vec3(61.5,204.0,181.5),
        Vec3(91.5,187.0,131.5),
        Vec3(77.5,160.0,162.5),
        Vec3(-10.5,162.0,109.5),
        Vec3(1.5,183.0,25.5),
        Vec3(1.5,170.0,0.0),
        Vec3(-94.5,201.0,-30.5),
        Vec3(-91.5,221.0,-53.5),
        Vec3(-64.5,206.0,-63.5),
        Vec3(0.5,170.0,0.5)
    )

    private var lastTimeChecked : Long = 0
    private var monolithCoords : Vec3? = null

    @SubscribeEvent
    fun worldRender(event: RenderWorldLastEvent){
        if (isDwarvenMine() || System.currentTimeMillis()/1000 - lastTimeChecked > 1) {
            if (monolithCoords != null) {
                val actualCoords = Vec3(monolithCoords!!.xCoord, monolithCoords!!.yCoord + 1.5, monolithCoords!!.zCoord)
                if (mc.theWorld.getChunkFromBlockCoords(BlockPos(actualCoords)).getBlock(BlockPos(actualCoords)) == Blocks.dragon_egg) {
                    drawBox(monolithCoords!!, Color(200,0,200,255), event.partialTicks)
                    return
                }
            }
            lastTimeChecked = System.currentTimeMillis() / 1000
            for (possibleBlock in monolithBlockCoords) {
                for (offsetX in -5..5) {
                    for (offsetZ in -5..5) {
                        val blockPos = BlockPos(
                            possibleBlock.xCoord + offsetX,
                            possibleBlock.yCoord,
                            possibleBlock.zCoord + offsetZ
                        )
                        if (mc.theWorld.getChunkFromBlockCoords(BlockPos(blockPos))
                                .getBlock(BlockPos(blockPos)) == Blocks.dragon_egg
                        ) {
                            UChat.chat("Found Monolith!")
                            monolithCoords = Vec3(
                                possibleBlock.xCoord + offsetX,
                                possibleBlock.yCoord - 1.5,
                                possibleBlock.zCoord + offsetZ
                            )
                            break
                        }
                    }
                }
            }
        }
        if (monolithCoords == null) return
        val actualCoords = Vec3(monolithCoords!!.xCoord, monolithCoords!!.yCoord+1.5, monolithCoords!!.zCoord)
        if (mc.thePlayer.worldObj.getChunkFromBlockCoords(BlockPos(actualCoords)).getBlock(BlockPos(actualCoords)) != Blocks.dragon_egg) return
        drawBox(monolithCoords!!, Color(200,0,200,255),event.partialTicks)
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load){
        lastTimeChecked = 0
    }

    private fun drawFilledBoundingBox(aabb: AxisAlignedBB, c: Color, alphaMultiplier: Float) {
        GlStateManager.disableDepth()
        GlStateManager.disableCull()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.disableTexture2D()
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        GlStateManager.color(
            c.red / 255f, c.green / 255f, c.blue / 255f,
            c.alpha / 255f * alphaMultiplier
        )
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        GlStateManager.enableCull()
    }

    private fun drawBox(pos: Vec3, color: Color, pt: Float) {
        val viewer: Entity = mc.renderViewEntity
        val viewerX: Double = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * pt
        val viewerY: Double = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * pt
        val viewerZ: Double = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * pt

        val x = pos.xCoord - 0.5 - viewerX
        val y = pos.yCoord - viewerY + 1.5
        val z = pos.zCoord - 0.5 - viewerZ

        drawFilledBoundingBox(AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), color, 0.6f)
    }

    private fun isDwarvenMine(): Boolean {
        return inSkyblock && sidebarLines.any { s -> locations.any { cleanSB(s).contains(it) } }
    }
}
