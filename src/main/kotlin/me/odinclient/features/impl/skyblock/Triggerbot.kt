package me.odinclient.features.impl.skyblock

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.VecUtils
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Triggerbot : Module(
    name = "Triggerbot",
    description = "Instantly left clicks if you are looking at a specified mob",
    category = Category.DUNGEON
) {
    private val blood: Boolean by BooleanSetting("Blood Mobs")
    private val spiritBear: Boolean by BooleanSetting("Spirit Bear")

    private val bloodMobs: Set<String> = setOf(
        "Revoker", "Psycho", "Reaper", "Cannibal", "Mute", "Ooze", "Putrid", "Freak", "Leech", "Tear",
        "Parasite", "Flamer", "Skull", "Mr.Dead", "Vader", "Frost", "Walker", "Bonzo", "Scarf", "Livid", "WanderingSoul"
    )

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (
            event.entity !is EntityOtherPlayerMP ||
            DungeonUtils.inBoss ||
            mc.currentScreen != null
        ) return
        val ent = event.entity
        val name = ent.name.replace(" ", "")
        if (
            !(bloodMobs.contains(name) && blood) &&
            !(name == "Spirit Bear" && spiritBear)
        ) return

        if (
            VecUtils.isFacingAABB(
                AxisAlignedBB(
                    event.entity.posX - 0.5,
                    event.entity.posY - 2.0,
                    event.entity.posZ - 0.5,
                    event.entity.posX + 0.5,
                    event.entity.posY + 3.0,
                    event.entity.posZ + 0.5
                ), 30f
            )
        ) {
            PlayerUtils.leftClick()
        }
    }
}