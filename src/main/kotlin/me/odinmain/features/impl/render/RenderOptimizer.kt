package me.odinmain.features.impl.render

import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Category
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
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.monster.*
import net.minecraft.entity.passive.*
import net.minecraft.init.Items
import net.minecraft.network.play.server.S0EPacketSpawnObject
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object RenderOptimizer : Module(
    name = "Render Optimizer",
    category = Category.RENDER,
    description = "Disables certain render function when they are not necessary."
) {
    private val fallingBlocks by BooleanSetting(name = "Remove Falling Blocks", default = true, description = "Removes falling blocks that are not necessary.")
    private val removeTentacles by BooleanSetting(name = "Remove P5 Tentacles", default = true, description = "Removes armorstands of tentacles which are not necessary.")
    private val hideHealerFairy by BooleanSetting(name = "Hide Healer Fairy", default = true, description = "Hides the healer fairy.")
    private val hideSoulWeaver by BooleanSetting(name = "Hide Soul Weaver", default = true, description = "Hides the soul weaver.")
    private val hideArcherBones by BooleanSetting(name = "Hide Archer Bones", default = true, description = "Hides the archer bones.")
    private val hide0HealthNames by BooleanSetting(name = "Hide 0 Health", default = true, description = "Hides the names of entities with 0 health.")
    private val hideWitherMinerName by BooleanSetting(name = "Hide WitherMiner Name", default = true, description = "Hides the wither miner name.")
    private val hideTerracottaName by BooleanSetting(name = "Hide Terracota Name", default = true, description = "Hides the terracota name.")
    private val hideNonStarredMobName by BooleanSetting(name = "Hide Non-Starred Mob Name", default = true, description = "Hides the non-starred mob name.")
    private val removeBlazePuzzleNames by BooleanSetting(name = "Hide blazes", default = false, description = "Hides the blazes in the blaze puzzle room.")

    private val showParticleOptions by DropdownSetting("Show Particles Options")
    private val removeExplosion by BooleanSetting("Remove Explosion", default = false, description = "Removes explosion particles.").withDependency { showParticleOptions }
    private val hideParticles by BooleanSetting(name = "Hide P5 Particles", default = true, description = "Hides particles that are not necessary.").withDependency { showParticleOptions }
    private val hideHeartParticles by BooleanSetting(name = "Hide Heart Particles", default = false, description = "Hides heart particles.").withDependency { showParticleOptions }

    private const val TENTACLE_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg1NzI3NzI0OSwKICAicHJvZmlsZUlkIiA6ICIxODA1Y2E2MmM0ZDI0M2NiOWQxYmY4YmM5N2E1YjgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJSdWxsZWQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdkODM2NzQ5MjZiODk3MTRlNmI1YTU1NDcwNTAxYzA0YjA2NmRkODdiZjZjMzM1Y2RkYzZlNjBhMWExYTVmNSIKICAgIH0KICB9Cn0="
    private const val HEALER_FAIRY_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcxOTQ2MzA5MTA0NywKICAicHJvZmlsZUlkIiA6ICIyNjRkYzBlYjVlZGI0ZmI3OTgxNWIyZGY1NGY0OTgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJxdWludHVwbGV0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJlZWRjZmZjNmExMWEzODM0YTI4ODQ5Y2MzMTZhZjdhMjc1MmEzNzZkNTM2Y2Y4NDAzOWNmNzkxMDhiMTY3YWUiCiAgICB9CiAgfQp9"
    private const val SOUL_WEAVER_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NTk1ODAzNjI1NTMsInByb2ZpbGVJZCI6ImU3NmYwZDlhZjc4MjQyYzM5NDY2ZDY3MjE3MzBmNDUzIiwicHJvZmlsZU5hbWUiOiJLbGxscmFoIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yZjI0ZWQ2ODc1MzA0ZmE0YTFmMGM3ODViMmNiNmE2YTcyNTYzZTlmM2UyNGVhNTVlMTgxNzg0NTIxMTlhYTY2In19fQ=="
    private val dungeonMobSpawns = setOf("Lurker", "Dreadlord", "Souleater", "Zombie", "Skeleton", "Skeletor", "Sniper", "Super Archer", "Spider", "Fels", "Withermancer")

    private val potatoMode by BooleanSetting(name = "Potato Mode", description = "")
    private val editMobColors by DropdownSetting("Edit Mob Colors").withDependency { potatoMode }

    private val caveSpiderColor by ColorSetting(name = "Cave Spider", default = Color(102, 0, 255), description = "").withDependency { potatoMode && editMobColors }
    private val spiderColor by ColorSetting(name = "Spider", default = Color(102, 102, 102), description = "").withDependency { potatoMode && editMobColors }
    private val pigColor by ColorSetting(name = "Pig", default = Color(255, 102, 255), description = "").withDependency { potatoMode && editMobColors }
    private val sheepColor by ColorSetting(name = "Sheep", default = Color.WHITE, description = "").withDependency { potatoMode && editMobColors }
    private val cowColor by ColorSetting(name = "Cow", default = Color(0, 255, 0), description = "").withDependency { potatoMode && editMobColors }
    private val mooshroomColor by ColorSetting(name = "Mooshroom", default = Color.RED, description = "").withDependency { potatoMode && editMobColors }
    private val wolfColor by ColorSetting(name = "Wolf", default = Color(128, 128, 128), description = "").withDependency { potatoMode && editMobColors }
    private val chickenColor by ColorSetting(name = "Chicken", default = Color.YELLOW, description = "").withDependency { potatoMode && editMobColors }
    private val ocelotColor by ColorSetting(name = "Ocelot", default = Color(255, 204, 0), description = "").withDependency { potatoMode && editMobColors }
    private val rabbitColor by ColorSetting(name = "Rabbit", default = Color(245, 245, 220), description = "").withDependency { potatoMode && editMobColors }
    private val silverfishColor by ColorSetting(name = "Silverfish", default = Color(179, 179, 179), description = "").withDependency { potatoMode && editMobColors }
    private val endermiteColor by ColorSetting(name = "Endermite", default = Color(138, 43, 226), description = "").withDependency { potatoMode && editMobColors }
    private val creeperColor by ColorSetting(name = "Creeper", default = Color(0, 255, 0), description = "").withDependency { potatoMode && editMobColors }
    private val endermanColor by ColorSetting(name = "Enderman", default = Color(51, 0, 102), description = "").withDependency { potatoMode && editMobColors }
    private val snowmanColor by ColorSetting(name = "Snowman", default = Color(240, 240, 240), description = "").withDependency { potatoMode && editMobColors }
    private val skeletonColor by ColorSetting(name = "Skeleton", default = Color(51, 51, 51), description = "").withDependency { potatoMode && editMobColors }
    private val witchColor by ColorSetting(name = "Witch", default = Color(128, 0, 128), description = "").withDependency { potatoMode && editMobColors }
    private val blazeColor by ColorSetting(name = "Blaze", default = Color(255, 102, 0), description = "").withDependency { potatoMode && editMobColors }
    private val pigZombieColor by ColorSetting(name = "Pig Zombie", default = Color(255, 0, 255), description = "").withDependency { potatoMode && editMobColors }
    private val zombieColor by ColorSetting(name = "Zombie", default = Color(0, 255, 0), description = "").withDependency { potatoMode && editMobColors }
    private val slimeColor by ColorSetting(name = "Slime", default = Color(0, 255, 0), description = "").withDependency { potatoMode && editMobColors }
    private val magmaCubeColor by ColorSetting(name = "Magma Cube", default = Color(255, 51, 51), description = "").withDependency { potatoMode && editMobColors }
    private val giantZombieColor by ColorSetting(name = "Giant Zombie", default = Color.RED, description = "").withDependency { potatoMode && editMobColors }
    private val ghastColor by ColorSetting(name = "Ghast", default = Color(255, 0, 102), description = "").withDependency { potatoMode && editMobColors }
    private val squidColor by ColorSetting(name = "Squid", default = Color.BLUE, description = "").withDependency { potatoMode && editMobColors }
    private val villagerColor by ColorSetting(name = "Villager", default = Color(169, 169, 169), description = "").withDependency { potatoMode && editMobColors }
    private val ironGolemColor by ColorSetting(name = "Iron Golem", default = Color(168, 168, 168), description = "").withDependency { potatoMode && editMobColors }
    private val batColor by ColorSetting(name = "Bat", default = Color(79, 79, 79), description = "").withDependency { potatoMode && editMobColors }
    private val guardianColor by ColorSetting(name = "Guardian", default = Color(0, 51, 255), description = "").withDependency { potatoMode && editMobColors }
    private val dragonColor by ColorSetting(name = "Dragon", default = Color(102, 0, 204), description = "").withDependency { potatoMode && editMobColors }
    private val witherColor by ColorSetting(name = "Wither", default = Color.BLACK, description = "").withDependency { potatoMode && editMobColors }
    private val xpOrbColor by ColorSetting(name = "XP Orb", default = Color.YELLOW, description = "").withDependency { potatoMode && editMobColors }
    private val armorStandColor by ColorSetting(name = "Armor Stand", default = Color(169, 169, 169), description = "").withDependency { potatoMode && editMobColors }
    private val horseColor by ColorSetting(name = "Horse", default = Color(153, 102, 51), description = "").withDependency { potatoMode && editMobColors }

    init {
        execute(500) {
            mc.theWorld?.loadedEntityList?.forEach {
                if (!DungeonUtils.inDungeons) return@execute
                if (removeBlazePuzzleNames) removeBlazePuzzleNames(it)
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

    private val mobColors: HashMap<Class<*>, Color> = hashMapOf(
        EntityCaveSpider::class.java to caveSpiderColor,
        EntitySpider::class.java to spiderColor,
        EntityPig::class.java to pigColor,
        EntitySheep::class.java to sheepColor,
        EntityCow::class.java to cowColor,
        EntityMooshroom::class.java to mooshroomColor,
        EntityWolf::class.java to wolfColor,
        EntityChicken::class.java to chickenColor,
        EntityOcelot::class.java to ocelotColor,
        EntityRabbit::class.java to rabbitColor,
        EntitySilverfish::class.java to silverfishColor,
        EntityEndermite::class.java to endermiteColor,
        EntityCreeper::class.java to creeperColor,
        EntityEnderman::class.java to endermanColor,
        EntitySnowman::class.java to snowmanColor,
        EntitySkeleton::class.java to skeletonColor,
        EntityWitch::class.java to witchColor,
        EntityBlaze::class.java to blazeColor,
        EntityPigZombie::class.java to pigZombieColor,
        EntityZombie::class.java to zombieColor,
        EntitySlime::class.java to slimeColor,
        EntityMagmaCube::class.java to magmaCubeColor,
        EntityGiantZombie::class.java to giantZombieColor,
        EntityGhast::class.java to ghastColor,
        EntitySquid::class.java to squidColor,
        EntityVillager::class.java to villagerColor,
        EntityIronGolem::class.java to ironGolemColor,
        EntityBat::class.java to batColor,
        EntityGuardian::class.java to guardianColor,
        EntityDragon::class.java to dragonColor,
        EntityWither::class.java to witherColor,
        EntityXPOrb::class.java to xpOrbColor,
        EntityArmorStand::class.java to armorStandColor,
        EntityHorse::class.java to horseColor
    )

    private val renderList: ArrayList<Entity> = ArrayList()

    @SubscribeEvent
    fun renderEntities(event: RenderWorldLastEvent) {
        mc.mcProfiler.endStartSection("entities2")

        GlStateManager.pushMatrix()
        GlStateManager.disableCull()
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.translate(-mc.renderManager.viewerPosX, -mc.renderManager.viewerPosY, -mc.renderManager.viewerPosZ)

        
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        for (entity in renderList) {
            val color = getColor(entity)
            with (entity.renderBoundingBox) {
                worldRenderer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex()

                worldRenderer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()

                worldRenderer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()

                worldRenderer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex()

                worldRenderer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()

                worldRenderer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex()
                worldRenderer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex()
            }
        }

        renderList.clear()
        tessellator.draw()
        GlStateManager.enableCull()
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
    }

    private fun getColor(entity: Entity): Color {
        return when (entity.javaClass) {
            EntityCaveSpider::class.java -> caveSpiderColor
            EntitySpider::class.java -> spiderColor
            EntityPig::class.java -> pigColor
            EntitySheep::class.java -> sheepColor
            EntityCow::class.java -> cowColor
            EntityMooshroom::class.java -> mooshroomColor
            EntityWolf::class.java -> wolfColor
            EntityChicken::class.java -> chickenColor
            EntityOcelot::class.java -> ocelotColor
            EntityRabbit::class.java -> rabbitColor
            EntitySilverfish::class.java -> silverfishColor
            EntityEndermite::class.java -> endermiteColor
            EntityCreeper::class.java -> creeperColor
            EntityEnderman::class.java -> endermanColor
            EntitySnowman::class.java -> snowmanColor
            EntitySkeleton::class.java -> skeletonColor
            EntityWitch::class.java -> witchColor
            EntityBlaze::class.java -> blazeColor
            EntityPigZombie::class.java -> pigZombieColor
            EntityZombie::class.java -> zombieColor
            EntitySlime::class.java -> slimeColor
            EntityMagmaCube::class.java -> magmaCubeColor
            EntityGiantZombie::class.java -> giantZombieColor
            EntityGhast::class.java -> ghastColor
            EntitySquid::class.java -> squidColor
            EntityVillager::class.java -> villagerColor
            EntityIronGolem::class.java -> ironGolemColor
            EntityBat::class.java -> batColor
            EntityGuardian::class.java -> guardianColor
            EntityDragon::class.java -> dragonColor
            EntityWither::class.java -> witherColor
            EntityXPOrb::class.java -> xpOrbColor
            EntityArmorStand::class.java -> armorStandColor
            EntityHorse::class.java -> horseColor
            else -> Color.WHITE
        }
    }


    fun hookRenderEntities(renderViewEntity: Entity, camera: ICamera, partialTicks: Float, ci: CallbackInfo) {
        if (this.enabled && potatoMode) ci.cancel() else return

        if (MinecraftForgeClient.getRenderPass() != 0) return

        mc.renderManager.cacheActiveRenderInfo(mc.theWorld, mc.fontRendererObj, mc.renderViewEntity, mc.pointedEntity, mc.gameSettings, partialTicks)
        val viewX = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * partialTicks
        val viewY = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * partialTicks
        val viewZ = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * partialTicks
        mc.renderManager.setRenderPosition(viewX, viewY, viewZ)

        renderTileEntities(camera, partialTicks)

        for (entity in mc.theWorld.loadedEntityList) {
            if (!(mc.renderManager.shouldRender(entity, camera, mc.renderManager.viewerPosX, mc.renderManager.viewerPosY, mc.renderManager.viewerPosZ) || entity.riddenByEntity == mc.thePlayer)) continue

            val shouldRender = (entity != mc.renderViewEntity || mc.gameSettings.thirdPersonView != 0 || (mc.renderViewEntity as? EntityLivingBase)?.isPlayerSleeping == true) &&
                    (entity.posY < 0.0 || entity.posY >= 256.0 || mc.theWorld.isBlockLoaded(BlockPos(entity)))

            if (!shouldRender) continue
            if (entity.ticksExisted == 0) {
                entity.lastTickPosX = entity.posX
                entity.lastTickPosY = entity.posY
                entity.lastTickPosZ = entity.posZ
            }

            if (mobColors[entity.javaClass] == null) {
                mc.renderManager.renderEntitySimple(entity, partialTicks)
                continue
            } else renderList.add(entity)
        }
    }

    private fun renderTileEntities(camera: ICamera, partialTicks: Float) {
        TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(mc.theWorld, mc.textureManager, mc.fontRendererObj, mc.renderViewEntity, partialTicks)
        val entity = this.mc.renderViewEntity
        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
        TileEntityRendererDispatcher.staticPlayerX = x
        TileEntityRendererDispatcher.staticPlayerY = y
        TileEntityRendererDispatcher.staticPlayerZ = z

        for (tileEntity in mc.theWorld.loadedTileEntityList) {
            if (camera.isBoundingBoxInFrustum(tileEntity.getRenderBoundingBox())) TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, partialTicks, -1)
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
        if (entity is EntityBlaze) entity.setDead()
        if (entity.customNameTag.noControlCodes.startsWith("[Lv15] Blaze "))
            entity.alwaysRenderNameTag = false
    }
}