package me.odinclient.features.impl.skyblock

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.ChatUtils
import net.minecraft.util.Vec3
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.client.event.RenderWorldLastEvent


object SoopyBurrow : Module(
    name = "SoopyBurrow",
    description = "Helps with Diana's event.",
    category = Category.SKYBLOCK,
    tag = TagType.NEW
) {

    private var renderPos: Vec3? = null

    private var dingIndex = 0
    private var lastDing = 0L
    private var lastDingPitch = 0f
    private var firstPitch = 0f

    private var lastSoundPoint: Vec3? = null

    private var dingSlope = mutableListOf<Float>()

    private var distance: Double? = null
    private var distance2: Double? = null

    val S29PacketSoundEffect.pos: Vec3
        get() = Vec3(this.x, this.y, this.z)

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        lastDing = 0L
        lastDingPitch = 0f
        firstPitch = 0f
        lastSoundPoint = null
        distance = null
        dingIndex = 0
        dingSlope.clear()
    }

    init {
        onPacket(S29PacketSoundEffect::class.java) {
            if (it.soundName != "note.harp") return@onPacket
            val pitch = it.pitch

            if (lastDing == 0L) {
                firstPitch = pitch
            }
            lastDing = System.currentTimeMillis()

            if (pitch < lastDingPitch) {
                firstPitch = pitch
                dingIndex = 0
                dingSlope.clear()
                lastDingPitch = pitch
                lastSoundPoint = null
                distance = null
            }

            if (lastDingPitch == 0f) {
                lastDingPitch = pitch
                distance = null
                lastSoundPoint = null
                return@onPacket
            }

            dingIndex++

            if (dingIndex > 1) dingSlope.add(pitch - lastDingPitch)
            if (dingSlope.size > 20) dingSlope.removeFirst()
            val slope =
                if (dingSlope.isNotEmpty()) dingSlope.reduce { a, b -> a + b }.toDouble() / dingSlope.size else 0.0
            val pos = it.pos
            lastSoundPoint = pos
            lastDingPitch = pitch

            distance2 = (Math.E / slope)
            //ChatUtils.modMessage("distance2 is $distance2")
            if (distance2!! > 1000) {
                ChatUtils.modMessage("distance too big: $distance2")
                distance2 = null
                return@onPacket
            }

            distance = distance2!!
            ChatUtils.modMessage("distance is $distance")


            val directionVec = it.pos.subtract(lastSoundPoint ?: it.pos)
            //render pos seems to just end whenever the particles end instead of using the distance  variable
            renderPos = it.pos.add(directionVec.multiplyXZ(distance!!))
            ChatUtils.modMessage(lastSoundPoint!!.add(directionVec.multiplyXZ(distance!!)))

        }



    }
    private fun Vec3.multiplyXZ(distance: Double): Vec3 {
        return Vec3(this.xCoord * distance, this.yCoord, this.zCoord * distance)
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        renderPos?.let {
            RenderUtils.renderBoxText("Diana", it, Color.WHITE)
        }
    }



}