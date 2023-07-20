package me.odinclient.features.impl.qol

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.skyblock.ItemUtils.itemID
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import me.odinclient.utils.skyblock.dungeon.map.MapUtils.equalsOneOf
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BrokenHype : Module(
    "Broken Hype",
    category = Category.QOL
) {
    private val showAlert: Boolean by BooleanSetting("Alert", true)
    private val playSound: Boolean by BooleanSetting("Play Sound", true)

    private var trackerKills = 0
    private var trackerXP = 0.0
    private val witherBlades = arrayOf("HYPERION", "ASTRAEA", "SCYLLA", "VALKYRIE", "NECRON_BLADE_UNREFINED")

    @SubscribeEvent
    fun onLivingDeath(event: LivingDeathEvent) {
        if (!DungeonUtils.inDungeons || !mc.thePlayer.heldItem?.itemID.equalsOneOf(witherBlades)) return

        val newKills = mc.thePlayer.heldItem.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes").getInteger("stats_book")
        val newXP = mc.thePlayer.heldItem.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes").getDouble("champion_combat_xp")

        if (trackerKills == newKills) return
        if (trackerXP == newXP && showAlert) PlayerUtils.alert("&l&5HYPE BROKEN!", playSound)

        trackerKills = newKills
        trackerXP = newXP
    }
}
