package me.odinmain.features.impl.skyblock

import me.odinmain.OdinMain.isLegitVersion
import me.odinmain.events.impl.ClickEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.addVec
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.findNearestGrassBlock
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.DianaBurrowEstimate.activeBurrows
import me.odinmain.utils.toVec3
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import kotlin.math.roundToInt

object DianaHelper : Module(
    name = "Diana Helper",
    desc = "Displays the location of the Diana guess and burrows."
) {
    private val guessColor by ColorSetting("Guess Color", Colors.WHITE, allowAlpha = true, desc = "Color of the guess text.")
    private val tracer by BooleanSetting("Tracer", true, desc = "Draws a line from your position to the guess.")
    private val tracerWidth by NumberSetting("Tracer Width", 5f, 1f, 20f, desc = "Width of the tracer line.").withDependency { tracer }
    private val tracerColor by ColorSetting("Tracer Line Color", Colors.WHITE, allowAlpha = true, desc = "Color of the tracer line.").withDependency { tracer }
    private val tracerBurrows by BooleanSetting("Tracer Burrows", true, desc = "Draws a line from your position to the burrows.")
    private val style by SelectorSetting("Style", "Filled", arrayListOf("Filled", "Outline", "Filled Outline"), desc = "Whether or not the box should be filled.")
    private val sendInqMsg by BooleanSetting("Send Inq Msg", true, desc = "Sends your coordinates to the party chat when you dig out an inquisitor.")
    private val showWarpSettings by DropdownSetting("Show Warp Settings")
    private val castle by BooleanSetting("Castle Warp", desc = "Warp to the castle.").withDependency { showWarpSettings }
    private val crypt by BooleanSetting("Crypt Warp", desc = "Warp to the crypt.").withDependency { showWarpSettings }
    private val stonks by BooleanSetting("Stonks Warp", desc = "Warp to the stonks.").withDependency { showWarpSettings }
    private val darkAuction by BooleanSetting("DA Warp", desc = "Warp to the dark auction.").withDependency { showWarpSettings }
    private val museum by BooleanSetting("Museum Warp", desc = "Warp to the museum.").withDependency { showWarpSettings }
    private val wizard by BooleanSetting("Wizard Warp", desc = "Warp to the wizard.").withDependency { showWarpSettings }
    private val warpKeybind by KeybindSetting("Warp Keybind", Keyboard.KEY_NONE, description = "Keybind to warp to the nearest warp location.").onPress {
        if (!cmdCooldown.hasTimePassed()) return@onPress
        sendCommand("warp ${warpLocation?.name ?: return@onPress}")
        warpLocation = null
    }
    private val autoWarp by BooleanSetting("Auto Warp", desc = "Automatically warps you to the nearest warp location after you activate the spade ability.").withDependency { !isLegitVersion }
    private val autoWarpWaitTime by NumberSetting("Auto Warp Wait Time", 2f, 0.2, 10.0, 0.1, unit = "s", desc = "Time to wait before warping.").withDependency { autoWarp }
    private val resetBurrows by ActionSetting("Reset Burrows", desc = "Removes all the current burrows.") { activeBurrows.clear() }
    private var warpLocation: WarpPoint? = null

    private var isDoingDiana: Boolean = false
    private val cmdCooldown = Clock(3_000)
    var renderPos: Vec3? = null

    private inline val hasSpade: Boolean
        get() = mc.thePlayer?.inventory?.mainInventory?.find { it.skyblockID == "ANCESTRAL_SPADE" } != null

    init {
        execute(2000) {
            if (!isDoingDiana)
                isDoingDiana = enabled && LocationUtils.currentArea.isArea(Island.Hub) && hasSpade
        }

        onPacket<S29PacketSoundEffect> ({ isDoingDiana }) {
            DianaBurrowEstimate.handleSoundPacket(it)
        }

        onPacket<S2APacketParticles> ({ isDoingDiana }) {
            DianaBurrowEstimate.handleParticlePacket(it)
            DianaBurrowEstimate.handleBurrow(it)
        }

        onPacket<C08PacketPlayerBlockPlacement> ({ isDoingDiana }) {
            DianaBurrowEstimate.blockEvent(it.position)
        }

        onPacket<C07PacketPlayerDigging> ({ isDoingDiana }) {
            DianaBurrowEstimate.blockEvent(it.position)
        }

        onWorldLoad {
            DianaBurrowEstimate.onWorldLoad()
            isDoingDiana = false
            renderPos = null
        }

        onMessage(Regex("^(Uh oh!|Woah!|Yikes!|Oi!|Danger!|Good Grief!|Oh!) You dug out a Minos Inquisitor!\$")) {
            if (sendInqMsg) partyMessage("${PlayerUtils.getPositionString()} I dug up an inquisitor come over here!")
            PlayerUtils.alert("§6§lInquisitor!")
        }

        onMessage(Regex("^(You dug out a Griffin Burrow! .+|You finished the Griffin burrow chain! \\(4\\/4\\))\$")) {
            DianaBurrowEstimate.onBurrowDug()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!isDoingDiana || (renderPos == null && activeBurrows.isEmpty())) return
        renderPos?.let { guess ->
            val distance = mc.thePlayer.positionVector.distanceTo(guess)
            if (guess.yCoord == 110.0 && distance < 64) {
                renderPos = findNearestGrassBlock(guess)
                return@let
            }
            warpLocation = WarpPoint.entries.filter { it.unlocked() }.minBy { warp ->
                warp.location.distanceTo(guess)
            }.takeIf { it.location.distanceTo(guess) + 35 < distance }

            if (tracer && distance > 15)
                Renderer.drawTracer(guess.addVec(.5, .5, .5), color = tracerColor, lineWidth = tracerWidth, depth = false)

            Renderer.drawCustomBeacon("§6Guess ${warpLocation?.displayName ?: ""}§r", guess, guessColor, increase = true, style = style)
        }

        activeBurrows.forEach { (location, burrow) ->
            if (tracerBurrows) Renderer.drawTracer(location.toVec3().addVec(.5, .5, .5), color = burrow.type.color, lineWidth = tracerWidth, depth = false)
            Renderer.drawCustomBeacon(burrow.type.text, location.toVec3(), burrow.type.color.withAlpha(0.75f), style = style)
        }
    }

    @SubscribeEvent
    fun onRightClick(event: ClickEvent.Right) {
        if (!isDoingDiana || !isHolding("ANCESTRAL_SPADE") || !autoWarp || isLegitVersion) return
        runIn((autoWarpWaitTime * 20).roundToInt()) {
            if (!cmdCooldown.hasTimePassed()) return@runIn
            modMessage("§6Warping to ${warpLocation?.displayName ?: return@runIn}")
            sendCommand("warp ${warpLocation?.name ?: return@runIn}")
            warpLocation = null
        }
    }

    private enum class WarpPoint(
        val displayName: String,
        val location: Vec3,
        var unlocked: () -> Boolean
    ) {
        HUB     (displayName = "§8(§fHub§8)",          Vec3(-3.0, 70.0, -70.0),   { true }),
        CASTLE  (displayName = "§8(§fCastle§8)",       Vec3(-250.0, 130.0, 45.0), { castle }),
        CRYPT   (displayName = "§8(§fCrypt§8)",        Vec3(-190.0, 74.0, -88.0), { crypt }),
        STONKS  (displayName = "§8(§fStonks§8)",       Vec3(-52.5, 70.0, -49.5),  { stonks }),
        DA      (displayName = "§8(§fDark Auction§8)", Vec3(91.0, 75.0, 173.0),   { darkAuction }),
        MUSEUM  (displayName = "§8(§fMuseum§8)",       Vec3(-75.0, 76.0, 81.0),   { museum }),
        WIZARD  (displayName = "§8(§fWizard§8)",       Vec3(42.5, 122.0, 69.0),   { wizard })
    }
}