package me.odinmain.features.impl.render

import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.render.RenderUtils.tessellator
import me.odinmain.utils.render.RenderUtils.worldRenderer
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.getSkullValue
import me.odinmain.utils.skyblock.skullTexture
import me.odinmain.utils.ui.Colors
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.item.*
import net.minecraft.entity.monster.*
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.*
import net.minecraft.init.Items
import net.minecraft.network.play.server.S0EPacketSpawnObject
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.tileentity.TileEntityEndPortal
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object RenderOptimizer : Module(
    "Render Optimizer",
    desc = "Disables certain render function when they are not necessary."
) {
    private val fallingBlocks by BooleanSetting("Remove Falling Blocks", true, desc = "Removes falling blocks that are not necessary.")
    private val removeTentacles by BooleanSetting("Remove P5 Tentacles", true, desc = "Removes armorstands of tentacles which are not necessary.")
    private val hideHealerFairy by BooleanSetting("Hide Healer Fairy", true, desc = "Hides the healer fairy.")
    private val hideSoulWeaver by BooleanSetting("Hide Soul Weaver", true, desc = "Hides the soul weaver.")
    private val hideArcherBones by BooleanSetting("Hide Archer Bones", true, desc = "Hides the archer bones.")
    private val hide0HealthNames by BooleanSetting("Hide 0 Health", true, desc = "Hides the names of entities with 0 health.")
    private val hideWitherMinerName by BooleanSetting("Hide WitherMiner Name", true, desc = "Hides the wither miner name.")
    private val hideTerracottaName by BooleanSetting("Hide Terracota Name", true, desc = "Hides the terracota name.")
    private val hideNonStarredMobName by BooleanSetting("Hide Non-Starred Mob Name", true, desc = "Hides the non-starred mob name.")
    private val removePuzzleBlazeNames by BooleanSetting("Hide blazes names", false, desc = "Hides the blazes in the blaze puzzle room.")
    private val removePuzzleBlaze by BooleanSetting("Hide blazes", true, desc = "Removes the blaze in the blaze puzzle room.")

    private val showParticleOptions by DropdownSetting("Show Particles Options")
    private val removeExplosion by BooleanSetting("Remove Explosion", false, desc = "Removes explosion particles.").withDependency { showParticleOptions }
    private val hideParticles by BooleanSetting("Hide P5 Particles", true, desc = "Hides particles that are not necessary.").withDependency { showParticleOptions }
    private val hideHeartParticles by BooleanSetting("Hide Heart Particles", false, desc = "Hides heart particles.").withDependency { showParticleOptions }

    private const val TENTACLE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg1NzI3NzI0OSwKICAicHJvZmlsZUlkIiA6ICIxODA1Y2E2MmM0ZDI0M2NiOWQxYmY4YmM5N2E1YjgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJSdWxsZWQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdkODM2NzQ5MjZiODk3MTRlNmI1YTU1NDcwNTAxYzA0YjA2NmRkODdiZjZjMzM1Y2RkYzZlNjBhMWExYTVmNSIKICAgIH0KICB9Cn0="
    private const val HEALER_FAIRY_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTQ2MzA5MTA0NywKICAicHJvZmlsZUlkIiA6ICIyNjRkYzBlYjVlZGI0ZmI3OTgxNWIyZGY1NGY0OTgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJxdWludHVwbGV0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJlZWRjZmZjNmExMWEzODM0YTI4ODQ5Y2MzMTZhZjdhMjc1MmEzNzZkNTM2Y2Y4NDAzOWNmNzkxMDhiMTY3YWUiCiAgICB9CiAgfQp9"
    private const val SOUL_WEAVER_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NTk1ODAzNjI1NTMsInByb2ZpbGVJZCI6ImU3NmYwZDlhZjc4MjQyYzM5NDY2ZDY3MjE3MzBmNDUzIiwicHJvZmlsZU5hbWUiOiJLbGxscmFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yZjI0ZWQ2ODc1MzA0ZmE0YTFmMGM3ODViMmNiNmE2YTcyNTYzZTlmM2UyNGVhNTVlMTgxNzg0NTIxMTlhYTY2In19fQ=="
    private val dungeonMobSpawns = setOf("Lurker", "Dreadlord", "Souleater", "Zombie", "Skeleton", "Skeletor", "Sniper", "Super Archer", "Spider", "Fels", "Withermancer")

    private val potatoMode by BooleanSetting("Potato Mode (exp)", desc = "Potato mode for low-end computers.")
    private val showStarredNametags by BooleanSetting("Show Starred Nametags", desc = "Shows the nametags of starred mobs.").withDependency { potatoMode }
    private val editEntityColors by DropdownSetting("Entity Colors").withDependency { potatoMode }
    private val editShouldRender by DropdownSetting("Potato Entities").withDependency { potatoMode }

    private val playerColor by ColorSetting("Player", Colors.BLACK, desc = "").withDependency { potatoMode && editEntityColors }
    private val caveSpiderColor by ColorSetting("Cave Spider", Color(102, 0, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val spiderColor by ColorSetting("Spider", Color(102, 102, 102), desc = "").withDependency { potatoMode && editEntityColors }
    private val pigColor by ColorSetting("Pig", Color(255, 102, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val sheepColor by ColorSetting("Sheep", Colors.WHITE, desc = "").withDependency { potatoMode && editEntityColors }
    private val cowColor by ColorSetting("Cow", Color(0, 255, 0), desc = "").withDependency { potatoMode && editEntityColors }
    private val mooshroomColor by ColorSetting("Mooshroom", Colors.MINECRAFT_RED, desc = "").withDependency { potatoMode && editEntityColors }
    private val wolfColor by ColorSetting("Wolf", Color(128, 128, 128), desc = "").withDependency { potatoMode && editEntityColors }
    private val chickenColor by ColorSetting("Chicken", Colors.MINECRAFT_YELLOW, desc = "").withDependency { potatoMode && editEntityColors }
    private val ocelotColor by ColorSetting("Ocelot", Color(255, 204, 0), desc = "").withDependency { potatoMode && editEntityColors }
    private val rabbitColor by ColorSetting("Rabbit", Color(245, 245, 220), desc = "").withDependency { potatoMode && editEntityColors }
    private val silverfishColor by ColorSetting("Silverfish", Color(179, 179, 179), desc = "").withDependency { potatoMode && editEntityColors }
    private val endermiteColor by ColorSetting("Endermite", Color(138, 43, 226), desc = "").withDependency { potatoMode && editEntityColors }
    private val creeperColor by ColorSetting("Creeper", Color(0, 255, 0), desc = "").withDependency { potatoMode && editEntityColors }
    private val endermanColor by ColorSetting("Enderman", Color(51, 0, 102), desc = "").withDependency { potatoMode && editEntityColors }
    private val snowmanColor by ColorSetting("Snowman", Color(240, 240, 240), desc = "").withDependency { potatoMode && editEntityColors }
    private val skeletonColor by ColorSetting("Skeleton", Color(51, 51, 51), desc = "").withDependency { potatoMode && editEntityColors }
    private val witchColor by ColorSetting("Witch", Color(128, 0, 128), desc = "").withDependency { potatoMode && editEntityColors }
    private val blazeColor by ColorSetting("Blaze", Color(255, 102, 0), desc = "").withDependency { potatoMode && editEntityColors }
    private val pigZombieColor by ColorSetting("Pig Zombie", Color(255, 0, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val zombieColor by ColorSetting("Zombie", Color(0, 255, 0), desc = "").withDependency { potatoMode && editEntityColors }
    private val slimeColor by ColorSetting("Slime", Color(0, 255, 0), desc = "").withDependency { potatoMode && editEntityColors }
    private val magmaCubeColor by ColorSetting("Magma Cube", Color(255, 51, 51), desc = "").withDependency { potatoMode && editEntityColors }
    private val ghastColor by ColorSetting("Ghast", Color(255, 0, 102), desc = "").withDependency { potatoMode && editEntityColors }
    private val squidColor by ColorSetting("Squid", Colors.MINECRAFT_BLUE, desc = "").withDependency { potatoMode && editEntityColors }
    private val villagerColor by ColorSetting("Villager", Color(169, 169, 169), desc = "").withDependency { potatoMode && editEntityColors }
    private val ironGolemColor by ColorSetting("Iron Golem", Color(168, 168, 168), desc = "").withDependency { potatoMode && editEntityColors }
    private val batColor by ColorSetting("Bat", Color(79, 79, 79), desc = "").withDependency { potatoMode && editEntityColors }
    private val guardianColor by ColorSetting("Guardian", Color(0, 51, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val xpOrbColor by ColorSetting("XP Orb", Colors.MINECRAFT_YELLOW, desc = "").withDependency { potatoMode && editEntityColors }
    private val armorStandColor by ColorSetting("Armor Stand", Color(169, 169, 169), desc = "").withDependency { potatoMode && editEntityColors }
    private val horseColor by ColorSetting("Horse", Color(153, 102, 51), desc = "").withDependency { potatoMode && editEntityColors }
    private val paintingColor by ColorSetting("Painting", Color(255, 255, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val arrowColor by ColorSetting("Arrow", Color(255, 255, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val snowballColor by ColorSetting("Snowball", Color(255, 255, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val enderPearlColor by ColorSetting("Ender Pearl", Color(255, 255, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val eggColor by ColorSetting("Egg", Color(255, 255, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val potionColor by ColorSetting("Potion", Color(255, 255, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val expBottleColor by ColorSetting("Exp Bottle", Color(255, 255, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val witherSkullColor by ColorSetting("Wither Skull", Color(255, 255, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val itemColor by ColorSetting("Item", Color(255, 255, 255), desc = "").withDependency { potatoMode && editEntityColors }
    private val boatColor by ColorSetting("Boat", Color(255, 255, 255), desc = "").withDependency { potatoMode && editEntityColors }

    private val shouldRenderPlayer by BooleanSetting("Render Player", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderCaveSpider by BooleanSetting("Render Cave Spider", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderSpider by BooleanSetting("Render Spider", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderPig by BooleanSetting("Render Pig", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderSheep by BooleanSetting("Render Sheep", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderCow by BooleanSetting("Render Cow", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderMooshroom by BooleanSetting("Render Mooshroom", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderWolf by BooleanSetting("Render Wolf", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderChicken by BooleanSetting("Render Chicken", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderOcelot by BooleanSetting("Render Ocelot", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderRabbit by BooleanSetting("Render Rabbit", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderSilverfish by BooleanSetting("Render Silverfish", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderEndermite by BooleanSetting("Render Endermite", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderCreeper by BooleanSetting("Render Creeper", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderEnderman by BooleanSetting("Render Enderman", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderSnowman by BooleanSetting("Render Snowman", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderSkeleton by BooleanSetting("Render Skeleton", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderWitch by BooleanSetting("Render Witch", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderBlaze by BooleanSetting("Render Blaze", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderPigZombie by BooleanSetting("Render Pig Zombie", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderZombie by BooleanSetting("Render Zombie", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderSlime by BooleanSetting("Render Slime", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderMagmaCube by BooleanSetting("Render Magma Cube", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderGhast by BooleanSetting("Render Ghast", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderSquid by BooleanSetting("Render Squid", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderVillager by BooleanSetting("Render Villager", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderIronGolem by BooleanSetting("Render Iron Golem", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderBat by BooleanSetting("Render Bat", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderGuardian by BooleanSetting("Render Guardian", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderXPOrb by BooleanSetting("Render XP Orb", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderArmorStand by BooleanSetting("Render Armor Stand", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderHorse by BooleanSetting("Render Horse", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderPainting by BooleanSetting("Render Painting", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderArrow by BooleanSetting("Render Arrow", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderSnowball by BooleanSetting("Render Snowball", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderEnderPearl by BooleanSetting("Render Ender Pearl", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderEgg by BooleanSetting("Render Egg", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderPotion by BooleanSetting("Render Potion", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderExpBottle by BooleanSetting("Render Exp Bottle", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderWitherSkull by BooleanSetting("Render Wither Skull", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderItem by BooleanSetting("Render Item", true, desc = "").withDependency { potatoMode && editShouldRender }
    private val shouldRenderBoat by BooleanSetting("Render Boat", true, desc = "").withDependency { potatoMode && editShouldRender }

    init {
        execute(500) {
            mc.theWorld?.loadedEntityList?.forEach {
                if (!DungeonUtils.inDungeons) return@execute
                if (removePuzzleBlazeNames || removePuzzleBlaze) removeBlazePuzzleNames(it)
                if (it !is EntityArmorStand) return@forEach
                if (hideArcherBones) handleHideArcherBones(it)
                if (removeTentacles) removeTentacles(it)
                if (hideHealerFairy) handleHealerFairy(it)
                if (hideSoulWeaver) handleSoulWeaver(it)
                if (hideNonStarredMobName) hideNonStarredMob(it)
                if (hideWitherMinerName) handleWitherMiner(it)
                if (hideTerracottaName) handleTerracotta(it)
            }
        }
    }

    private val healthMatches = arrayOf(
        Regex("^§.\\[§.Lv\\d+§.] §.+ (?:§.)+0§f/.+§c❤$"),
        Regex("^.+ (?:§.)+0§c❤$")
    )

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        if (!LocationUtils.isInSkyblock) return
        if (event.packet is S1CPacketEntityMetadata && hide0HealthNames) {
            mc.theWorld?.getEntityByID(event.packet.entityId)?.let { entity ->
                event.packet.func_149376_c()?.find { it.objectType == 4 }?.toString()?.let { name ->
                    if (healthMatches.any { regex -> regex.matches(name) }) entity.setDead()
                }
            }
        }

        if (event.packet is S0EPacketSpawnObject && event.packet.type == 70 && fallingBlocks) event.isCanceled = true

        if (event.packet !is S2APacketParticles) return

        if (event.packet.particleType.equalsOneOf(EnumParticleTypes.EXPLOSION_NORMAL, EnumParticleTypes.EXPLOSION_LARGE, EnumParticleTypes.EXPLOSION_HUGE) && removeExplosion)
            event.isCanceled = true

        if (DungeonUtils.getF7Phase() == M7Phases.P5 && hideParticles && !event.packet.particleType.equalsOneOf(EnumParticleTypes.ENCHANTMENT_TABLE, EnumParticleTypes.FLAME, EnumParticleTypes.FIREWORKS_SPARK))
            event.isCanceled = true

        if (hideHeartParticles && event.packet.particleType == EnumParticleTypes.HEART)
            event.isCanceled = true
    }

    private val renderList = arrayListOf<Pair<Entity, Color>>()
    private val defaultRenderList = arrayListOf<Entity>()

    @SubscribeEvent
    fun renderEntities(event: RenderWorldLastEvent) {
        mc.mcProfiler.endStartSection("entities2")
        GlStateManager.pushMatrix()
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.translate(-mc.renderManager.viewerPosX, -mc.renderManager.viewerPosY, -mc.renderManager.viewerPosZ)

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        for (pair in renderList) {
            val color = pair.second
            if (color.alpha < 0.1) continue
            val box = pair.first.renderBoundingBox
            drawEntityProxy(box, color)
        }
        tessellator.draw()
        renderList.clear()

        GlStateManager.resetColor()
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    private fun drawEntityProxy(box: AxisAlignedBB, color: Color) {
        worldRenderer.pos(box.maxX, box.minY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.minY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.minY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.maxY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.minY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.minY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.maxY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.maxX, box.minY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.minY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.maxY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.minX, box.minY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.maxX, box.minY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
    }

    private val entityColorMap = mapOf(
        EntityPlayer::class.java to Pair(::shouldRenderPlayer, ::playerColor),
        EntityCaveSpider::class.java to Pair(::shouldRenderCaveSpider, ::caveSpiderColor),
        EntitySpider::class.java to Pair(::shouldRenderSpider, ::spiderColor),
        EntityPig::class.java to Pair(::shouldRenderPig, ::pigColor),
        EntitySheep::class.java to Pair(::shouldRenderSheep, ::sheepColor),
        EntityCow::class.java to Pair(::shouldRenderCow, ::cowColor),
        EntityMooshroom::class.java to Pair(::shouldRenderMooshroom, ::mooshroomColor),
        EntityWolf::class.java to Pair(::shouldRenderWolf, ::wolfColor),
        EntityChicken::class.java to Pair(::shouldRenderChicken, ::chickenColor),
        EntityOcelot::class.java to Pair(::shouldRenderOcelot, ::ocelotColor),
        EntityRabbit::class.java to Pair(::shouldRenderRabbit, ::rabbitColor),
        EntitySilverfish::class.java to Pair(::shouldRenderSilverfish, ::silverfishColor),
        EntityEndermite::class.java to Pair(::shouldRenderEndermite, ::endermiteColor),
        EntityCreeper::class.java to Pair(::shouldRenderCreeper, ::creeperColor),
        EntityEnderman::class.java to Pair(::shouldRenderEnderman, ::endermanColor),
        EntitySnowman::class.java to Pair(::shouldRenderSnowman, ::snowmanColor),
        EntitySkeleton::class.java to Pair(::shouldRenderSkeleton, ::skeletonColor),
        EntityWitch::class.java to Pair(::shouldRenderWitch, ::witchColor),
        EntityBlaze::class.java to Pair(::shouldRenderBlaze, ::blazeColor),
        EntityPigZombie::class.java to Pair(::shouldRenderPigZombie, ::pigZombieColor),
        EntityZombie::class.java to Pair(::shouldRenderZombie, ::zombieColor),
        EntitySlime::class.java to Pair(::shouldRenderSlime, ::slimeColor),
        EntityMagmaCube::class.java to Pair(::shouldRenderMagmaCube, ::magmaCubeColor),
        EntityGhast::class.java to Pair(::shouldRenderGhast, ::ghastColor),
        EntitySquid::class.java to Pair(::shouldRenderSquid, ::squidColor),
        EntityVillager::class.java to Pair(::shouldRenderVillager, ::villagerColor),
        EntityIronGolem::class.java to Pair(::shouldRenderIronGolem, ::ironGolemColor),
        EntityBat::class.java to Pair(::shouldRenderBat, ::batColor),
        EntityGuardian::class.java to Pair(::shouldRenderGuardian, ::guardianColor),
        EntityXPOrb::class.java to Pair(::shouldRenderXPOrb, ::xpOrbColor),
        EntityArmorStand::class.java to Pair(::shouldRenderArmorStand, ::armorStandColor),
        EntityHorse::class.java to Pair(::shouldRenderHorse, ::horseColor),
        EntityPainting::class.java to Pair(::shouldRenderPainting, ::paintingColor),
        EntityArrow::class.java to Pair(::shouldRenderArrow, ::arrowColor),
        EntitySnowball::class.java to Pair(::shouldRenderSnowball, ::snowballColor),
        EntityEnderPearl::class.java to Pair(::shouldRenderEnderPearl, ::enderPearlColor),
        EntityEgg::class.java to Pair(::shouldRenderEgg, ::eggColor),
        EntityPotion::class.java to Pair(::shouldRenderPotion, ::potionColor),
        EntityExpBottle::class.java to Pair(::shouldRenderExpBottle, ::expBottleColor),
        EntityWitherSkull::class.java to Pair(::shouldRenderWitherSkull, ::witherSkullColor),
        EntityItem::class.java to Pair(::shouldRenderItem, ::itemColor),
        EntityBoat::class.java to Pair(::shouldRenderBoat, ::boatColor)
    )

    private fun getColor(entity: Entity): Color? {
        val (toggle, color) = entityColorMap[entity.javaClass] ?: return null
        return if (toggle()) color() else null
    }

    @JvmStatic
    fun hookRenderEntities(renderViewEntity: Entity, camera: ICamera, partialTicks: Float, ci: CallbackInfo) {
        if (enabled && potatoMode) ci.cancel() else return
        if (MinecraftForgeClient.getRenderPass() != 0) return

        mc.renderManager.cacheActiveRenderInfo(mc.theWorld, mc.fontRendererObj, mc.renderViewEntity, mc.pointedEntity, mc.gameSettings, partialTicks)
        val viewX = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * partialTicks
        val viewY = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * partialTicks
        val viewZ = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * partialTicks
        mc.renderManager.setRenderPosition(viewX, viewY, viewZ)

        renderTileEntities(camera, partialTicks)

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity.isInvisible || (mc.gameSettings.thirdPersonView == 0 && entity == mc.renderViewEntity) ||
                (entity.posY < 0.0 || entity.posY >= 256.0 || !mc.theWorld.isBlockLoaded(BlockPos(entity)))) continue
            if (!(mc.renderManager.shouldRender(entity, camera, mc.renderManager.viewerPosX, mc.renderManager.viewerPosY, mc.renderManager.viewerPosZ))) continue
            if (entity.ticksExisted == 0) {
                entity.lastTickPosX = entity.posX
                entity.lastTickPosY = entity.posY
                entity.lastTickPosZ = entity.posZ
            }

            if (showStarredNametags && entity is EntityArmorStand && entity.name.startsWith("§6✯ ")) {
                mc.renderManager.renderEntitySimple(entity, partialTicks)
                continue
            }

            getColor(entity)?.let { renderList.add(Pair(entity, it)) } ?: defaultRenderList.add(entity)
        }

        for (entity in defaultRenderList) { mc.renderManager.renderEntitySimple(entity, partialTicks) }
        defaultRenderList.clear()
    }

    private fun renderTileEntities(camera: ICamera, partialTicks: Float) {
        TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(mc.theWorld, mc.textureManager, mc.fontRendererObj, mc.renderViewEntity, partialTicks)
        val entity = mc.renderViewEntity
        TileEntityRendererDispatcher.staticPlayerX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
        TileEntityRendererDispatcher.staticPlayerY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
        TileEntityRendererDispatcher.staticPlayerZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks

        for (tileEntity in mc.theWorld.loadedTileEntityList) {
            if (tileEntity is TileEntitySign || tileEntity is TileEntityEndPortal) continue
            tileEntity?.let { if (camera.isBoundingBoxInFrustum(it.renderBoundingBox)) TileEntityRendererDispatcher.instance.renderTileEntity(it, partialTicks, -1) }
        }
    }

    private fun handleHideArcherBones(entity: Entity) {
        val itemEntity = entity as? EntityItem ?: return
        if (itemEntity.entityItem.itemDamage == 15 && itemEntity.entityItem.item === Items.dye)
            entity.setDead()
    }

    private fun removeTentacles(entity: Entity) {
        if (DungeonUtils.getF7Phase() == M7Phases.P5 && getSkullValue(entity) == TENTACLE_TEXTURE)
            entity.setDead()
    }

    private fun handleHealerFairy(entity: Entity) {
        val armorStand = entity as? EntityArmorStand ?: return
        if (armorStand.heldItem?.item == Items.skull && armorStand.heldItem?.skullTexture == HEALER_FAIRY_TEXTURE)
            armorStand.setDead()
    }

    private fun handleSoulWeaver(entity: Entity) {
        if (getSkullValue(entity) == SOUL_WEAVER_TEXTURE) entity.setDead()
    }

    private fun handleWitherMiner(entity: Entity) {
        if (entity.customNameTag.noControlCodes.containsOneOf("Wither Miner", "Wither Guard", "Apostle"))
            entity.setDead()
    }

    private fun handleTerracotta(entity: Entity) {
        if (entity.customNameTag.noControlCodes.contains("Terracotta "))
            entity.setDead()
    }

    private fun hideNonStarredMob(entity: Entity) {
        val name = entity.customNameTag
        if (!DungeonUtils.inBoss && !name.startsWith("§6✯ ") && name.contains("§c❤") && dungeonMobSpawns.any { it in name })
            entity.setDead()
    }

    private fun removeBlazePuzzleNames(entity: Entity) {
        if (removePuzzleBlaze && entity is EntityBlaze) entity.setDead()
        if (removePuzzleBlazeNames && entity.customNameTag.noControlCodes.startsWith("[Lv15] Blaze "))
            entity.alwaysRenderNameTag = false
    }
}