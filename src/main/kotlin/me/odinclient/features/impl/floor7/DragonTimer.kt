package me.odinclient.features.impl.floor7

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.events.impl.ReceivePacketEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.ui.hud.HudElement
import me.odinclient.ui.hud.TextHud
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.render.gui.nvg.getTextWidth
import me.odinclient.utils.render.gui.nvg.textWithControlCodes
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
    description = "Displays a timer for when M7 dragons spawn.",
    category = Category.FLOOR7
) {

    private val textScale: Float by NumberSetting(name = "Text scale", default = 1f, min = 0f, max = 10f, increment = 0.1f)
    private val textBackground: Boolean by BooleanSetting(name = "Text background")
    private val increaseWithDistance: Boolean by BooleanSetting(name = "Increase With Distance")

    private const val dragonSpawnTime = 5000L

    // TODO: add a background to make it more readable (a setting)
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, true) {
        if (it) {
            textWithControlCodes("§5Purple spawning in §a4500ms", 1f, 9f, 16f, Fonts.REGULAR)
            textWithControlCodes("§cRed spawning in §e1200ms", 1f, 26f, 16f, Fonts.REGULAR)
            max(getTextWidth("Purple spawning in 4500ms", 16f, Fonts.REGULAR),
                getTextWidth("Red spawning in 1200ms", 16f, Fonts.REGULAR)
            ) + 2f to 33f
        } else if (toRender.size != 0) {
            var width = 0f
            toRender.forEachIndexed { index, triple ->
                textWithControlCodes(triple.first, 1f, 9f + index * 17f, 16f, Fonts.REGULAR)
                width = max(width, getTextWidth(triple.first.noControlCodes, 16f, Fonts.REGULAR))
            }
            width to toRender.size * 17f
        } else 0f to 0f
    }

    private var times: MutableMap<DC, Long> = mutableMapOf(
        DC.Orange to 0L,
        DC.Red to 0L,
        DC.Green to 0L,
        DC.Blue to 0L,
        DC.Purple to 0L
    )

    private var toRender: MutableList<Triple<String, Int, DC>> = ArrayList()

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



        DC.entries.forEach { c ->
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
        DC.entries.forEach { it.checkAlive() }
        val currentTime = System.currentTimeMillis()
        toRender = ArrayList()

        DC.entries.forEachIndexed { index, dragonColor ->
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
                increase = true,
                renderBlackBox = true,
                scale = textScale
            )
        }
    }

    @SubscribeEvent
    fun onWorldLoad(@Suppress("UNUSED_PARAMETER") event: WorldEvent.Load) {
        times.replaceAll { _, _ -> 0L }
        toRender = ArrayList()
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