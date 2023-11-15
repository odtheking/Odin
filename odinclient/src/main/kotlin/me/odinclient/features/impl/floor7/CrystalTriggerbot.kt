package me.odinclient.features.impl.floor7

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderBoundingBox
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object CrystalTriggerbot : Module(
    name = "Crystal Triggerbot",
    category = Category.FLOOR7,
    description = "Collects and places energy crystals"
) {

    private val take: Boolean by BooleanSetting("Take", default = true)
    private val place: Boolean by BooleanSetting("Place", default = true)

    private val clickClock = Clock(500)

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!DungeonUtils.inBoss || DungeonUtils.getPhase() != 1 || !clickClock.hasTimePassed()) return
        if (take && mc.objectMouseOver.entityHit is EntityEnderCrystal || place && mc.objectMouseOver.entityHit.name.noControlCodes == "CLICK HERE") {
            PlayerUtils.rightClick()
            clickClock.update()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        val entityList = mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()
        entityList.forEach {
            it.isInvisible = false
            RenderUtils.drawCustomBox(it.renderBoundingBox, Color.GREEN, 2f, false)
        }
    }

}