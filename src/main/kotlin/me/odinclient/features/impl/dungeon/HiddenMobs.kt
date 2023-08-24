package me.odinclient.features.impl.dungeon

import me.odinclient.events.impl.RenderEntityModelEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HiddenMobs : Module(
    "Hidden Mobs",
    category = Category.DUNGEON
) {

    private val showShadowAssassin: Boolean by BooleanSetting("Show Shadow Assassins")
    private val showStealthy: Boolean by BooleanSetting("Show Stealthy Mobs")
    private val showFels: Boolean by BooleanSetting("Show Fels")

    private val watcherMobs = listOf(
        "Revoker",
        "Psycho",
        "Reaper",
        "Cannibal",
        "Mute",
        "Ooze",
        "Putrid",
        "Freak",
        "Leech",
        "Tear",
        "Parasite",
        "Flamer",
        "Skull",
        "Mr. Dead",
        "Vader",
        "Frost",
        "Walker",
        "Wandering Soul",
        "Bonzo",
        "Scarf",
        "Livid"
    )

    @SubscribeEvent
    fun onRenderEntity(event: RenderEntityModelEvent) {
        if (!DungeonUtils.inDungeons) return
        if (event.entity.isInvisible && when (event.entity) {
                is EntityEnderman -> showFels && event.entity.name == "Dinnerbone"
                is EntityPlayer -> showShadowAssassin && event.entity.name.contains("Shadow Assassin") ||
                        showStealthy && watcherMobs.any { event.entity.name.trim() == it }
                else -> false
            }
        ) {
            event.entity.isInvisible = false
        }
    }
}
