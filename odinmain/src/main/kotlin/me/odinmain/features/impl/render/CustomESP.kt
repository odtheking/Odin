
    package me.odinmain.features.impl.render

    import me.odinmain.OdinMain.onLegitVersion
    import me.odinmain.config.MiscConfig.espList
    import me.odinmain.events.impl.PostEntityMetadata
    import me.odinmain.events.impl.RenderEntityOutlineEvent
    import me.odinmain.features.Category
    import me.odinmain.features.Module
    import me.odinmain.features.settings.Setting.Companion.withDependency
    import me.odinmain.features.settings.impl.BooleanSetting
    import me.odinmain.features.settings.impl.ColorSetting
    import me.odinmain.features.settings.impl.NumberSetting
    import me.odinmain.features.settings.impl.SelectorSetting
    import me.odinmain.utils.ServerUtils.getPing
    import me.odinmain.utils.getPositionEyes
    import me.odinmain.utils.render.Color
    import me.odinmain.utils.render.RenderUtils
    import me.odinmain.utils.render.RenderUtils.renderVec
    import me.odinmain.utils.render.RenderUtils.renderX
    import me.odinmain.utils.render.RenderUtils.renderY
    import me.odinmain.utils.render.RenderUtils.renderZ
    import net.minecraft.entity.Entity
    import net.minecraft.entity.boss.EntityWither
    import net.minecraft.entity.item.EntityArmorStand
    import net.minecraftforge.client.event.RenderWorldLastEvent
    import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

    object CustomESP : Module(
        name = "Custom ${if (onLegitVersion) "Highlight" else "ESP"}",
        category = Category.RENDER,
        tag = TagType.FPSTAX,
        description =
        if (onLegitVersion) "Allows you to highlight selected mobs. (/highlight)"
        else "Allows you to see selected mobs through walls. (/esp)"
    ) {
        private val scanDelay: Long by NumberSetting("Scan Delay", 500L, 10L, 2000L, 100L)
        private val starredMobESP: Boolean by BooleanSetting("Starred Mob ESP", true, description = "Highlights mobs with a star in their name (remove star from the separate list).")
        val color: Color by ColorSetting("Color", Color.RED, true)
        val mode: Int by SelectorSetting("Mode", "Outline", arrayListOf("Outline", "Overlay", "Boxes"))
        private val tracerLimit: Int by NumberSetting("Tracer Limit", 0, 0, 15, description = "ESP will draw tracer to all mobs when you have under this amount of mobs marked, set to 0 to disable. Helpful for finding lost mobs.")

        private val xray: Boolean by BooleanSetting("Through Walls", true).withDependency { !onLegitVersion }
        private val thickness: Float by NumberSetting("Outline Thickness", 5f, 1f, 20f, 0.5f).withDependency { mode != 1 }

        val renderThrough: Boolean get() = if (onLegitVersion) false else xray

        var currentEntities = mutableSetOf<Entity>()

        init {
            execute({ scanDelay }) {
                currentEntities.removeAll { it.isDead }
                getEntities()
            }

            execute(30_000) {
                currentEntities.clear()
                getEntities()
            }

            onWorldLoad { currentEntities.clear() }
        }

        @SubscribeEvent
        fun onRenderEntityModel(event: RenderEntityOutlineEvent) {
            if (mode != 0) return

            if (event.type === RenderEntityOutlineEvent.Type.NO_XRAY && !renderThrough) {
                event.queueEntitiesToOutline { getMob(it) }
            } else if (event.type === RenderEntityOutlineEvent.Type.XRAY && renderThrough) {
                event.queueEntitiesToOutline { getMob(it) }
            }
        }

        private fun getMob(entity: Entity): Int? {
            if (entity !in currentEntities) return null
            return color.rgba
        }


        @SubscribeEvent
        fun onRenderWorldLast(event: RenderWorldLastEvent) {
            currentEntities.forEach {
                if (currentEntities.size < tracerLimit)
                    RenderUtils.draw3DLine(getPositionEyes(mc.thePlayer.renderVec), getPositionEyes(it.renderVec), color, 2, false)

                if (mode == 2)
                    RenderUtils.drawBoxOutline(
                        it.renderX - it.width / 2, it.width.toDouble(),
                        it.renderY, it.height.toDouble(),
                        it.renderZ - it.width / 2, it.width.toDouble(),
                        color, thickness / 5f, renderThrough
                    )
            }
        }

        @SubscribeEvent
        fun postMeta(event: PostEntityMetadata) {
            val entity = mc.theWorld.getEntityByID(event.packet.entityId) ?: return
            checkEntity(entity)
            if (starredMobESP) checkStarred(entity)
        }

        private fun getEntities() {
            mc.theWorld?.loadedEntityList?.forEach {
                checkEntity(it)
                if (starredMobESP) checkStarred(it)
            }
        }

        private fun checkEntity(entity: Entity) {
            if (entity !is EntityArmorStand || espList.none { entity.name.contains(it, true) } || entity in currentEntities) return
            currentEntities.add(getMobEntity(entity) ?: return)
        }

        private fun checkStarred(entity: Entity) {
            if (entity !is EntityArmorStand || !entity.name.startsWith("§6✯ ") || !entity.name.endsWith("§c❤") || entity in currentEntities) return
            currentEntities.add(getMobEntity(entity) ?: return)
        }

        private fun getMobEntity(entity: Entity): Entity? {
            return mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, entity.entityBoundingBox.expand(0.0, 1.0, 0.0))
                .filter { it != null && it !is EntityArmorStand && it.getPing() != 1 }
                .minByOrNull { entity.getDistanceToEntity(it) }
                .takeIf { !(it is EntityWither && it.isInvisible) }
        }
    }


