package me.odinclient.features.impl.skyblock

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.ClickEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.rightClick
import me.odinclient.utils.skyblock.LocationUtils.inSkyblock
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.util.StringUtils
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoAttune : Module(
    "Auto Blaze Attune",
    category = Category.SKYBLOCK
) {

    private var lastAttuneTime = 0L

    private val attunements = listOf(
        Attunement("ASHEN", Items.stone_sword, 0),
        Attunement("AURIC", Items.golden_sword, 0),
        Attunement("SPIRIT", Items.iron_sword, 1),
        Attunement("CRYSTAL", Items.diamond_sword, 1)
    )

    private val sword0 = listOf(
        "Firedust Dagger", "Kindlebane Dagger", "Pyrochaos Dagger"
    )

    private val sword1 = listOf(
        "Twilight Dagger", "Mawdredge Dagger", "Deathripper Dagger"
    )

    @SubscribeEvent
    fun onLeftClick(event: ClickEvent.LeftClickEvent) {
        if (!inSkyblock) return
        val list = mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>().filter {
            it.getDistanceSqToEntity(mc.thePlayer) < 36 && attunements.any { attunement ->
                StringUtils.stripControlCodes(it.name).startsWith(attunement.name)
            }
        }
        val attunementArmorStand = if (list.size == 1) list.first() else list.firstOrNull {
            mc.thePlayer.run {
                val look: Vec3 = getLook(1.0f)
                val lookVec = getPositionEyes(1.0f).addVector(
                    look.xCoord * 6, look.yCoord * 6, look.zCoord * 6
                )
                it.entityBoundingBox.offset(0.0, -1.0, 0.0).expand(0.5, 1.0, 0.5)
                    .calculateIntercept(mc.thePlayer.getPositionEyes(1.0f), lookVec)
            } != null
        } ?: return
        attunements.find {
            StringUtils.stripControlCodes(attunementArmorStand.name).startsWith(it.name)
        }?.let { swapTo(it) }
    }

    private fun swapTo(attunement: Attunement) {
        if (System.currentTimeMillis() - lastAttuneTime < 400) return
        val swordNames = if (attunement.type == 0) sword0 else sword1
        for (i in 0..8) {
            val item = mc.thePlayer.inventory.getStackInSlot(i) ?: continue
            if (swordNames.any { item.displayName.contains(it) }) {
                mc.thePlayer.inventory.currentItem = i
                if (item.item != attunement.sword) {
                    rightClick()
                }
                lastAttuneTime = System.currentTimeMillis()
                return
            }
        }
    }

    data class Attunement(val name: String, val sword: Item, val type: Int)
}
