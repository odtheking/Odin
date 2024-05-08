package me.odinclient.features.impl.skyblock

import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.PlayerUtils.leftClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.Relic.currentRelic
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent


object Triggerbot : Module(
    name = "Triggerbot",
    description = "Various Triggerbots.",
    category = Category.DUNGEON
) {
    private val blood: Boolean by BooleanSetting("Blood Mobs")
    private val bloodClickType: Boolean by DualSetting("Blood Click Type", "Left", "Right", description = "What button to click for blood mobs.").withDependency { blood }
    private val spiritBear: Boolean by BooleanSetting("Spirit Bear")
    private val crystal: Boolean by BooleanSetting("Crystal Triggerbot", default = false)
    private val take: Boolean by BooleanSetting("Take", default = true).withDependency { crystal }
    private val place: Boolean by BooleanSetting("Place", default = true).withDependency { crystal }

    private val secretTriggerbot: Boolean by BooleanSetting("Secret Triggerbot", default = false)
    private val stbDelay: Long by NumberSetting("Delay", 200L, 0, 1000).withDependency { secretTriggerbot }
    private val stbCH: Boolean by BooleanSetting("Crystal Hollows Chests", true, description = "Opens chests in crystal hollows when looking at them").withDependency { secretTriggerbot }
    private val secretTBInBoss: Boolean by BooleanSetting("In Boss", true, description = "Makes the triggerbot work in dungeon boss aswell.").withDependency { secretTriggerbot }

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

    private val relicTriggerBot: Boolean by BooleanSetting("Relic triggerbot", false, description = "Automatically clicks the correct relic in the cauldron.")
    private val tbClock = Clock(1000)

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityOtherPlayerMP || mc.currentScreen != null || !DungeonUtils.inDungeons) return
        val ent = event.entity
        val name = ent.name.replace(" ", "")
        if (!(bloodMobs.contains(name) && blood) && !(name == "Spirit Bear" && spiritBear)) return

        val (x, y, z) = Triple(ent.posX, ent.posY, ent.posZ)
        if (!isFacingAABB(AxisAlignedBB(x - .5, y - 2.0, z - .5, x + .5, y + 3.0, z + .5), 30f)) return

        if (bloodClickType && name != "Spirit Bear") PlayerUtils.rightClick()
        else leftClick()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!DungeonUtils.inBoss || DungeonUtils.getPhase() != Island.M7P1 || !clickClock.hasTimePassed() || mc.objectMouseOver == null || !crystal) return
        if ((take && mc.objectMouseOver.entityHit is EntityEnderCrystal) || (place && mc.objectMouseOver.entityHit?.name?.noControlCodes == "Energy Crystal Missing" && mc.thePlayer.heldItem.displayName.noControlCodes == "Energy Crystal")) {
            PlayerUtils.rightClick()
            clickClock.update()
        }
    }

    @SubscribeEvent
    fun onClientTickEvent(event: TickEvent.ClientTickEvent) {
        if (!relicTriggerBot || !tbClock.hasTimePassed()) return
        val obj = mc.objectMouseOver ?: return
        if (obj.entityHit is EntityArmorStand && obj.entityHit?.inventory?.get(4)?.itemID in cauldronMap.keys) {
            PlayerUtils.rightClick()
            tbClock.update()
        }

        if (
            Vec2(obj.blockPos?.x ?: 0, obj.blockPos?.z ?: 0) != cauldronMap[currentRelic] ||
            !obj.blockPos?.y.equalsOneOf(6, 7)
        ) return
        PlayerUtils.rightClick()
        tbClock.update()
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
            val state = mc.theWorld.getBlockState(pos) ?: return@execute
            val tileEntity = mc.theWorld.getTileEntity(pos) ?: null
            clickedPositions = clickedPositions.filter { it.value + 1000L > System.currentTimeMillis() }
            if (
                (pos.x in 58..62 && pos.y in 133..136 && pos.z == 142) || // looking at lights device
                clickedPositions.containsKey(pos) // already clicked
            ) return@execute

            if (tileEntity is TileEntityChest && tileEntity.numPlayersUsing >= 1) return@execute

            if (stbCH && LocationUtils.currentArea == Island.CrystalHollows && state.block == Blocks.chest) {
                PlayerUtils.rightClick()
                triggerBotClock.update()
                clickedPositions = clickedPositions.plus(pos to System.currentTimeMillis())
                return@execute
            }

            if (!DungeonUtils.inDungeons || (!secretTBInBoss && DungeonUtils.inBoss) || !DungeonUtils.isSecret(state, pos)) return@execute

            PlayerUtils.rightClick()
            triggerBotClock.update()
            if (tileEntity is TileEntityChest) return@execute
            clickedPositions = clickedPositions.plus(pos to System.currentTimeMillis())
        }
    }
}