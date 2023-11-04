package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.VecUtils
import me.odinmain.utils.VecUtils.addVec
import me.odinmain.utils.VecUtils.toVec3i
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.floor
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderVec
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.DianaBurrowEstimate
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


object DianaHelper : Module(
    name = "Diana Helper",
    description = "Helps with Diana's event.",
    category = Category.SKYBLOCK,
    tag = TagType.NEW
) {
    private val guessColor: Color by ColorSetting("Guess Color", default = Color.WHITE)
    private val tracerColor: Color by ColorSetting("Tracer Line Color", default = Color.WHITE, allowAlpha = true)
    private val tracer: Boolean by BooleanSetting("Tracer", default = false)
    private val tracerWidth: Int by NumberSetting("Tracer Width", default = 5, min = 1, max = 20)
    private val sendInqMsg: Boolean by BooleanSetting("Send Inq Msg", default = true)
    private val showWarpSettings: Boolean by BooleanSetting("Show Warp Settings", default = true)
    private val castle: Boolean by BooleanSetting("Castle Warp").withDependency { showWarpSettings }
    private val crypt: Boolean by BooleanSetting("Crypt Warp").withDependency { showWarpSettings }
    private val darkAuction: Boolean by BooleanSetting("DA Warp").withDependency { showWarpSettings }
    private val museum: Boolean by BooleanSetting("Museum Warp").withDependency { showWarpSettings }
    private val wizard: Boolean by BooleanSetting("Wizard Warp").withDependency { showWarpSettings }
    private var warpLocation: WarpPoint? = null

    private val cmdCooldown = Clock(3_000)
    var renderPos: Vec3? = null
    val burrowsRender = mutableMapOf<Vec3i, BurrowType>()

    enum class BurrowType(val text: String, val color: Color) {
        START("§aStart", Color.GREEN),
        MOB("§cMob", Color.RED),
        TREASURE("§6Treasure", Color.ORANGE),
        UNKNOWN("§fUnknown?!", Color.WHITE),
    }


    init {
        onPacket(S29PacketSoundEffect::class.java) { DianaBurrowEstimate.handleSoundPacket(it) }

        onPacket(S2APacketParticles::class.java) { DianaBurrowEstimate.handleParticlePacket(it) }

        onPacket(S2APacketParticles::class.java) { DianaBurrowEstimate.handleBurrow(it) }

        onWorldLoad {
            DianaBurrowEstimate.reset()
            burrowsRender.clear()
            renderPos = null
            DianaBurrowEstimate.recentBurrows.clear()
        }
    }


    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        DianaBurrowEstimate.chat(event)

        if(!event.message.contains("You dug out ") || !event.message.contains("Inquis") || !enabled || !sendInqMsg) return

        ChatUtils.modMessage("x: ${PlayerUtils.posX.floor().toInt()}, y: ${PlayerUtils.posY.floor().toInt()}, z: ${PlayerUtils.posZ.floor().toInt()}")
        PlayerUtils.alert("§6§lInquisitor!")


    }

    @SubscribeEvent
    fun onInteract(event: PacketSentEvent) {
        if (event.packet is C08PacketPlayerBlockPlacement)
            DianaBurrowEstimate.blockEvent(event.packet.position.toVec3i())
        else if (event.packet is C07PacketPlayerDigging)
            DianaBurrowEstimate.blockEvent(event.packet.position.toVec3i())
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        renderPos?.let { guess ->
            warpLocation = WarpPoint.entries.filter { it.unlocked() }.minBy { warp ->
                warp.location.distanceTo(guess)
            }.takeIf { it.location.distanceTo(guess) + 30 < mc.thePlayer.positionVector.distanceTo(guess) }

            if (tracer)
                RenderUtils.draw3DLine(mc.thePlayer.renderVec.addVec(y = VecUtils.fastEyeHeight()), guess.addVec(.5, .5, .5), tracerColor, tracerWidth, depth = true, event.partialTicks)

            RenderUtils.renderCustomBeacon("§6Guess${warpLocation?.displayName ?: ""}§r", guess, guessColor, event.partialTicks)
        }

        val burrowsRenderCopy = HashMap(burrowsRender)

        burrowsRenderCopy.forEach { (location, type) ->
            RenderUtils.renderCustomBeacon(type.text, Vec3(location), type.color, event.partialTicks)
        }
    }

    override fun onKeybind() {
        if (!cmdCooldown.hasTimePassed()) return
        ChatUtils.sendCommand("warp ${warpLocation?.name ?: return}")
    }

    private enum class WarpPoint(
        val displayName: String,
        val location: Vec3,
        var unlocked: () -> Boolean
    ) {
        HUB     (displayName = " §8(§fHub§8)",          Vec3(-3.0, 70.0, -70.0),   { true }),
        CASTLE  (displayName = " §8(§fCastle§8)",       Vec3(-250.0, 130.0, 45.0), { castle }),
        CRYPT   (displayName = " §8(§fCrypt§8)",        Vec3(-190.0, 74.0, -88.0), { crypt }),
        DA      (displayName = " §8(§fDark Auction§8)", Vec3(91.0, 75.0, 173.0),   { darkAuction }),
        MUSEUM  (displayName = " §8(§fMuseum§8)",       Vec3(-75.0, 76.0, 81.0),   { museum }),
        WIZARD  (displayName = " §8(§fWizard§8)",       Vec3(42.5, 122.0, 69.0),   { wizard })
    }
}