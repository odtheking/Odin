package me.odinclient.features.qol

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BrokenHype{

    private var trackerKills = 0
    private var trackerXP = 0.0
    private val witherBlades = arrayOf("HYPERION", "ASTRAEA", "SCYLLA", "VALKYRIE", "NECRON_BLADE_UNREFINED")

    @SubscribeEvent
    fun onLivingDeath(event: LivingDeathEvent) {
        if (!config.brokenHype ||
            mc.thePlayer.heldItem == null
             || !DungeonUtils.inDungeons ||
            !witherBlades.contains(mc.thePlayer.heldItem.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes").getString("id"))
        ) return


        val newKills = mc.thePlayer.heldItem.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes").getInteger("stats_book")
        val newXP = mc.thePlayer.heldItem.serializeNBT().getCompoundTag("tag").getCompoundTag("ExtraAttributes").getDouble("champion_combat_xp")

        if (trackerKills == newKills) return

        if (trackerXP == newXP && config.brokenHypeShowTitle) PlayerUtils.alert("&l&5HYPE BROKEN!", config.brokenHypePlaySound)

        trackerKills = newKills
        trackerXP = newXP
    }
}
