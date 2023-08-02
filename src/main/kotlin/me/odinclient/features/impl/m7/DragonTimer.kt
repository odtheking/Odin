package me.odinclient.features.impl.m7

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.ReceivePacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.ui.hud.TextHud
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.WorldUtils
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.max

object DragonTimer : Module(
    "Dragon Timer",
    category = Category.M7
) {
    private const val dragonSpawnTime = 5000L

    private val hud: Boolean by HudSetting("Dragon Timer Hud", DragonTimerHud)

    private var times: MutableMap<DC, Long> = mutableMapOf(
        DC.Orange to 0L,
        DC.Red to 0L,
        DC.Green to 0L,
        DC.Blue to 0L,
        DC.Purple to 0L
    )

    var toRender: MutableList<Triple<String, Int, DC>> = ArrayList()

    @SubscribeEvent
    fun onReceivePacket(event: ReceivePacketEvent) {
        if (event.packet !is S2APacketParticles /*|| LocationUtils.getPhase() != 5*/) return
        val particle = event.packet

        if (
            particle.particleCount != 20 ||
            particle.yCoordinate != 19.0 ||
            particle.particleType != EnumParticleTypes.FLAME ||
            particle.xOffset != 2f ||
            particle.yOffset != 3f ||
            particle.zOffset != 2f ||
            particle.particleSpeed != 0f ||
            !particle.isLongDistance ||
            particle.xCoordinate % 1 != 0.0 ||
            particle.zCoordinate % 1 != 0.0
        ) return



        DC.values().forEach { c ->
            if (checkParticle(particle, c) && times[c] == 0L) {
                times[c] = System.currentTimeMillis()
            }
        }
    }

    private fun checkParticle(event: S2APacketParticles, color: DC): Boolean {
        val (x, y, z) = listOf(event.xCoordinate, event.yCoordinate, event.zCoordinate)
        return  x >= color.x.first && x <= color.x.second &&
                y >= 15 && y <= 22 &&
                z >= color.z.first && z <= color.z.second
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        DC.values().forEach { it.checkAlive() }
        val currentTime = System.currentTimeMillis()
        toRender = ArrayList()

        DC.values().forEachIndexed { index, dragonColor ->
            val time = times[dragonColor] ?: 0L
            if (time == 0L || !dragonColor.alive) return@forEachIndexed
            if (currentTime - time < dragonSpawnTime) {
                val spawnTime = dragonSpawnTime - (currentTime - time)
                val colorCode = when {
                    spawnTime <= 1000 -> "§c"
                    spawnTime <= 3000 -> "§e"
                    else -> "§a"
                }
                toRender.add(
                    Triple(
                        "§${dragonColor.color}${dragonColor} spawning in ${colorCode}${spawnTime} ms",
                        index,
                        dragonColor
                    )
                )
            } else {
                times[dragonColor] = 0L
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (toRender.size == 0) return
        toRender.forEach {
            RenderUtils.drawStringInWorld(
                it.first,
                it.third.textPos,
                depthTest = false,
                increase = false,
                renderBlackBox = true,
                scale = max(1.0, mc.thePlayer.positionVector.distanceTo(it.third.textPos) / 5.0).toFloat()
            )
        }
    }

    @SubscribeEvent
    fun onWorldLoad(@Suppress("UNUSED_PARAMETER") event: WorldEvent.Load) {
        times.replaceAll { _, _ -> 0L }
        toRender = ArrayList()
    }

    object DragonTimerHud : TextHud(0f, 0f) {
        override fun getLines(example: Boolean): MutableList<String> {
            return if (example) {
                mutableListOf(
                    "§6Purple spawning in §a4500ms",
                    "§cRed spawning in §e1200ms"
                )
            } else if (toRender.size != 0) {
                toRender.map { it.first }.toMutableList()
            } else mutableListOf()
        }
    }

    enum class DC(
        private val pos: BlockPos,
        val color: String,
        val x: Pair<Int, Int>,
        val z: Pair<Int, Int>,
        val textPos: Vec3,
        var alive: Boolean
    ) {
        Red(
            BlockPos(32, 22, 59),
            "c",
            Pair(24, 30),
            Pair(56, 62),
            Vec3(27.0, 18.0, 60.0),
            true,
        ),
        Orange(
            BlockPos(80, 23, 56),
            "6",
            Pair(82, 88),
            Pair(53, 59),
            Vec3(84.0, 18.0, 56.0),
            true
        ),
        Green(
            BlockPos(32, 23, 94),
            "a",
            Pair(23, 29),
            Pair(91, 97),
            Vec3(26.0, 18.0, 95.0),
            true
        ),
        Blue(
            BlockPos(79, 23, 94),
            "b",
            Pair(82, 88),
            Pair(91, 97),
            Vec3(84.0, 18.0, 95.0),
            true
        ),
        Purple(
            BlockPos(56, 22, 120),
            "5",
            Pair(53, 59),
            Pair(122, 128),
            Vec3(57.0, 18.0, 125.0),
            true
        );

        fun checkAlive() {
            this.alive = WorldUtils.getBlockIdAt(this.pos) != 0
        }
    }
}