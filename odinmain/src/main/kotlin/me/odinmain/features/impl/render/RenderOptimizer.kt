package me.odinmain.features.impl.render

import me.odinmain.events.impl.ReceivePacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getSkullValue
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Items
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RenderOptimizer : Module(
    name = "Render Optimizer",
    category = Category.RENDER,
    description = "Disables certain render function when they are not necessary, resulting in a decrease in gpu usage."
) {

    private val fallingBlocks: Boolean by BooleanSetting(name = "Remove Falling Blocks", default = true, description = "Removes falling blocks that are not necessary.")
    private val p5Mobs: Boolean by BooleanSetting(name = "Remove P5 Armor Stands", default = true, description = "Removes armor stands that are not necessary.")
    private val hideParticles: Boolean by BooleanSetting(name = "Hide Particles", default = true, description = "Hides particles that are not necessary.")
    private val hideHeartParticles: Boolean by BooleanSetting(name = "Hide Heart Particles", default = true, description = "Hides heart particles.")
    private val hideHealerFairy: Boolean by BooleanSetting(name = "Hide Healer Fairy", default = true, description = "Hides the healer fairy.")
    private val hideSoulWeaver: Boolean by BooleanSetting(name = "Hide Soul Weaver", default = true, description = "Hides the soul weaver.")
    private val hideArcherBones: Boolean by BooleanSetting(name = "Hide Archer Bones", default = true, description = "Hides the archer bones.")

    private const val TENTACLE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM3MjIzZDAxOTA2YWI2M2FmMWExNTk4ODM0M2I4NjM3ZTg1OTMwYjkwNWMzNTEyNWI1NDViMzk4YzU5ZTFjNSJ9fX0="
    private const val HEALER_FAIRY_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjM2UzMWNmYzY2NzMzMjc1YzQyZmNmYjVkOWE0NDM0MmQ2NDNiNTVjZDE0YzljNzdkMjczYTIzNTIifX19"
    private const val SOUL_WEAVER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyNGVkNjg3NTMwNGZhNGExZjBjNzg1YjJjYjZhNmE3MjU2M2U5ZjNlMjRlYTU1ZTE4MTc4NDUyMTE5YWE2NiJ9fX0="
    @SubscribeEvent
    fun joinWorldEvent(event: EntityJoinWorldEvent) {
        if (event.entity is EntityFallingBlock && fallingBlocks) event.entity.setDead()

        if (hideArcherBones && event.entity is EntityItem && (event.entity as EntityItem).entityItem.itemDamage == 15 && (event.entity as EntityItem).entityItem.item === Items.dye)

        if (event.entity !is EntityArmorStand) return

        if (
             p5Mobs && DungeonUtils.getPhase() == 5 &&
            getSkullValue(event.entity as EntityArmorStand)?.contains(TENTACLE_TEXTURE) == true
        )
            event.entity.setDead()

        if (DungeonUtils.inDungeons && hideHealerFairy &&
            (event.entity as EntityArmorStand).heldItem.item == Items.skull &&
            (event.entity as EntityArmorStand).heldItem
                ?.tagCompound
                ?.getCompoundTag("SkullOwner")
                ?.getCompoundTag("Properties")
                ?.getTagList("textures", 10)
                ?.getCompoundTagAt(0)
                ?.getString("Value")?.contains(
                    HEALER_FAIRY_TEXTURE) == true
            )
            event.entity.setDead()

        if (DungeonUtils.inDungeons && hideSoulWeaver &&
            getSkullValue(event.entity as EntityArmorStand)?.contains(SOUL_WEAVER_TEXTURE) == true
            )
            event.entity.setDead()
    }
    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet !is S2APacketParticles) return

        if (DungeonUtils.getPhase() == 5 && hideParticles &&
            !event.packet.particleType.name.containsOneOf("ENCHANTMENT TABLE", "FLAME", "FIREWORKS_SPARK"))
            event.isCanceled = true

        if (hideHeartParticles && event.packet.particleType.name.containsOneOf("HEART"))
            event.isCanceled = true

    }




}