package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object TerminalQOL : Module(
    name = "Terminal Qol",
    category = Category.FLOOR7,
    description = "Triggerbot to open inactive terminals and an option to show inactive terminals"
) {

    private val terminalTriggerbot: Boolean by BooleanSetting(name = "Terminal Triggerbot")
    private val onGround: Boolean by BooleanSetting(name = "On Ground").withDependency { terminalTriggerbot }
    private val showInactive: Boolean by BooleanSetting(name = "Show Inactive")
    private val throughWalls: Boolean by BooleanSetting(name = "Through Walls").withDependency { showInactive }

    private var lastClick = 0L
    private var terminalList = listOf<Entity>()

    init {
        execute(1000) {
            terminalList = mc.theWorld?.loadedEntityList?.filter { it is EntityArmorStand && it.name.noControlCodes.contains("Inactive", true) }!!
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!terminalTriggerbot || DungeonUtils.getPhase() != 3 || System.currentTimeMillis() - lastClick < 1000) return
        val lookingAt = mc.objectMouseOver.entityHit ?: return
        if (lookingAt !is EntityArmorStand || lookingAt.name.noControlCodes.contains("Inactive", true) || mc.currentScreen != null || this.onGround && !mc.thePlayer.onGround ) return
        PlayerUtils.rightClick()
        lastClick = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!showInactive) return
        terminalList.forEach {
            RenderUtils.drawCustomESPBox(it.entityBoundingBox, Color.RED, 1f, phase = throughWalls)
            RenderUtils.drawFilledBox(it.entityBoundingBox, Color.RED.withAlpha(0.4f), phase = throughWalls)
        }
    }

}