package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.config
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.LocationUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.boss.BossStatus
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.roundToInt

object WatcherBar : Module(
    name = "Watcher Bar",
    description = "Shows the watcher's health",
    category = Category.DUNGEON
) {
    private var currentName: String? = null

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!config.watcherBar || !DungeonUtils.inDungeons) return
        if (!BossStatus.bossName.noControlCodes.contains("The Watcher")) return

        val health = BossStatus.healthScale
        val floor = LocationUtils.currentDungeon?.floor ?: return

        if (health < 0.05) {
            currentName = null
            return
        }
        val amount = 12 + floor.floorNumber
        currentName = " " + (amount * health).roundToInt() + "/" + amount
    }

    @SubscribeEvent
    fun onRenderBossHealth(event: RenderGameOverlayEvent) {
        if (
            !DungeonUtils.inDungeons ||
            event.type != RenderGameOverlayEvent.ElementType.BOSSHEALTH ||
            currentName == null
        ) return
        if (BossStatus.bossName.noControlCodes != "The Watcher") return
        BossStatus.bossName += currentName
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentName = null
    }
}