package me.odinclient.features.impl.skyblock

import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.features.Module
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Colors
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
    description = "Automates the Chocolate Factory."
) {
    private val clickFactory by BooleanSetting("Click Factory", false, desc = "Click the cookie in the Chocolate Factory menu.")
    private val autoUpgrade by BooleanSetting("Auto Upgrade", false, desc = "Automatically upgrade the worker.")
    private val delay by NumberSetting("Delay", 150L, 50, 1500, 5, unit = "ms", desc = "Delay between actions.")
    private val upgradeDelay by NumberSetting("Upgrade delay", 500L, 300, 2000, 100, unit = "ms", desc = "Delay between upgrades.")
    private val claimStray by BooleanSetting("Claim Strays", false, desc = "Claim stray rabbits in the Chocolate Factory menu.")
    private val cancelSound by BooleanSetting("Cancel Sound", false, desc = "Cancels the eating sound in the Chocolate Factory.")
    private val upgradeMessage by BooleanSetting("Odin Upgrade Message", false, desc = "Prints a message when upgrading.")
    private val eggEsp by BooleanSetting("Egg ESP", false, desc = "Shows the location of the egg.")
    private var chocolate = 0L

    private val indexToName = mapOf(28 to "Bro", 29 to "Cousin", 30 to "Sis", 31 to "Daddy", 32 to "Granny", 33 to "Uncle", 34 to "Dog")
    private val possibleLocations = arrayOf(
        Island.SpiderDen, Island.CrimsonIsle, Island.TheEnd, Island.GoldMine, Island.DeepCaverns, Island.DwarvenMines,
        Island.CrystalHollows, Island.FarmingIsland, Island.ThePark, Island.DungeonHub, Island.Hub, Island.BackwaterBayou
    )

    init {
        onWorldLoad { currentDetectedEggs.clear() }
        execute(delay = { delay }) {
            if (!isInChocolateFactory()) return@execute

            if (clickFactory) windowClick(13, ClickType.Right)
            
            if (!claimStray) return@execute
            val found = (mc.thePlayer?.openContainer as? ContainerChest)?.inventorySlots?.find { it.stack?.displayName?.containsOneOf("CLICK ME!", "Golden Rabbit") == true } ?: return@execute
            windowClick(found.slotNumber, ClickType.Left)
        }

        execute(delay = { upgradeDelay }) {
            val container = mc.thePlayer.openContainer as? ContainerChest ?: return@execute
            if (container.name != "Chocolate Factory") return@execute

            chocolate =  container.getSlot(13)?.stack?.unformattedName?.replace(Regex("\\D"), "")?.toLongOrNull() ?: 0L

            findWorker(container)
            if (!found) return@execute
            if (chocolate > bestCost && autoUpgrade) {
                windowClick(bestWorker, ClickType.Middle)
                if (upgradeMessage) modMessage("Trying to upgrade: Rabbit " + indexToName[bestWorker] + " with " + bestCost + " chocolate.")
            }
        }

        execute(delay = { 3000 }) {
            if (eggEsp && LocationUtils.currentArea in possibleLocations && currentDetectedEggs.size < 6) scanForEggs()
        }

        onMessage(Regex(".*(A|found|collected).+Chocolate (Breakfast|Lunch|Dinner|Brunch|Déjeuner|Supper).*")) {
            if (it.groupValues.getOrNull(1).equalsOneOf("found", "collected"))
                currentDetectedEggs.minByOrNull { egg -> egg.entity.getDistanceToEntity(mc.thePlayer) }?.isFound = true
        }
    }

    private var bestWorker = 28
    private var bestCost = 0
    private var found = false

    private fun findWorker(container: Container) {
        val items = container.inventory ?: return
        val workers = mutableListOf<List<String?>>()
        repeat(7) { workers.add(items[it + 28]?.lore ?: return) }
        found = false
        var maxValue = 0
        repeat(7) {
            val worker = workers[it]
            if (worker.contains("climbed as far")) return@repeat
            val index = worker.indexOfFirst { it?.contains("Cost") == true }.takeIf { workerIndex -> workerIndex != -1 } ?: return@repeat
            val cost = worker[index + 1]?.noControlCodes?.replace(Regex("\\D"), "")?.toIntOrNull() ?: return@repeat
            val value = cost / (it + 1).toFloat()
            if (value < maxValue || !found) {
                bestWorker = 28 + it
                maxValue = value.toInt()
                bestCost = cost
                found = true
            }
        }
    }

    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundEvent) {
        if (cancelSound && event.name == "random.eat" && isInChocolateFactory()) event.result = null
    }

    private var currentDetectedEggs = mutableListOf<Egg>()

    private enum class ChocolateEggs(
        val texture: String, val type: String, val color: Color
    ) {
        Breakfast(BunnyEggTextures.BREAKFAST_EGG_TEXTURE, "§6Breakfast Egg", Colors.MINECRAFT_GOLD),
        Lunch(BunnyEggTextures.LUNCH_EGG_TEXTURE, "§9Lunch Egg ", Colors.MINECRAFT_BLUE),
        Dinner(BunnyEggTextures.DINNER_EGG_TEXTURE, "§aDinner Egg", Colors.MINECRAFT_GREEN),
        Brunch(BunnyEggTextures.BREAKFAST_EGG_TEXTURE, "§6Brunch Egg", Colors.MINECRAFT_GOLD),
        Dejeuner(BunnyEggTextures.LUNCH_EGG_TEXTURE, "§9Déjeuner Egg ", Colors.MINECRAFT_BLUE),
        Supper(BunnyEggTextures.DINNER_EGG_TEXTURE, "§aSupper Egg", Colors.MINECRAFT_GREEN),
    }

    private data class Egg(val entity: EntityArmorStand, val renderName: String, val color: Color, var isFound: Boolean = false)

    private fun scanForEggs() {
        mc.theWorld?.loadedEntityList?.forEach { entity ->
            if (entity !is EntityArmorStand) return@forEach
            val eggType = ChocolateEggs.entries.find { it.texture == getSkullValue(entity) } ?: return@forEach
            currentDetectedEggs.add(Egg(entity, eggType.type, eggType.color))
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!eggEsp) return
        currentDetectedEggs.forEach { egg ->
            if (egg.isFound) return@forEach
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
