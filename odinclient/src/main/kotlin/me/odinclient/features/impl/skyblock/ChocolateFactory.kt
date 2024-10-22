package me.odinclient.features.impl.skyblock

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ChocolateFactory : Module(
    name = "Chocolate Factory",
    description = "Automates the Chocolate Factory.",
    category = Category.SKYBLOCK
) {
    private val clickFactory by BooleanSetting("Click Factory", false, description = "Click the cookie in the Chocolate Factory menu.")
    private val autoUpgrade by BooleanSetting("Auto Upgrade", false, description = "Automatically upgrade the worker.")
    private val delay by NumberSetting("Delay", 150L, 50, 300, 5, unit = "ms", description = "Delay between actions.")
    private val upgradeDelay by NumberSetting("Upgrade delay", 500L, 300, 2000, 100, unit = "ms", description = "Delay between upgrades.")
    private val claimStray by BooleanSetting("Claim Strays", false, description = "Claim stray rabbits in the Chocolate Factory menu.")
    private val cancelSound by BooleanSetting("Cancel Sound", false, description = "Cancels the eating sound in the Chocolate Factory.")
    private val upgradeMessage by BooleanSetting("Odin Upgrade Message", false, description = "Prints a message when upgrading.")
    private val eggEsp by BooleanSetting("Egg ESP", false, description = "Shows the location of the egg.")
    private var chocolate = 0L

    private val indexToName = mapOf(28 to "Bro", 29 to "Cousin", 30 to "Sis", 31 to "Daddy", 32 to "Granny", 33 to "Uncle", 34 to "Dog")
    private val possibleLocations = arrayOf(
        Island.SpiderDen,
        Island.CrimsonIsle,
        Island.TheEnd,
        Island.GoldMine,
        Island.DeepCaverns,
        Island.DwarvenMines,
        Island.CrystalHollows,
        Island.FarmingIsland,
        Island.ThePark,
        Island.DungeonHub,
        Island.Hub
    )

    init {
        onWorldLoad { currentDetectedEggs = arrayOfNulls(3) }
        execute(delay = { delay }) {
            if (!isInChocolateFactory()) return@execute

            if (clickFactory) windowClick(13, PlayerUtils.ClickType.Right)
            
            if (!claimStray) return@execute
            val found = (mc.thePlayer?.openContainer as? ContainerChest)?.inventorySlots?.find { it.stack?.displayName?.contains("CLICK ME!") == true } ?: return@execute
            windowClick(found.slotNumber, PlayerUtils.ClickType.Left)
        }

        execute(delay = { upgradeDelay }) {
            val container = mc.thePlayer.openContainer as? ContainerChest ?: return@execute
            if (container.name != "Chocolate Factory") return@execute
            val choco = container.getSlot(13)?.stack ?: return@execute

            chocolate = choco.unformattedName.replace(Regex("\\D"), "").toLongOrNull() ?: 0L

            findWorker(container)
            if (!found) return@execute
            if (chocolate > bestCost && autoUpgrade) {
                windowClick(bestWorker, PlayerUtils.ClickType.Middle)
                if (upgradeMessage) modMessage("Trying to upgrade: Rabbit " + indexToName[bestWorker] + " with " + bestCost + " chocolate.")
            }
        }

        execute(delay = { 3000 }) {
            if (!eggEsp) currentDetectedEggs = arrayOfNulls(3)
            if (eggEsp && possibleLocations.contains(LocationUtils.currentArea) && currentDetectedEggs.filterNotNull().size < 3) scanForEggs()
        }

        onMessage(Regex(".*(A|found|collected).+Chocolate (Lunch|Dinner|Breakfast).*")){ it ->
            if(!eggEsp) return@onMessage
            val match = Regex(".*(A|found|collected).+Chocolate (Lunch|Dinner|Breakfast).*").find(it) ?: return@onMessage
            val egg = ChocolateEggs.entries.find { it.type.contains(match.groupValues[2]) } ?: return@onMessage
            when (match.groupValues[1]) {
                "A" -> currentDetectedEggs[egg.index] = null
                "found", "collected" -> currentDetectedEggs[egg.index]?.isFound = true
            }
        }
    }

    private var bestWorker = 28
    private var bestCost = 0
    private var found = false

    private fun findWorker(container: Container) {
        val items = container.inventory ?: return
        val workers = mutableListOf<List<String?>>()
        for (i in 28 until 35) {
            workers.add(items[i]?.lore ?: return)
        }
        found = false
        var maxValue = 0
        for (i in 0 until 7) {
            val worker = workers[i]
            if (worker.contains("climbed as far")) continue
            val index = worker.indexOfFirst { it?.contains("Cost") == true }.takeIf { it != -1 } ?: continue
            val cost = worker[index + 1]?.noControlCodes?.replace(Regex("\\D"), "")?.toIntOrNull() ?: continue
            val value = cost / (i + 1).toFloat()
            if (value < maxValue || !found) {
                bestWorker = 28 + i
                maxValue = value.toInt()
                bestCost = cost
                found = true
            }
        }
    }

    @SubscribeEvent
    fun guiLoad(event: GuiEvent.GuiLoadedEvent) {
        if (isInChocolateFactory()) findWorker(event.gui)
    }

    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundEvent) {
        if (!cancelSound || event.name != "random.eat" || !isInChocolateFactory()) return
        event.result = null // This should cancel the sound event
    }

    private var currentDetectedEggs = arrayOfNulls<Egg>(3)

    private enum class ChocolateEggs(
        val texture: String, val type: String, val color: Color, val index: Int
    ) {
        Breakfast(BunnyEggTextures.BREAKFAST_EGG_TEXTURE, "§6Breakfast Egg", Color.ORANGE, 0),
        Lunch(BunnyEggTextures.LUNCH_EGG_TEXTURE, "§9Lunch Egg ", Color.BLUE, 1),
        Dinner(BunnyEggTextures.DINNER_EGG_TEXTURE, "§aDinner Egg", Color.GREEN, 2),
    }

    private data class Egg(val entity: EntityArmorStand, val renderName: String, val color: Color, var isFound: Boolean = false)

    private fun getEggType(entity: EntityArmorStand): ChocolateEggs? {
        val texture = getSkullValue(entity)
        return ChocolateEggs.entries.find { it.texture == texture }
    }

    private fun scanForEggs() {
        mc.theWorld?.loadedEntityList?.forEach { entity ->
            if (entity !is EntityArmorStand) return@forEach
            val eggType = getEggType(entity) ?: return@forEach
            currentDetectedEggs[eggType.index] = currentDetectedEggs[eggType.index] ?: Egg(entity, eggType.type, eggType.color)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        currentDetectedEggs.forEach { egg ->
            if (egg == null || egg.isFound) return@forEach
            Renderer.drawCustomBeacon(egg.renderName, Vec3(egg.entity.posX - 0.5, egg.entity.posY + 1.47, egg.entity.posZ - 0.5), egg.color, increase = true, beacon = false)
        }
    }

    private fun isInChocolateFactory(): Boolean {
        return mc.thePlayer?.openContainer is ContainerChest && mc.thePlayer?.openContainer?.name == "Chocolate Factory"
    }

    private data object BunnyEggTextures {
        const val DINNER_EGG_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY0OTcwMSwKICAicHJvZmlsZUlkIiA6ICI3NGEwMzQxNWY1OTI0ZTA4YjMyMGM2MmU1NGE3ZjJhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZXp6aXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlMzYxNjU4MTlmZDI4NTBmOTg1NTJlZGNkNzYzZmY5ODYzMTMxMTkyODNjMTI2YWNlMGM0Y2M0OTVlNzZhOCIKICAgIH0KICB9Cn0"
        const val LUNCH_EGG_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjU2ODExMiwKICAicHJvZmlsZUlkIiA6ICI3NzUwYzFhNTM5M2Q0ZWQ0Yjc2NmQ4ZGUwOWY4MjU0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWVkcmVsIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdhZTZkMmQzMWQ4MTY3YmNhZjk1MjkzYjY4YTRhY2Q4NzJkNjZlNzUxZGI1YTM0ZjJjYmM2NzY2YTAzNTZkMGEiCiAgICB9CiAgfQp9"
        const val BREAKFAST_EGG_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY3MzE0OSwKICAicHJvZmlsZUlkIiA6ICJiN2I4ZTlhZjEwZGE0NjFmOTY2YTQxM2RmOWJiM2U4OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbmFiYW5hbmFZZzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ5MzMzZDg1YjhhMzE1ZDAzMzZlYjJkZjM3ZDhhNzE0Y2EyNGM1MWI4YzYwNzRmMWI1YjkyN2RlYjUxNmMyNCIKICAgIH0KICB9Cn0"
    }
}
