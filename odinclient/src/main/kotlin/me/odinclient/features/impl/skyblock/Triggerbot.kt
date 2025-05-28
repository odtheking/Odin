package me.odinclient.features.impl.skyblock

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.PlayerUtils.leftClick
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.ArrowAlign.clicksRemaining
import me.odinmain.features.impl.floor7.p3.ArrowAlign.currentFrameRotations
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.skyblockID
import me.odinmain.utils.skyblock.unformattedName
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Triggerbot : Module(
    name = "Triggerbot",
    desc = "Provides triggerbots for Blood, Spirit Bear, Crystal Triggerbot, Secret Triggerbot, Relic Triggerbot."
) {
    private val bloodDropDown by DropdownSetting("Blood Dropdown", false)
    private val blood by BooleanSetting("Blood Mobs", false, desc = "Automatically clicks blood mobs.").withDependency { bloodDropDown }
    private val bloodClickType by BooleanSetting("Blood Click Type", false, desc = "What button to click for blood mobs.").withDependency { blood && bloodDropDown }

    private val spiritBearDropDown by DropdownSetting("Spirit Bear Dropdown", false)
    private val spiritBear by BooleanSetting("Spirit Bear", false, desc = "Automatically clicks the spirit bear.").withDependency { spiritBearDropDown }

    private val crystalDropDown by DropdownSetting("Crystal Dropdown", false)
    private val crystal by BooleanSetting("Crystal Triggerbot", false, desc = "Automatically takes and places crystals.").withDependency { crystalDropDown }
    private val take by BooleanSetting("Take", true, desc = "Takes crystals.").withDependency { crystal && crystalDropDown }
    private val place by BooleanSetting("Place", true, desc = "Places crystals.").withDependency { crystal && crystalDropDown }

    private val secretTriggerbotDropDown by DropdownSetting("Secret Triggerbot Dropdown", false)
    private val secretTriggerbot by BooleanSetting("Secret Triggerbot", false, desc = "Automatically clicks secret buttons.").withDependency { secretTriggerbotDropDown }
    private val stbDelay by NumberSetting("Delay", 200L, 0, 1000, unit = "ms", desc = "The delay between each click.").withDependency { secretTriggerbot && secretTriggerbotDropDown }

    private val stbCH by BooleanSetting("Crystal Hollows Chests", true, desc = "Opens chests in crystal hollows when looking at them.").withDependency { secretTriggerbot && secretTriggerbotDropDown }
    private val secretTBInBoss by BooleanSetting("In Boss", true, desc = "Makes the triggerbot work in dungeon boss aswell.").withDependency { secretTriggerbot && secretTriggerbotDropDown }

    private val alignTriggerBotDropDown by DropdownSetting("Arrow Align Dropdown", false)
    private val alignTriggerbot: Boolean by BooleanSetting("Align Triggerbot", false, desc = "Automatically clicks the correct arrow in the arrow align device.").withDependency { alignTriggerBotDropDown }
    private val sneakToDisableTriggerbot: Boolean by BooleanSetting("Sneak to disable", false, desc = "Disables triggerbot when you are sneaking").withDependency { alignTriggerbot && alignTriggerBotDropDown }
    private val alignDelay by NumberSetting("Align Delay", 200L, 70, 500, desc = "The delay between each click.", unit = "ms").withDependency { alignTriggerbot && alignTriggerBotDropDown }

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

    private val relicTriggerBot by BooleanSetting("Relic triggerbot", false, desc = "Automatically clicks the correct relic in the cauldron.")
    private val tbClock = Clock(1000)

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityOtherPlayerMP || mc.currentScreen != null || !DungeonUtils.inDungeons) return
        val name = event.entity.name.replace(" ", "")
        if (!(blood && name in bloodMobs) && !(spiritBear && name == "SpiritBear")) return

        if (!isFacingAABB(AxisAlignedBB(event.entity.posX - .5, event.entity.posY - 2.0, event.entity.posZ - .5, event.entity.posX + .5, event.entity.posY + 3.0, event.entity.posZ + .5), 30f)) return

        if (bloodClickType && name != "SpiritBear") PlayerUtils.rightClick() else leftClick()
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
                obj.entityHit is EntityArmorStand && obj.entityHit?.inventory?.get(4)?.skyblockID in cauldronMap.keys -> {
                    PlayerUtils.rightClick()
                    tbClock.update()
                }
                Vec2(obj.blockPos?.x ?: 0, obj.blockPos?.z ?: 0) == cauldronMap[mc.thePlayer?.heldItem?.skyblockID ?: ""] && obj.blockPos?.y.equalsOneOf(6, 7) -> {
                    PlayerUtils.rightClick()
                    tbClock.update()
                }
            }
        }
    }

    init {
        execute( {alignDelay} ) {
            if (alignTriggerbot) arrowAlignTriggerbot()
        }

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
    private val frameGridCorner = Vec3(-2.0, 120.0, 75.0)

    private fun arrowAlignTriggerbot() {
        if ((sneakToDisableTriggerbot && mc.thePlayer.isSneaking) || clicksRemaining.isEmpty()) return
        val targetFrame = mc.objectMouseOver?.entityHit as? EntityItemFrame ?: return

        val (x, y, z) = targetFrame.positionVector.floorVec()
        val frameIndex = ((y - frameGridCorner.yCoord) + (z - frameGridCorner.zCoord) * 5).toInt()
        if (x != frameGridCorner.xCoord || currentFrameRotations?.get(frameIndex) == -1 || frameIndex !in 0..24) return
        clicksRemaining[frameIndex]?.let {
            PlayerUtils.rightClick()
            triggerBotClock.update()
        }
    }
}