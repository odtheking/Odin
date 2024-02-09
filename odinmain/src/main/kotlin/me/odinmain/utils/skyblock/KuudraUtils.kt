package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.skyblock.Kuudra
import me.odinmain.features.impl.skyblock.Kuudra.highlightFreshColor
import me.odinmain.features.impl.skyblock.Kuudra.nameColor
import me.odinmain.features.impl.skyblock.Kuudra.supplyWaypointColor
import me.odinmain.utils.addVec
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderBoundingBox
import me.odinmain.utils.render.world.RenderUtils.renderCustomBeacon
import me.odinmain.utils.render.world.RenderUtils.renderVec
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityGiantZombie
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import kotlin.math.cos
import kotlin.math.sin
import net.minecraft.util.AxisAlignedBB as AABB

object KuudraUtils {
    var kuudraTeammates = ArrayList<KuddraPlayer>()
    var giantZombies: MutableList<EntityGiantZombie> = mutableListOf()
    var supplies = BooleanArray(6) { true }
    var kuudraEntity: EntityMagmaCube? = null
    data class BoxParameters(
        val x: Double,
        val y: Double,
        val z: Double,
        val color: Color,
        val scale: Double,
        val depth: Double,
        val isInnerBox: Boolean
    )

    data class KuddraPlayer(
        val playerName: String,
        var eatFresh: Boolean = false,
        var eatFreshTime: Long = 0,
        val entity: EntityPlayer? = null
    )

    private val boxParametersList = listOf(
        // Box 1
        BoxParameters(-97.0, 157.0, -112.0, Color(0, 0, 255), Kuudra.pearlBox, 1.0, true),
        BoxParameters(-70.5, 79.0, -134.5, Color(0, 0, 255), 2.0, 1.0, false),
        BoxParameters(-85.5, 78.0, -128.5, Color(0, 0, 255), 2.0, 1.0, false),

        // Box 2
        BoxParameters(-95.5, 161.0, -105.5, Color(0, 0, 255), Kuudra.pearlBox, 1.0, true),
        BoxParameters(-67.5, 77.0, -122.5, Color(0, 255, 0), 2.0, 1.0, false),

        // Box 3 (X)
        BoxParameters(-103.0, 160.0, -109.0, Color(0, 0, 255), Kuudra.pearlBox, 1.0, true),
        BoxParameters(-134.5, 77.0, -138.5, Color(255, 255, 255), 1.0, 1.0, false),
        BoxParameters(-130.5, 79.0, -113.5, Color(150, 15, 255), 1.0, 1.0, false),
        BoxParameters(-110.0, 155.0, -106.0, Color(0, 0, 255), Kuudra.pearlBox, 1.0, true),

        // Box 4 (Square)
        BoxParameters(-43.5, 120.0, -149.5, Color(0, 0, 255), Kuudra.pearlBox, 1.0, true),
        BoxParameters(-45.5, 135.0, -138.5, Color(0, 0, 255), Kuudra.pearlBox, 1.0, true),
        BoxParameters(-35.5, 138.0, -124.5, Color(0, 0, 255), Kuudra.pearlBox, 1.0, true),
        BoxParameters(-26.5, 126.0, -111.5, Color(0, 0, 255), Kuudra.pearlBox, 1.0, true),
        BoxParameters(-140.5, 78.0, -90.5, Color(255, 0, 0), 0.0, 1.0, false),

        // Box 5 (=)
        BoxParameters(-106.0, 165.0, -101.0, Color(0, 0, 255), Kuudra.pearlBox, 1.0, true),
        BoxParameters(-65.5, 76.0, -87.5, Color(0, 255, 0), 0.0, 1.0, false),

        // Box 6 (/)
        BoxParameters(-105.0, 157.0, -98.0, Color(0, 0, 255), Kuudra.pearlBox, 1.0, true),
        BoxParameters(-112.5, 76.5, -68.5, Color(0, 0, 255), 0.0, 1.0, false)
    )


    fun handleArmorStand(event: EntityJoinWorldEvent) {
        if (event.entity is EntityArmorStand && Kuudra.phase == 1 && !event.entity.name.contains("Lv") && !event.entity.toString().contains("name=Armor Stand")) {
            if (event.entity.name.contains("SUPPLIES RECEIVED")) {
                val x = event.entity.posX.toInt()
                val z = event.entity.posZ.toInt()

                if (x == -98 && z == -112) supplies[0] = false
                if (x == -98 && z == -99) supplies[1] = false
                if (x == -110 && z == -106) supplies[2] = false
                if (x == -106 && z == -112) supplies[3] = false
                if (x == -94 && z == -106) supplies[4] = false
                if (x == -106 && z == -99) supplies[5] = false
            }
        }
    }

    fun cancelArmorStandEvent(event: EntityLivingBase) {
        if (event is EntityArmorStand && event.toString().noControlCodes.contains("[\"[Lv\"]"))
            mc.theWorld.removeEntity(event)

    }


    fun renderTeammatesNames() {
        kuudraTeammates.forEach { teammate ->
            val player = teammate.entity ?: return@forEach
            RenderUtils.drawStringInWorld(player.name, teammate.entity.renderVec.addVec(y = 2.6),
                if (teammate.eatFresh) highlightFreshColor.rgba else nameColor.rgba,
                depthTest = false, increase = false, renderBlackBox = false,
                scale = 0.05f
            )
        }
    }

    fun renderPearlBoxes() {
        for (box in boxParametersList) {
            if (box.isInnerBox) RenderUtils.drawBoxOutline(box.x, box.y, box.z, box.scale, box.color, 3f, true)
            else RenderUtils.drawFilledBox(AABB(box.x, box.y, box.z, box.x + 1, box.y + 1, box.z + 1), box.color, true)
        }
    }

    fun renderGiantZombies() {
        giantZombies.forEach {
            val yaw = it.rotationYaw
            renderCustomBeacon("Supply", x = it.posX + (3.7 * cos((yaw + 130) * (Math.PI / 180))),
                y = 72.0, it.posZ + (3.7 * sin((yaw + 130) * (Math.PI / 180))), supplyWaypointColor, true)
        }
    }

    fun highlightKuudra() {
        kuudraEntity?.renderBoundingBox?.let { RenderUtils.drawBoxOutline(it, Color(255, 0, 0), 3f, true) }
    }

}