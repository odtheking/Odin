package me.odinmain.features.impl.skyblock

import com.github.stivais.ui.color.Color
import me.odinmain.OdinMain
import me.odinmain.OdinMain.isLegitVersion
import me.odinmain.events.impl.ClickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.*
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
    description = "Displays the location of the Diana guess and burrows.",
    category = Category.SKYBLOCK
) {
    private val guessColor: Color by ColorSetting("Guess Color", Color.WHITE, allowAlpha = true, description = "Color of the guess text")
    private val tracer: Boolean by BooleanSetting("Tracer", default = false, description = "Draws a line from your position to the guess")
    private val tracerWidth: Float by NumberSetting("Tracer Width", default = 5f, min = 1f, max = 20f).withDependency { tracer }
    private val tracerColor: Color by ColorSetting("Tracer Line Color", Color.WHITE, allowAlpha = true, description = "Color of the tracer line").withDependency { tracer }
    private val tracerBurrows: Boolean by BooleanSetting("Tracer Burrows", default = false, description = "Draws a line from your position to the burrows")
    private val style: Int by SelectorSetting("Style", "Filled", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.")
    private val sendInqMsg: Boolean by BooleanSetting("Send Inq Msg", default = true, description = "Sends a message to the party when you dig out an inquisitor")
    private val showWarpSettings: Boolean by BooleanSetting("Show Warp Settings", default = true, description = "Shows the warp settings")
    private val castle: Boolean by BooleanSetting("Castle Warp").withDependency { showWarpSettings }
    private val crypt: Boolean by BooleanSetting("Crypt Warp").withDependency { showWarpSettings }
    private val darkAuction: Boolean by BooleanSetting("DA Warp").withDependency { showWarpSettings }
    private val museum: Boolean by BooleanSetting("Museum Warp").withDependency { showWarpSettings }
    private val wizard: Boolean by BooleanSetting("Wizard Warp").withDependency { showWarpSettings }
    private val autoWarp: Boolean by BooleanSetting("Auto Warp", description = "Automatically warps you to the nearest warp location 2 seconds after you activate the spade ability.").withDependency { !isLegitVersion }
    private var warpLocation: WarpPoint? = null

    private val cmdCooldown = Clock(3_000)
    var renderPos: Vec3? = null
    val burrowsRender = mutableMapOf<Vec3i, BurrowType>()
    private val hasSpade: Boolean
        get() = mc.thePlayer?.inventory?.mainInventory?.find { it.itemID == "ANCESTRAL_SPADE" } != null
    private val isDoingDiana: Boolean
        get() = hasSpade && LocationUtils.currentArea.isArea(Island.Hub) && enabled

    enum class BurrowType(val text: String, val color: Color) {
        START("§aStart", Color.GREEN),
        MOB("§cMob", Color.RED),
        TREASURE("§6Treasure", Color.MINECRAFT_GOLD),
        UNKNOWN("§fUnknown?!", Color.WHITE),
    }

    init {
        onPacket(S29PacketSoundEffect::class.java, { isDoingDiana }) {
            DianaBurrowEstimate.handleSoundPacket(it)
        }

        onPacket(S2APacketParticles::class.java, { isDoingDiana }) {
            DianaBurrowEstimate.handleParticlePacket(it)
            DianaBurrowEstimate.handleBurrow(it)
        }

        onPacket(C08PacketPlayerBlockPlacement::class.java, { isDoingDiana }) {
            DianaBurrowEstimate.blockEvent(it.position.toVec3i())
        }

        onPacket(C07PacketPlayerDigging::class.java, { isDoingDiana }) {
            DianaBurrowEstimate.blockEvent(it.position.toVec3i(), it.status == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK)
        }

        onWorldLoad {
            DianaBurrowEstimate.reset()
            burrowsRender.clear()
            renderPos = null
            DianaBurrowEstimate.recentBurrows.clear()
        }

        onMessage(Regex("^(Uh oh!|Woah!|Yikes!|Oi!|Danger!|Good Grief!|Oh!) You dug out a Minos Inquisitor!\$")) {
            if (sendInqMsg) partyMessage(PlayerUtils.getPositionString())
            PlayerUtils.alert("§6§lInquisitor!")
        }

        onMessage(Regex(".*")) {
            DianaBurrowEstimate.chat(it)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!LocationUtils.currentArea.isArea(Island.Hub)) return
        if (renderPos == null && burrowsRender.isEmpty()) return
        renderPos?.let { guess ->
            if (guess.yCoord == 110.0 && mc.thePlayer.positionVector.distanceTo(guess) < 64) {
                renderPos = findNearestGrassBlock(guess)
                return
            }
            warpLocation = WarpPoint.entries.filter { it.unlocked() }.minBy { warp ->
                warp.location.distanceTo(guess)
            }.takeIf { it.location.distanceTo(guess) + 35 < mc.thePlayer.positionVector.distanceTo(guess) }

            if (tracer)
                Renderer.draw3DLine(mc.thePlayer.renderVec.addVec(y = fastEyeHeight()), guess.addVec(.5, .5, .5), color = tracerColor, lineWidth = tracerWidth, depth = false)

            Renderer.drawCustomBeacon("§6Guess${warpLocation?.displayName ?: ""}§r", guess, guessColor, increase = true, style = style)
        }

        val burrowsRenderCopy = burrowsRender.toMap()

        burrowsRenderCopy.forEach { (location, type) ->
            if (tracerBurrows) Renderer.draw3DLine(mc.thePlayer.renderVec.addVec(y = fastEyeHeight()), Vec3(location).addVec(.5, .5, .5), color = type.color, lineWidth = tracerWidth, depth = false)
            Renderer.drawCustomBeacon(type.text, Vec3(location), type.color, style = style)
        }
    }

    @SubscribeEvent
    fun onRightClick(event: ClickEvent.RightClickEvent) {
        if (!isHolding("ANCESTRAL_SPADE") || !autoWarp || OdinMain.isLegitVersion) return
        runIn(40) {
            onKeybind()
        }
    }

    override fun onKeybind() {
        if (!cmdCooldown.hasTimePassed()) return
        sendCommand("warp ${warpLocation?.name ?: return}")
        warpLocation = null
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