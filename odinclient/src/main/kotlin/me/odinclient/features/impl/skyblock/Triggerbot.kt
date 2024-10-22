package me.odinclient.features.impl.skyblock

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.PlayerUtils.leftClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.KingRelics.currentRelic
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Triggerbot : Module(
    name = "Triggerbot",
    description = "Various Triggerbots. (Blood, Spirit Bear, Crystal Triggerbot, Secret Triggerbot, Relic Triggerbot)",
    category = Category.DUNGEON
) {
    private val blood by BooleanSetting("Blood Mobs", default = false, description = "Automatically clicks blood mobs.")
    private val bloodClickType by DualSetting("Blood Click Type", "Left", "Right", description = "What button to click for blood mobs.").withDependency { blood }

    private val spiritBear by BooleanSetting("Spirit Bear", default = false, description = "Automatically clicks the spirit bear.")

    private val crystal by BooleanSetting("Crystal Triggerbot", default = false, description = "Automatically takes and places crystals.")
    private val take by BooleanSetting("Take", default = true, description = "Takes crystals.").withDependency { crystal }
    private val place by BooleanSetting("Place", default = true, description = "Places crystals.").withDependency { crystal }

    private val secretTriggerbot by BooleanSetting("Secret Triggerbot", default = false, description = "Automatically clicks secret buttons.")
    private val stbDelay by NumberSetting("Delay", 200L, 0, 1000, unit = "ms", description = "The delay between each click.").withDependency { secretTriggerbot }

    private val stbCH by BooleanSetting("Crystal Hollows Chests", true, description = "Opens chests in crystal hollows when looking at them.").withDependency { secretTriggerbot }
    private val secretTBInBoss by BooleanSetting("In Boss", true, description = "Makes the triggerbot work in dungeon boss aswell.").withDependency { secretTriggerbot }

    private val triggerBotClock = Clock(stbDelay)
    private var clickedPositions = mapOf<BlockPos, Long>()
    private val clickClock = Clock(500)
    private val bloodMobs = setOf(
        "Revoker", "Tear", "Ooze", "Cannibal", "Walker", "Putrid", "Mute", "Parasite", "WanderingSoul", "Leech",
        "Flamer", "Skull", "Mr.Dead", "Vader", "Frost", "Freak", "Bonzo", "Scarf", "Livid", "Psycho", "Reaper",
    )
    private val cauldronMap = mapOf(
        "GREEN_KING_RELIC" to Vec2(49, 44),
        "RED_KING_RELIC" to Vec2(51, 42),
        "PURPLE_KING_RELIC" to Vec2(54, 41),
        "ORANGE_KING_RELIC" to Vec2(57, 42),
        "BLUE_KING_RELIC" to Vec2(59, 44)
    )

    private val relicTriggerBot by BooleanSetting("Relic triggerbot", false, description = "Automatically clicks the correct relic in the cauldron.")
    private val tbClock = Clock(1000)

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityOtherPlayerMP || mc.currentScreen != null || !DungeonUtils.inDungeons) return
        val name = event.entity.name.replace(" ", "")
        if ((blood && name !in bloodMobs) || (spiritBear && name != "Spirit Bear")) return

        if (!isFacingAABB(AxisAlignedBB(event.entity.posX - .5, event.entity.posY - 2.0, event.entity.posZ - .5, event.entity.posX + .5, event.entity.posY + 3.0, event.entity.posZ + .5), 30f)) return

        if (bloodClickType && name != "Spirit Bear") PlayerUtils.rightClick() else leftClick()
    }

    @SubscribeEvent
    fun onClientTickEvent(event: TickEvent.ClientTickEvent) {
        if (crystal && DungeonUtils.inBoss && DungeonUtils.getF7Phase() == M7Phases.P1 && clickClock.hasTimePassed() && mc.objectMouseOver != null) {
            if ((take && mc.objectMouseOver.entityHit is EntityEnderCrystal) || (place && mc.objectMouseOver.entityHit?.name?.noControlCodes == "Energy Crystal Missing" && mc.thePlayer?.heldItem?.unformattedName == "Energy Crystal")) {
                PlayerUtils.rightClick()
                clickClock.update()
            }
        }

        if (relicTriggerBot && tbClock.hasTimePassed()) {
            val obj = mc.objectMouseOver ?: return
            when {
                obj.entityHit is EntityArmorStand && obj.entityHit?.inventory?.get(4)?.itemID in cauldronMap.keys -> {
                    PlayerUtils.rightClick()
                    tbClock.update()
                }
                Vec2(obj.blockPos?.x ?: 0, obj.blockPos?.z ?: 0) == cauldronMap[currentRelic] && obj.blockPos?.y.equalsOneOf(6, 7) -> {
                    PlayerUtils.rightClick()
                    tbClock.update()
                }
            }
        }
    }

    init {
        execute(0) {
            if (
                !secretTriggerbot ||
                !triggerBotClock.hasTimePassed(stbDelay) ||
                DungeonUtils.currentRoomName.equalsOneOf("Water Board", "Three Weirdos") ||
                mc.currentScreen != null
            ) return@execute

            val pos = mc.objectMouseOver?.blockPos ?: return@execute
            val state = mc.theWorld?.getBlockState(pos) ?: return@execute
            clickedPositions = clickedPositions.filter { it.value + 1000L > System.currentTimeMillis() }

            when {
                (pos.x in 58..62 && pos.y in 133..136 && pos.z == 142) || clickedPositions.containsKey(pos) -> return@execute

                stbCH && LocationUtils.currentArea.isArea(Island.CrystalHollows) && state.block == Blocks.chest -> {
                    PlayerUtils.rightClick()
                    triggerBotClock.update()
                    clickedPositions = clickedPositions.plus(pos to System.currentTimeMillis())
                    return@execute
                }

                (DungeonUtils.inDungeons && !(!secretTBInBoss && DungeonUtils.inBoss) && DungeonUtils.isSecret(state, pos)) -> {
                    PlayerUtils.rightClick()
                    triggerBotClock.update()
                    clickedPositions = clickedPositions.plus(pos to System.currentTimeMillis())
                }
            }
        }
    }
}