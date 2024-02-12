package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.DrawSlotEvent
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
import me.odinmain.utils.*
import me.odinmain.utils.ServerUtils.getPing
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.OutlineUtils
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.KuudraUtils.cancelArmorStandEvent
import me.odinmain.utils.skyblock.KuudraUtils.giantZombies
import me.odinmain.utils.skyblock.KuudraUtils.handleArmorStand
import me.odinmain.utils.skyblock.KuudraUtils.highlightKuudra
import me.odinmain.utils.skyblock.KuudraUtils.kuudraEntity
import me.odinmain.utils.skyblock.KuudraUtils.kuudraTeammates
import me.odinmain.utils.skyblock.KuudraUtils.renderGiantZombies
import me.odinmain.utils.skyblock.KuudraUtils.renderPearlBoxes
import me.odinmain.utils.skyblock.KuudraUtils.renderTeammatesNames
import me.odinmain.utils.skyblock.KuudraUtils.supplies
import me.odinmain.utils.skyblock.PlayerUtils.alert
import me.odinmain.utils.skyblock.PlayerUtils.posX
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.PlayerUtils.posZ
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.entity.monster.EntityGiantZombie
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Kuudra : Module(
    name = "Kuudra",
    description = "Various kuudra utilities",
    category = Category.SKYBLOCK
) {
    private val playerOutline: Boolean by BooleanSetting("Player Outline", true, description = "Outlines the player")
    private val highlightName: Boolean by BooleanSetting("Name Highlight", true, description = "Highlights the player name")
    private val outlineColor: Color by ColorSetting("Outline Color", Color.PURPLE, true, description = "Color of the player outline")
    val nameColor: Color by ColorSetting("Name Color", Color.PINK, true, description = "Color of the name highlight")

    private val renderNameTags: Boolean by BooleanSetting("Disable name tags", true, description = "Disables the name tags of the mobs")

    private val removePerks: Boolean by BooleanSetting("Remove Perks", false, description = "Removes some perks from the perk menu")
    private val renderStun: Boolean by BooleanSetting("Render Stun", false, description = "Renders the stun perk")

    private val suppliesWaypoints: Boolean by BooleanSetting("Supplies Waypoints", true, description = "Renders the supply waypoints")
    val supplyWaypointColor: Color by ColorSetting("Supply Waypoint Color", Color.YELLOW, true, description = "Color of the supply waypoints")

    private val pearlWaypoints: Boolean by BooleanSetting("Pearl Waypoints", true, description = "Renders the pearl waypoints")
    val pearlBox: Double by NumberSetting("pearlBox", 1.0, 0.5, 1.5, description = "Size of the pearl box")

    private val buildHelperDraw: Boolean by BooleanSetting("Build Helper Draw", false, description = "Draws the build helper")
    private val buildHud: HudElement by HudSetting("Build helper", 10f, 10f, 1f, true) {
        if (it) {
            text("§6Build §f50%", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR)
            text("§6Builders §f2", 1f, 24f, Color.WHITE, 12f, OdinFont.REGULAR)

            getTextWidth("4Build 50%", 12f) + 2f to 34f
        } else {
            if (phase != 2) return@HudSetting 0f to 0f
            text("§6Build §f$build", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR)
            text("§6Builders §f$builders", 1f,  18f, Color.WHITE, 12f, OdinFont.REGULAR)

            getTextWidth("4Build 50%", 12f) + 2f to 34f
        }
    }

    private val notifyFresh: Boolean by BooleanSetting("Notify Fresh", true, description = "Notifies your party when you get fresh tools")
    private val highlightFresh: Boolean by BooleanSetting("Highlight Fresh", true, description = "Highlights fresh tools users")
    val highlightFreshColor: Color by ColorSetting("Highlight Fresh Color", Color.YELLOW, true)
    private val hud: HudElement by HudSetting("Fresh tools timer", 10f, 10f, 1f, true) {
        if (it) {
            text("§4Fresh Tools§f: 1000ms", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR)
            getTextWidth("Fresh Tools: 1000ms", 12f) + 2f to 16f
        } else {
            val player = kuudraTeammates.find { it.playerName == mc.thePlayer.name } ?: return@HudSetting 0f to 0f

            if (player.eatFresh)
                text("§4Fresh Tools§f: ${10000L - (System.currentTimeMillis() - player.eatFreshTime)}ms", 1f, 9f, Color.WHITE,12f, OdinFont.REGULAR)

            getTextWidth("Fresh Tools: 1000ms", 12f) + 2f to 12f
        }
    }

    private val vanqNotifier by BooleanSetting("Vanq Notifier", false, description = "Notifies you when you gets vanquisher")
    private val ac: Boolean by BooleanSetting("All Chat", false, description = "Sends the message to all chat")
    private val pc: Boolean by BooleanSetting("Party Chat", true, description = "Sends the message to party chat")

    private val kuudraHighlight: Boolean by BooleanSetting("Kuudra Highlight", true, description = "Highlights kuudra")
    val kuudraColor: Color by ColorSetting("Kuudra Color", Color.RED, true, description = "Color of the kuudra highlight")

    private var builders = ""
    private var build = ""
    var phase = 0

    init {

        onWorldLoad {
            phase = 0
            kuudraTeammates = ArrayList()
            supplies = BooleanArray(6) { true }
            giantZombies = mutableListOf()
            kuudraEntity = null
        }

        onMessage(Regex("^Party > ?(?:\\[.+])? (.{0,16}): FRESH")) {
            val (playerName) = Regex("^Party > ?(?:\\[.+])? (.{0,16}): FRESH").find(it)?.destructured ?: return@onMessage
            if (highlightFresh) return@onMessage
            modMessage("$playerName has fresh tools")
            kuudraTeammates.forEach { kuudraPlayer ->
                if (kuudraPlayer.playerName !== playerName) return@forEach
                kuudraPlayer.eatFresh = true
                runIn(200) {
                    modMessage("${kuudraPlayer.playerName} Fresh tools has expired")
                    kuudraPlayer.eatFresh = false
                }
            }
        }

        execute(1000) {
            val entities = mc.theWorld.loadedEntityList

            giantZombies = entities.filter { it is EntityGiantZombie && it.heldItem.toString() == "1xitem.skull@3" } as MutableList<EntityGiantZombie>

            entities.forEach { entity -> if (entity is EntityMagmaCube && entity.slimeSize == 30)  kuudraEntity = entity }
        }
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val message = event.message

        when (message)
        {
            "[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!" -> {
                phase = 1

                kuudraTeammates = mc.theWorld.getLoadedEntityList().filter { it is EntityOtherPlayerMP  && it.getPing() == 1 && !it.isInvisible }
                    .map { KuudraUtils.kuudraPlayer((it as EntityOtherPlayerMP).name, false, 0, it) } as ArrayList<KuudraUtils.kuudraPlayer>
                kuudraTeammates.add(KuudraUtils.kuudraPlayer(mc.thePlayer.name, false, 0, mc.thePlayer))
            }

            "[NPC] Elle: OMG! Great work collecting my supplies!" -> phase = 2

            "[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!" -> phase = 3

            "[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!" -> phase = 4

            "Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!" -> {
                if (!notifyFresh) return
                val teammate = kuudraTeammates.find { it.playerName == mc.thePlayer.name } ?: return
                teammate.eatFresh = true
                teammate.eatFreshTime = System.currentTimeMillis()
                modMessage("Fresh tools has been activated")
                runIn(200) {
                    modMessage("Fresh tools has expired")
                    teammate.eatFresh = false
                }
                partyMessage("FRESH")
            }

            "A Vanquisher is spawning nearby!" -> {
                if (!vanqNotifier) return
                modMessage("Vanquisher has spawned!")
                alert("§5Vanquisher has spawned!")

                if (ac) allMessage("x: ${posX.floor()}, y: ${posY.floor()}, z: ${posZ.floor()}")
                if (pc) partyMessage("x: ${posX.floor()}, y: ${posY.floor()}, z: ${posZ.floor()}")
            }
        }
    }

    @SubscribeEvent
    fun handleNames(event: RenderLivingEvent.Pre<*>) {
        if (renderNameTags) cancelArmorStandEvent(event.entity)
        if (highlightName && LocationUtils.currentArea == "Kuudra") renderTeammatesNames(event.entity)
    }

    @SubscribeEvent
    fun joinWorldEvent(event: EntityJoinWorldEvent) {
        handleArmorStand(event)
        if (event.entity is EntityOtherPlayerMP && event.entity.getPing() == 1 && !event.entity.isInvisible) {
            kuudraTeammates.add(KuudraUtils.kuudraPlayer((event.entity as EntityOtherPlayerMP).name, false, 0, event.entity as EntityOtherPlayerMP))
        }
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (event.entity == mc.thePlayer || !playerOutline || LocationUtils.currentArea != "Kuudra") return
        val teammate = kuudraTeammates.find { it.entity == event.entity } ?: return

        OutlineUtils.outlineEntity(event, 5f, if (teammate.eatFresh) highlightFreshColor else outlineColor, true)
    }
    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (pearlWaypoints && phase == 1) renderPearlBoxes()
        if (suppliesWaypoints && phase == 1) renderGiantZombies()
        if (kuudraHighlight) highlightKuudra()
        if (phase == 2) renderBuildHelper()
    }

    private fun renderBuildHelper() {
        mc.theWorld.loadedEntityList.forEach {
            if (it.name.contains("Lv") || it.toString().contains("name=Armor Stand")) return@forEach
            val name = it.name.noControlCodes
            if (name.contains("Building Progress")) {
                builders = name.substring(name.indexOf("(") + 1, name.indexOf("(") +2)
                if (buildHelperDraw) RenderUtils.drawStringInWorld("$builders builders", it.positionVector, Color.WHITE.rgba, false, false, scale = 0.5f)
                val regex = Regex("\\D")
                build = name.substring(0, name.indexOf("%")).replace(regex, "")
                if (buildHelperDraw) RenderUtils.drawStringInWorld("$build% built", it.positionVector.addVec(y = 7), Color.WHITE.rgba, false, false, scale = 0.5f)
            } else if (name.contains("PROGRESS: ") && name.contains("%")) {
                if (buildHelperDraw) RenderUtils.renderCustomBeacon(it.name, it.posX, it.posY, it.posZ, Color(0, 255, 0), true)
            }
        }
    }
    @SubscribeEvent
    fun renderSlot(event: DrawSlotEvent) {
        if (event.gui !is GuiChest || event.gui.inventorySlots !is ContainerChest || !removePerks || (event.gui.inventorySlots as ContainerChest).name != "Perk Menu") return
        val slotName = event.slot.stack?.displayName.noControlCodes
        if (slotName.containsOneOf("Steady Hands", "Bomberman") || slotName == "Elle's Lava Rod" || slotName == "Elle's Pickaxe" ||
            slotName == "Auto Revive" || (!renderStun && slotName.containsOneOf("Mining Frenzy", "Human Cannonball")))
            event.isCanceled = true
    }
    @SubscribeEvent
    fun guiMouseClick(event: GuiClickEvent) {
        if (event.gui !is GuiChest || event.gui.inventorySlots !is ContainerChest || !removePerks || (event.gui.inventorySlots as ContainerChest).name != "Perk Menu") return
        val slot = event.gui.slotUnderMouse?.stack?.displayName.noControlCodes
        if (slot.containsOneOf("Steady Hands", "Bomberman") || slot == "Elle's Lava Rod" || slot == "Elle's Pickaxe" ||
            slot == "Auto Revive" || (!renderStun && slot.containsOneOf("Mining Frenzy", "Human Cannonball")))
            event.isCanceled = true
    }
}