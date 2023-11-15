package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object TerminalTriggerbot : Module(
    name = "Terminal Triggerbot",
    category = Category.FLOOR7,
    description = "Triggerbot to open inactive terminals"
) {

    private val onGround: Boolean by BooleanSetting(name = "On Ground")

    private val clickClock = Clock(1000)
    private var terminalList = listOf<Entity>()

    init {
        execute(1000) {
            if (!enabled) return@execute
            terminalList = mc.theWorld?.loadedEntityList?.filter { it is EntityArmorStand && it.name.noControlCodes == "Inactive Terminal"} ?: emptyList()
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (DungeonUtils.getPhase() != 3 || !clickClock.hasTimePassed()) return
        val lookingAt = mc.objectMouseOver.entityHit ?: return
        if (lookingAt !is EntityArmorStand || mc.currentScreen != null || this.onGround && !mc.thePlayer.onGround ) return
        PlayerUtils.rightClick()
        clickClock.update()
    }

}