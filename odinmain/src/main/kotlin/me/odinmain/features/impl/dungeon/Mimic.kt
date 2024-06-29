package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain.isLegitVersion
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.RenderChestEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.LocationUtils.currentDungeon
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.toAABB
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Mimic : Module(
    "Mimic",
    description = "Helpful mimic utilities.",
    category = Category.DUNGEON
) {
    private val mimicMessageToggle: Boolean by BooleanSetting("Toggle Mimic Message", default = true)
    val mimicMessage: String by StringSetting("Mimic Message", "Mimic Killed!", 128, description = "Message sent when mimic is detected as killed").withDependency { mimicMessageToggle }
    val reset: () -> Unit by ActionSetting("Send message", description = "Sends Mimic killed message in party chat.") { mimicKilled() }
    private val mimicBox: Boolean by BooleanSetting("Mimic Box", false, description = "Draws a box around the mimic chest.")
    private val style: Int by SelectorSetting("Style", Renderer.defaultStyle, Renderer.styles, description = Renderer.styleDesc).withDependency { mimicBox }
    private val color: Color by ColorSetting("Color", Color.RED.withAlpha(0.5f), allowAlpha = true, description = "The color of the box.").withDependency { mimicBox }
    private val lineWidth: Float by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.").withDependency { mimicBox }
    private val depthCheck: Boolean by BooleanSetting("Depth check", false, description = "Boxes show through walls.").withDependency { mimicBox }

    private var chestUpdate: Long = 0
    private var pos: BlockPos? = null

    init {
        execute(100) {
            if (!DungeonUtils.inDungeons || chestUpdate == 0L || pos == null || DungeonUtils.mimicKilled ||
                System.currentTimeMillis() - chestUpdate > 600 || mc.thePlayer.getDistanceSq(pos) > 400 ) return@execute
            if (mc.theWorld.loadedEntityList.find { it is EntityZombie && it.isChild && (0..3).all { i -> it.getCurrentArmor(i) == null } } == null) mimicKilled()
        }

        onWorldLoad {
            chestUpdate = 0L
            pos = null
        }
    }

    @SubscribeEvent
    fun onBlockUpdate(event: BlockChangeEvent) {
        if (event.old != Blocks.trapped_chest || event.update != Blocks.air ) return
        chestUpdate = System.currentTimeMillis()
        pos = event.pos
    }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        val entity = event.entity
        if (!DungeonUtils.inDungeons || entity !is EntityZombie || !entity.isChild || !(0..3).all { entity.getCurrentArmor(it) == null }) return
        mimicKilled()
    }

    @SubscribeEvent
    fun onRenderLast(event: RenderChestEvent.Post) {
        if (event.chest.chestType != 1 || !DungeonUtils.inDungeons || DungeonUtils.inBoss || !mimicBox) return
        Renderer.drawStyledBox(event.chest.pos.toAABB(), color, style, lineWidth, isLegitVersion)
    }

    private fun mimicKilled() {
        if (DungeonUtils.mimicKilled) return
        if (mimicMessageToggle) partyMessage(mimicMessage)
        currentDungeon?.dungeonStats?.mimicKilled = true
    }

    override fun onKeybind() {
        reset()
    }
}