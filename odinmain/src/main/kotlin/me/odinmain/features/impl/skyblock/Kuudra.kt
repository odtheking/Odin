package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.DrawSlotEvent
import me.odinmain.events.impl.EntityLeaveWorldEvent
import me.odinmain.events.impl.GuiClickEvent
import me.odinmain.events.impl.RenderEntityModelEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.hud.HudElement
import me.odinmain.ui.util.getTextWidth
import me.odinmain.ui.util.text
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.OutlineUtils
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.KuudraUtils.addGiantZombie
import me.odinmain.utils.skyblock.KuudraUtils.addKuudraTeammate
import me.odinmain.utils.skyblock.KuudraUtils.cancelArmorStandEvent
import me.odinmain.utils.skyblock.KuudraUtils.getKuudra
import me.odinmain.utils.skyblock.KuudraUtils.handleArmorStand
import me.odinmain.utils.skyblock.KuudraUtils.highlightKuudra
import me.odinmain.utils.skyblock.KuudraUtils.kuudraTeammates
import me.odinmain.utils.skyblock.KuudraUtils.renderGiantZombies
import me.odinmain.utils.skyblock.KuudraUtils.renderPearlBoxes
import me.odinmain.utils.skyblock.KuudraUtils.renderTeammatesNames
import me.odinmain.utils.skyblock.KuudraUtils.supplies
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Kuudra : Module(
    name = "Kuddra",
    description = "Various kuudra utilities",
    category = Category.SKYBLOCK
) {
    val playerOutline: Boolean by BooleanSetting("Player Outline", true)
    val highlightName: Boolean by BooleanSetting("Name Highlight", true)
    val outlineColor: Color by ColorSetting("Outline Color", Color.PURPLE, true)
    val nameColor: Color by ColorSetting("Name Color", Color.PINK, true)

    private val renderNameTags: Boolean by BooleanSetting("Disable name tags", true)

    private val removePerks: Boolean by BooleanSetting("Remove Perks", true)
    private val renderStun: Boolean by BooleanSetting("Render Stun", true)

    private val suppliesWaypoints: Boolean by BooleanSetting("Supplies Waypoints", true)
    val supplyWaypointColor: Color by ColorSetting("Supply Waypoint Color", Color.YELLOW, true)

    private val pearlWaypoints: Boolean by BooleanSetting("Pearl Waypoints", true)
    val pearlBox: Double by NumberSetting("pearlBox", 1.0, 0.5, 1.5)

    private val buildHelper: Boolean by BooleanSetting("Build Helper", true)

    private val notifyFresh: Boolean by BooleanSetting("Notify Fresh", true)
    private val highlightFresh: Boolean by BooleanSetting("Highlight Fresh", true)
    val highlightFreshColor: Color by ColorSetting("Highlight Fresh Color", Color.YELLOW, true)
    private val hud: HudElement by HudSetting("Fresh tools timer", 10f, 10f, 1f, true) {
        if (it) {
            text("§4Fresh Tools: 9s", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR)
            getTextWidth("Fresh Tools: 10s", 12f) + 2f to 16f
        } else {
            text("§4Fresh Tools: ${kuudraTeammates.find { it.playerName == mc.thePlayer.name }?.eatFreshTime?.minus(System.currentTimeMillis())}s", 1f, 9f, Color.WHITE,12f, OdinFont.REGULAR)
            getTextWidth("Fresh Tools: 10s", 12f) + 2f to 12f
        }
    }

    private val kuudraHighlight: Boolean by BooleanSetting("Kuudra Highlight", true)


    var phase = 0

    init {
        onMessage(Regex("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!")) {
            phase = 1
            modMessage("Starting kuudra run with ${kuudraTeammates.map { it.playerName }}")
        }
        onMessage(Regex("[NPC] Elle: OMG! Great work collecting my supplies!")) { phase = 2 }
        onMessage(Regex("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!")) { phase = 3 }
        onMessage(Regex("[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!")) { phase = 4 }

        onWorldLoad {
            phase = 0
            kuudraTeammates = ArrayList()
            supplies = BooleanArray(6) { true }
        }

        onMessage(Regex("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!"))
        {
            kuudraTeammates.forEach { kuddraPlayer ->
                kuddraPlayer.eatFresh = true
                kuddraPlayer.eatFreshTime = System.currentTimeMillis()
                execute(10000, 0) {
                    kuddraPlayer.eatFresh = false
                }
            }
            partyMessage("FRESH")
        }
        onMessage(Regex("^Party > ?(?:\\[.+])? (.{0,16}): FRESH")) {
            val (_, playerName) = Regex("^Party > ?(?:\\[.+])? (.{0,16}): FRESH").find(it)?.destructured ?: return@onMessage
            kuudraTeammates.forEach { kuddraPlayer ->
                if (kuddraPlayer.playerName == playerName) kuddraPlayer.eatFresh = true
            }
        }
    }
    @SubscribeEvent
    fun joinWorldEvent(event: EntityJoinWorldEvent) {
        addGiantZombie(event)
        addKuudraTeammate(event)
        handleArmorStand(event)
        getKuudra(event)
        if (renderNameTags) cancelArmorStandEvent(event)
    }
    @SubscribeEvent
    fun leaveWorldEvent(event: EntityLeaveWorldEvent) {
        KuudraUtils.leaveWorldEvent(event)
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (event.entity == mc.thePlayer || !playerOutline) return
        val teammate = kuudraTeammates.find { it.entity == event.entity } ?: return

        OutlineUtils.outlineEntity(event, 5f, if (teammate.eatFresh) highlightFreshColor else nameColor, true)
    }
    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (highlightName) renderTeammatesNames()
        if (pearlWaypoints) renderPearlBoxes()
        if (suppliesWaypoints) renderGiantZombies()
        if (kuudraHighlight) highlightKuudra()
    }
    @SubscribeEvent
    fun renderSlot(event: DrawSlotEvent) {
        if (!removePerks || !event.slot.toString().contains("ContainerLocalMenu: Perk Menu")) return
        val slotName = event.slot.stack.displayName.noControlCodes
        if (slotName.containsOneOf("Steady Hands", "Bomberman") || slotName == "Elle's Lava Rod" || slotName == "Elle's Pickaxe" ||
            slotName == "Auto Revive" || (renderStun && slotName.containsOneOf("Mining Frenzy", "Human Cannonball")))
            event.isCanceled = true
    }
    @SubscribeEvent
    fun guiMouseClick(event: GuiClickEvent) {
        if (event.gui !is GuiChest || event.gui.inventorySlots !is ContainerChest || !removePerks || (event.gui.inventorySlots as ContainerChest).name != "Perk Menu") return
        val slot = event.gui.slotUnderMouse.stack.displayName.noControlCodes
        if (slot.containsOneOf("Steady Hands", "Bomberman") || slot == "Elle's Lava Rod" || slot == "Elle's Pickaxe" ||
            slot == "Auto Revive" || (renderStun && slot.containsOneOf("Mining Frenzy", "Human Cannonball")))
            event.isCanceled = true
    }
}