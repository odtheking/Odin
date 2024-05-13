package me.odinclient.features.impl.skyblock

import me.odinmain.events.impl.GuiEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.BunnyEggTextures
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
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
    "Chocolate Factory",
    description = "Automatically clicks the cookie in the Chocolate Factory menu.",
    category = Category.SKYBLOCK
) {
    private val clickFactory: Boolean by BooleanSetting(
        "Click Factory",
        false,
        description = "Click the cookie in the Chocolate Factory menu."
    )
    private val autoUpgrade: Boolean by BooleanSetting(
        "Auto Upgrade",
        false,
        description = "Automatically upgrade the worker."
    )
    private val delay: Long by NumberSetting("Delay", 150, 50, 300, 5)
    private val upgradeDelay: Long by NumberSetting("Upgrade delay", 500, 300, 2000, 100)
    private val cancelSound: Boolean by BooleanSetting("Cancel Sound")
    private val upgradeMessage: Boolean by BooleanSetting(
        "Odin Upgrade Message",
        false,
        description = "Prints a message when upgrading."
    )
    private val eggEsp: Boolean by BooleanSetting("Egg ESP", false, description = "Shows the location of the egg.")
    private var chocolate = 0
    private var chocoProduction = 0f

    val indexToName = mapOf(29 to "Bro", 30 to "Cousin", 31 to "Sis", 32 to "Daddy", 33 to "Granny")
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
        onWorldLoad { currentChocolateEggs = arrayOfNulls(3) }
        execute(delay = { delay }) {
            val container = mc.thePlayer?.openContainer as? ContainerChest ?: return@execute
            if (container.name != "Chocolate Factory") return@execute

            if (clickFactory) windowClick(13, 1, 0)
        }

        execute(delay = { upgradeDelay }) {
            val container = mc.thePlayer.openContainer as? ContainerChest ?: return@execute
            if (container.name != "Chocolate Factory") return@execute

            val choco = container.getSlot(13)?.stack ?: return@execute

            chocolate = choco.displayName.noControlCodes.replace(Regex("\\D"), "").toInt()
            chocoProduction =
                choco.lore.find { it.endsWith("§8per second") }?.noControlCodes?.replace(",", "")?.toFloatOrNull() ?: 0f

            findWorker(container)
            if (!found) return@execute
            if (chocolate > bestCost && autoUpgrade) {
                windowClick(bestWorker, 2, 3)
                if (upgradeMessage) modMessage("Trying to upgrade: Rabbit " + indexToName[bestWorker] + " with " + bestCost + " chocolate.")
            }
        }

        execute(delay = { 3000 }) {
            if (eggEsp && possibleLocations.contains(LocationUtils.currentArea)) scanForEggs()
        }
    }

    private var bestWorker = 29
    private var bestCost = 0
    private var found = false

    private fun findWorker(container: Container) {
        val items = container.inventory ?: return
        val workers = mutableListOf<List<String?>>()
        for (i in 29 until 34) {
            workers.add(items[i]?.lore ?: return)
        }
        found = false
        var maxValue = 0;
        for (i in 0 until 5) {
            val worker = workers[i]
            if (worker.contains("climbed as far")) continue
            val index = worker.indexOfFirst { it?.contains("Cost") == true }.takeIf { it != -1 } ?: continue
            val cost = worker[index + 1]?.noControlCodes?.replace(Regex("\\D"), "")?.toIntOrNull() ?: continue
            val value = cost / (i + 1).toFloat()
            if (value < maxValue || !found) {
                bestWorker = 29 + i
                maxValue = value.toInt()
                bestCost = cost
                found = true
            }
        }
    }

    @SubscribeEvent
    fun guiLoad(event: GuiEvent.GuiLoadedEvent) {
        if (event.name == "Chocolate Factory") findWorker(event.gui)
    }

    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundEvent) {
        if (!cancelSound) return
        if (event.name == "random.eat") event.result = null // This should cancel the sound event
    }


    private var currentChocolateEggs = arrayOfNulls<Egg>(3)

    private enum class ChocolateEggs(
        val texture: String, val type: String, val color: Color, val index: Int
    ) {
        Breakfast(BunnyEggTextures.breakfastEggTexture, "§6Breakfast Egg", Color.ORANGE, 0),
        Lunch(BunnyEggTextures.lunchEggTexture, "§9Lunch Egg ", Color.BLUE, 1),
        Dinner(BunnyEggTextures.dinnerEggTexture, "§aDinner Egg", Color.GREEN, 2),
    }

    data class Egg(val entity: EntityArmorStand, val renderName: String, val color: Color)

    private fun getEggType(entity: EntityArmorStand): ChocolateEggs? {
        val texture = getSkullValue(entity)
        return ChocolateEggs.entries.find { it.texture == texture }
    }

    fun scanForEggs() {
        for (entity in mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()) {
            val egg = getEggType(entity) ?: continue
            currentChocolateEggs[egg.index] = Egg(entity, egg.type, egg.color)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (currentChocolateEggs.isEmpty()) return
        for (egg in currentChocolateEggs) {
            if (egg == null) continue
            val location = Vec3(egg.entity.posX - 0.5, egg.entity.posY + 1.47, egg.entity.posZ - 0.5)
            Renderer.drawCustomBeacon(egg.renderName, location, egg.color, increase = true, beacon = false)
        }
    }
}