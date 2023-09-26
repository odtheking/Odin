package me.odinclient.features.impl.skyblock

import cc.polyfrost.oneconfig.libs.eventbus.Subscribe
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs
import kotlin.math.pow

object DianaHelper : Module(
    name = "Diana Helper",
    description = "Helps with Diana's event.",
    category = Category.SKYBLOCK,
    tag = TagType.NEW
) {
    private val S29PacketSoundEffect.pos: Vec3
        get() = Vec3(this.x, this.y, this.z)
    private var lastPos: Vec3? = null
    private var renderPos: Vec3? = null

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            if (it.soundName != "note.harp") return@onPacket
            val directionVec = it.pos.subtract(lastPos ?: it.pos)
            lastPos = it.pos

            val correct: Double = if (it.pitch > 1.05)
                -1.0
            else if (it.pitch > 0.92)
                1.5 - it.pitch
            else if (it.pitch > 0.77)
                 2.0 / it.pitch.pow(2)
            else
                 -abs(0.4 / (0.7 - it.pitch))

            val distance = 4 / it.pitch.pow(6) + 0.2 / it.pitch.pow(5) - correct;
            renderPos = it.pos.add(directionVec.multiplyXZ(distance.toFloat()))
            //modMessage(lastPos!!.add(directionVec.multiply(dist)))
        }


        //RenderUtils.renderBoxText()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        renderPos?.let {
            RenderUtils.renderBoxText("Diana", it, Color.WHITE)
        }
    }

    private fun Vec3.multiplyXZ(distance: Float): Vec3 {
        return Vec3(this.xCoord * distance, this.yCoord, this.zCoord * distance)
    }

}