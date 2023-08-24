package me.odinclient.features.impl.render

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.EntityUtils.drawBlockBox
import me.odinclient.utils.skyblock.LocationUtils.inSkyblock
import me.odinclient.utils.skyblock.ScoreboardUtils.cleanSB
import me.odinclient.utils.skyblock.ScoreboardUtils.sidebarLines
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.BlockStainedGlassPane
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object GemstoneESP : Module(
    "Gemstone ESP",
    category = Category.RENDER
) {

    private val gemstoneESPRadius: Int by NumberSetting("Gemstone ESP Radius", 20, 1, 50, 1)
    private val gemstoneESPTime: Int by NumberSetting("Gemstone ESP Time", 250, 1, 1000, 1, description = "Time in ms between scan cycles.")
    private val gemstoneAmber: Boolean by BooleanSetting("Amber")
    private val gemstoneAmethyst: Boolean by BooleanSetting("Amethyst")
    private val gemstoneJade: Boolean by BooleanSetting("Jade")
    private val gemstoneJasper: Boolean by BooleanSetting("Jasper")
    private val gemstoneRuby: Boolean by BooleanSetting("Ruby")
    private val gemstoneSapphire: Boolean by BooleanSetting("Sapphire")
    private val gemstoneTopaz: Boolean by BooleanSetting("Topaz")

    private val gemstoneList = HashMap<BlockPos, Color>()
    private var lastUpdate: Long = 0
    private val locations = listOf(
        "Jungle",
        "Jungle Temple",
        "Mithril Deposits",
        "Mines of Divan",
        "Goblin Holdout",
        "Goblin Queen's Den",
        "Precursor Remnants",
        "Lost Precursor City",
        "Crystal Nucleus",
        "Magma Fields",
        "Khazad-dûm",
        "Fairy Grotto",
        "Dragon's Lair"
    )
    private var thread: Thread? = null

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !isCrystalHollow()) return
        if (thread?.isAlive == true || lastUpdate + gemstoneESPTime > System.currentTimeMillis()) return
        thread = Thread({
            val blockList = HashMap<BlockPos, Color>()
            val player = mc.thePlayer.position
            val radius = gemstoneESPRadius
            val vec3i = Vec3i(radius, radius, radius)
            BlockPos.getAllInBox(player.add(vec3i), player.subtract(vec3i)).forEach {
                val blockState = mc.theWorld.getBlockState(it)
                val dyeColor = when (blockState.block) {
                    Blocks.stained_glass -> blockState.getValue(BlockStainedGlass.COLOR)
                    Blocks.stained_glass_pane -> blockState.getValue(BlockStainedGlassPane.COLOR)
                    else -> return@forEach
                }
                val color = getColor(dyeColor) ?: return@forEach
                blockList[it] = color
            }
            synchronized(gemstoneList) {
                gemstoneList.clear()
                gemstoneList.putAll(blockList)
            }
            lastUpdate = System.currentTimeMillis()
        }, "Gemstone ESP")
        thread!!.start()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!isCrystalHollow()) return
        synchronized(gemstoneList) {
            gemstoneList.forEach { (blockPos, color) ->
                drawBlockBox(blockPos, color, outline = true, fill = false, event.partialTicks)
            }
        }
    }

    private fun getColor(dyeColor: EnumDyeColor): Color? {
        return when (dyeColor) {
            EnumDyeColor.ORANGE -> if (gemstoneAmber) Color(237, 139, 35) else null
            EnumDyeColor.PURPLE -> if (gemstoneAmethyst) Color(137, 0, 201) else null
            EnumDyeColor.LIME -> if (gemstoneJade) Color(157, 249, 32) else null
            EnumDyeColor.MAGENTA -> if (gemstoneJasper) Color(214, 15, 150) else null
            EnumDyeColor.RED -> if (gemstoneRuby) Color(188, 3, 29) else null
            EnumDyeColor.LIGHT_BLUE -> if (gemstoneSapphire) Color(60, 121, 224) else null
            EnumDyeColor.YELLOW -> if (gemstoneTopaz) Color(249, 215, 36) else null
            else -> null
        }
    }

    private fun isCrystalHollow(): Boolean {
        return inSkyblock && sidebarLines.any { s -> locations.any { cleanSB(s).contains(it) } }
    }
}
