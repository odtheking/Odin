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
    "Chocolate Factory",
    description = "Automates the Chocolate Factory.",
    category = Category.SKYBLOCK
) {
    private val clickFactory: Boolean by BooleanSetting("Click Factory", false, description = "Click the cookie in the Chocolate Factory menu.")
    private val autoUpgrade: Boolean by BooleanSetting("Auto Upgrade", false, description = "Automatically upgrade the worker.")
    private val delay: Long by NumberSetting("Delay", 150, 50, 300, 5)
    private val upgradeDelay: Long by NumberSetting("Upgrade delay", 500, 300, 2000, 100)
    private val cancelSound: Boolean by BooleanSetting("Cancel Sound")
    private val upgradeMessage: Boolean by BooleanSetting("Odin Upgrade Message", false, description = "Prints a message when upgrading.")
    private val eggEsp: Boolean by BooleanSetting("Egg ESP", false, description = "Shows the location of the egg.")
    private var chocolate = 0

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
        }

        execute(delay = { upgradeDelay }) {
            val container = mc.thePlayer.openContainer as? ContainerChest ?: return@execute
            if (container.name != "Chocolate Factory") return@execute

            val choco = container.getSlot(13)?.stack ?: return@execute

            chocolate = choco.displayName.noControlCodes.replace(Regex("\\D"), "").toIntOrNull() ?: 0

            findWorker(container)
            if (!found) return@execute
            if (chocolate > bestCost && autoUpgrade) {
                windowClick(bestWorker, PlayerUtils.ClickType.Middle)
                if (upgradeMessage) modMessage("Trying to upgrade: Rabbit " + indexToName[bestWorker] + " with " + bestCost + " chocolate.")
            }
        }

        execute(delay = { 3000 }) {
            if(!eggEsp) currentDetectedEggs = arrayOfNulls(3)
            if (eggEsp && possibleLocations.contains(LocationUtils.currentArea) && currentDetectedEggs.filterNotNull().size < 3) scanForEggs()
        }

        onMessage(Regex(".*(A|found|collected).+Chocolate (Lunch|Dinner|Breakfast).*")){ it ->
            if(!eggEsp) return@onMessage
            val match = Regex(".*(A|found|collected).+Chocolate (Lunch|Dinner|Breakfast).*").find(it) ?: return@onMessage
            val egg = ChocolateEggs.entries.find { it.type.contains(match.groupValues[2]) } ?: return@onMessage
            when(match.groupValues[1]) {
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
        Breakfast(BunnyEggTextures.breakfastEggTexture, "§6Breakfast Egg", Color.ORANGE, 0),
        Lunch(BunnyEggTextures.lunchEggTexture, "§9Lunch Egg ", Color.BLUE, 1),
        Dinner(BunnyEggTextures.dinnerEggTexture, "§aDinner Egg", Color.GREEN, 2),
    }

    data class Egg(val entity: EntityArmorStand, val renderName: String, val color: Color, var isFound: Boolean = false)

    private fun getEggType(entity: EntityArmorStand): ChocolateEggs? {
        val texture = getSkullValue(entity)
        return ChocolateEggs.entries.find { it.texture == texture }
    }

    private fun scanForEggs() {
        mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>().forEach { entity ->
            val eggType = getEggType(entity) ?: return@forEach
            currentDetectedEggs[eggType.index] = currentDetectedEggs[eggType.index] ?: Egg(entity, eggType.type, eggType.color)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        currentDetectedEggs.filterNotNull().filter { !it.isFound }.forEach { egg ->
            val eggLocation = Vec3(egg.entity.posX - 0.5, egg.entity.posY + 1.47, egg.entity.posZ - 0.5)
            Renderer.drawCustomBeacon(egg.renderName, eggLocation, egg.color, increase = true, beacon = false)
        }
    }

    private fun isInChocolateFactory(): Boolean {
        return mc.thePlayer.openContainer is ContainerChest && mc.thePlayer.openContainer.name == "Chocolate Factory"
    }
}
