package me.odinclient.features.impl.skyblock

import me.odinclient.utils.skyblock.PlayerUtils.leftClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.utils.VecUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


object Triggerbot : Module(
    name = "Triggerbot",
    description = "Instantly left clicks if you are looking at a specified mob.",
    category = Category.DUNGEON
) {
    private val blood: Boolean by BooleanSetting("Blood Mobs")
    private val spiritBear: Boolean by BooleanSetting("Spirit Bear")
    private val bloodClickType: Boolean by DualSetting("Blood Click Type", "Left", "Right", description = "What button to click for blood mobs.")

    private val bloodMobs: Set<String> = setOf(
        "Revoker", "Tear", "Ooze", "Cannibal", "Walker", "Putrid", "Mute", "Parasite", "WanderingSoul", "Leech",
        "Flamer", "Skull", "Mr.Dead", "Vader", "Frost", "Freak", "Bonzo", "Scarf", "Livid", "Psycho", "Reaper",
    )

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityOtherPlayerMP || mc.currentScreen != null || !DungeonUtils.inDungeons) return
        val ent = event.entity
        val name = ent.name.replace(" ", "")
        if (!(bloodMobs.contains(name) && blood) && !(name == "Spirit Bear" && spiritBear)) return

        val (x, y, z) = Triple(ent.posX, ent.posY, ent.posZ)
        if (!VecUtils.isFacingAABB(AxisAlignedBB(x - .5, y - 2.0, z - .5, x + .5, y + 3.0, z + .5), 30f)) return

        if (bloodClickType && name != "Spirit Bear") PlayerUtils.rightClick()
        else leftClick()
    }
}