# Trajectory Preview Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Render a predicted flight path (line + impact marker + entity-hit highlight) for the projectile item the player is holding.

**Architecture:** A pure physics utility (`ProjectileSim`) does item→launch-parameters mapping and tick-by-tick forward simulation with block/entity collision. A thin render module (`TrajectoryPreview`) wires settings and drawing. Physics constants and simulation structure are ported from the ProjectilesTrajectoryPreview Fabric mod (cloned reference at `/tmp/claude-1001/-home-girish-Odin/d78e02dd-b5ae-4d13-94cd-86eb14395a6a/scratchpad/ptp`, files `physics/ProjectileInfo.java` and `PtpClient.java:252-344`).

**Tech Stack:** Kotlin, Fabric Loom, Minecraft 26.1.2 (Mojang-mapped names — the reference mod targets MC 26.2 with the same names: `getEyePosition`, `getViewVector`, `ClipContext`, `getEntitiesOfClass`, `BowItem.getPowerForTime`).

**Spec:** `docs/superpowers/specs/2026-07-17-trajectory-preview-design.md`

## Global Constraints

- No unit-test infrastructure exists in this repo. The red/green cycle is replaced by: write code → `./gradlew build` must succeed → in-game verification checklist (Task 3). Do not add a test framework.
- Commit message style follows repo convention: plain sentence, no `feat:`/`fix:` prefixes (see `git log --oneline`).
- Module registration pattern: `object <Name> : Module(...)` in `features/impl/<category>/`; category is inferred from package when the `category` ctor arg is omitted (`Module.kt:128`).
- Rendering only via existing `RenderEvent.Extract` extension functions in `utils/render/RenderUtils.kt` — do not add new GL/render code.
- All simulation is client-side and purely visual. No packets, no server interaction.
- First `./gradlew build` downloads MC + generates mappings — takes several minutes; that is normal, not a hang.

---

### Task 1: `ProjectileSim` physics utility

**Files:**
- Create: `src/main/kotlin/com/odtheking/odin/utils/ProjectileSim.kt`

**Interfaces:**
- Consumes: nothing from other tasks. Uses `com.odtheking.odin.OdinMod.mc` only indirectly (callers pass `Player`).
- Produces (Task 2 relies on these exact signatures):
  - `ProjectileSim.launchFor(stack: ItemStack, player: Player, partialTicks: Float): Launch?`
  - `ProjectileSim.simulate(player: Player, launch: Launch, maxTicks: Int = 200): SimResult`
  - `ProjectileSim.Launch(type: ProjectileType, position: Vec3, velocity: Vec3)`
  - `ProjectileSim.SimResult(points: List<Vec3>, blockHit: BlockHitResult?, entityHit: Entity?)`
  - `ProjectileSim.ProjectileType` enum: `ARROW`, `THROWN`, `POTION`, `FISHING_ROD`

- [ ] **Step 1: Write the file**

```kotlin
package com.odtheking.odin.utils

import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.EggItem
import net.minecraft.world.item.EnderpearlItem
import net.minecraft.world.item.FishingRodItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SnowballItem
import net.minecraft.world.item.ThrowablePotionItem
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

/**
 * Client-side forward simulation of vanilla projectile physics.
 * Constants and step order ported from maDU59's ProjectilesTrajectoryPreview (MIT).
 * The preview is the center line — vanilla adds random per-shot spread.
 */
object ProjectileSim {

    private const val DEG_TO_RAD = 0.017453292f

    // P = pos += vel, D = vel *= drag, G = vel.y -= gravity, applied in listed order each tick
    enum class StepOrder { PDG, GDP, GPD }

    enum class ProjectileType(
        val gravity: Double,
        val drag: Double,
        val waterDrag: Double,
        val order: StepOrder,
        val hitsWater: Boolean = false
    ) {
        ARROW(0.05, 0.99, 0.6, StepOrder.PDG),
        THROWN(0.03, 0.99, 0.8, StepOrder.GDP),
        POTION(0.05, 0.99, 0.8, StepOrder.GDP),
        FISHING_ROD(0.03, 0.92, 0.92, StepOrder.GPD, hitsWater = true)
    }

    data class Launch(val type: ProjectileType, val position: Vec3, val velocity: Vec3)

    data class SimResult(val points: List<Vec3>, val blockHit: BlockHitResult?, val entityHit: Entity?)

    /**
     * Maps the held item to launch parameters, or null if the item is not a supported projectile.
     * Bows use the real draw charge while drawing, otherwise assume full charge —
     * Skyblock shortbows (Terminator, Juju) always fire at full power without drawing.
     */
    fun launchFor(stack: ItemStack, player: Player, partialTicks: Float): Launch? {
        val eyePos = player.getEyePosition(partialTicks).subtract(0.0, 0.1, 0.0)
        return when (stack.item) {
            is BowItem -> {
                val charge = if (player.isUsingItem && player.useItem === stack)
                    BowItem.getPowerForTime(player.ticksUsingItem)
                else 1f
                if (charge < 0.1f) return null
                Launch(ProjectileType.ARROW, eyePos, player.getViewVector(partialTicks).scale(3.0 * charge))
            }
            is EnderpearlItem, is SnowballItem, is EggItem ->
                Launch(ProjectileType.THROWN, eyePos, player.getViewVector(partialTicks).scale(1.5))
            is ThrowablePotionItem ->
                Launch(ProjectileType.POTION, eyePos, angleFromRot(player.xRot, player.yRot, -20f).scale(0.5))
            is FishingRodItem -> {
                if (player.fishing != null) return null
                val h = Mth.cos(-player.yRot * DEG_TO_RAD - Mth.PI)
                val i = Mth.sin(-player.yRot * DEG_TO_RAD - Mth.PI)
                val j = -Mth.cos(-player.xRot * DEG_TO_RAD)
                val k = Mth.sin(-player.xRot * DEG_TO_RAD)
                val eye = player.getEyePosition(partialTicks)
                val pos = Vec3(eye.x - i * 0.3, eye.y, eye.z - h * 0.3)
                var vel = Vec3((-i).toDouble(), Mth.clamp(-(k / j), -5f, 5f).toDouble(), (-h).toDouble())
                val len = vel.length()
                vel = vel.multiply(0.6 / len + 0.5, 0.6 / len + 0.5, 0.6 / len + 0.5)
                Launch(ProjectileType.FISHING_ROD, pos, vel)
            }
            else -> null
        }
    }

    /**
     * Ticks the projectile forward until it hits a block, hits an entity,
     * falls out of the world, or maxTicks elapse.
     */
    fun simulate(player: Player, launch: Launch, maxTicks: Int = 200): SimResult {
        val level = player.level()
        val points = ArrayList<Vec3>(maxTicks + 2)
        var pos = launch.position
        var prevPos = pos
        var vel = launch.velocity.add(player.deltaMovement)
        var drag = launch.type.drag
        val gravity = launch.type.gravity

        repeat(maxTicks) {
            points.add(pos)

            when (launch.type.order) {
                StepOrder.PDG -> { pos = pos.add(vel); vel = vel.scale(drag); vel = vel.subtract(0.0, gravity, 0.0) }
                StepOrder.GDP -> { vel = vel.subtract(0.0, gravity, 0.0); vel = vel.scale(drag); pos = pos.add(vel) }
                StepOrder.GPD -> { vel = vel.subtract(0.0, gravity, 0.0); pos = pos.add(vel); vel = vel.scale(drag) }
            }

            var closestEntity: Entity? = null
            var closestDistSq = Double.MAX_VALUE
            var entityHitPos: Vec3? = null
            level.getEntitiesOfClass(Entity::class.java, AABB(prevPos, pos).inflate(1.0)) { e ->
                !e.isSpectator && e.isAlive && e !is Projectile && e !is ItemEntity && e !is ExperienceOrb && e !== player
            }.forEach { entity ->
                val clip = entity.boundingBox.inflate(entity.pickRadius.toDouble()).clip(prevPos, pos)
                if (clip.isPresent) {
                    val distSq = prevPos.distanceToSqr(clip.get())
                    if (distSq < closestDistSq) {
                        closestDistSq = distSq
                        closestEntity = entity
                        entityHitPos = clip.get()
                    }
                }
            }

            val fluid = if (launch.type.hitsWater) ClipContext.Fluid.WATER else ClipContext.Fluid.NONE
            val blockHit = level.clip(ClipContext(prevPos, pos, ClipContext.Block.COLLIDER, fluid, player))
            if (!launch.type.hitsWater) {
                drag = if (blockHit.type == HitResult.Type.MISS &&
                    level.clip(ClipContext(prevPos, pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER, player)).type != HitResult.Type.MISS
                ) launch.type.waterDrag else launch.type.drag
            }

            if (blockHit.type != HitResult.Type.MISS && prevPos.distanceToSqr(blockHit.location) < closestDistSq) {
                points.add(blockHit.location)
                return SimResult(points, blockHit, null)
            }
            entityHitPos?.let { hit ->
                points.add(hit)
                return SimResult(points, null, closestEntity)
            }
            if (pos.y < level.minY - 120) return SimResult(points, null, null)
            prevPos = pos
        }
        return SimResult(points, null, null)
    }

    // Direction vector from pitch/yaw with a pitch offset — vanilla thrown-potion launch uses -20°.
    private fun angleFromRot(pitch: Float, yaw: Float, pitchOffset: Float): Vec3 {
        val x = -Mth.sin(yaw * DEG_TO_RAD) * Mth.cos(pitch * DEG_TO_RAD)
        val y = -Mth.sin((pitch + pitchOffset) * DEG_TO_RAD)
        val z = Mth.cos(yaw * DEG_TO_RAD) * Mth.cos(pitch * DEG_TO_RAD)
        return Vec3(x.toDouble(), y.toDouble(), z.toDouble()).normalize()
    }
}
```

API fallbacks if `./gradlew build` reports unresolved names on MC 26.1.2 (reference mod compiles these on 26.2, so failures are unlikely):
- `BowItem.getPowerForTime(int)` missing → inline vanilla formula: `val t = player.ticksUsingItem / 20f; val charge = ((t * t + t * 2f) / 3f).coerceAtMost(1f)`.
- `player.fishing` missing → drop the check (preview then also shows while a bobber is already out — acceptable).
- `Mth.sin`/`Mth.cos` taking Double instead of Float (as in the reference's decompile) → wrap args with `.toDouble()` and results with `.toFloat()`.
- `level.minY` missing → use `level.getMinY()` explicitly or `level.minBuildHeight`.

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`. Fix any unresolved-reference errors using the fallbacks above before proceeding.

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/odtheking/odin/utils/ProjectileSim.kt
git commit -m "Added ProjectileSim projectile physics utility"
```

---

### Task 2: `TrajectoryPreview` module + registration

**Files:**
- Create: `src/main/kotlin/com/odtheking/odin/features/impl/render/TrajectoryPreview.kt`
- Modify: `src/main/kotlin/com/odtheking/odin/features/ModuleManager.kt:66-67` (render group of the `registerModules` call)

**Interfaces:**
- Consumes (from Task 1): `ProjectileSim.launchFor(stack, player, partialTicks): Launch?`, `ProjectileSim.simulate(player, launch): SimResult`, `SimResult.points/blockHit/entityHit`.
- Produces: `object TrajectoryPreview : Module` — referenced only by `ModuleManager`.

- [ ] **Step 1: Write the module**

```kotlin
package com.odtheking.odin.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ProjectileSim
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.drawLine
import com.odtheking.odin.utils.render.drawWireFrameBox
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.EggItem
import net.minecraft.world.item.EnderpearlItem
import net.minecraft.world.item.FishingRodItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SnowballItem
import net.minecraft.world.item.ThrowablePotionItem
import net.minecraft.world.phys.AABB

object TrajectoryPreview : Module(
    name = "Trajectory Preview",
    description = "Renders the predicted flight path of held projectiles. Vanilla adds random spread, so real impacts can differ slightly."
) {
    private val arrows by BooleanSetting("Arrows", true, desc = "Preview bow arrows. Assumes full charge unless actively drawing.")
    private val pearls by BooleanSetting("Ender Pearls", true, desc = "Preview ender pearls.")
    private val snowballs by BooleanSetting("Snowballs & Eggs", false, desc = "Preview snowballs and eggs.")
    private val fishingRod by BooleanSetting("Fishing Rod", false, desc = "Preview fishing rod casts.")
    private val potions by BooleanSetting("Potions", false, desc = "Preview splash and lingering potions.")
    private val lineColor by ColorSetting("Line Color", Colors.WHITE, true, desc = "Color of the trajectory line.")
    private val impactColor by ColorSetting("Impact Color", Colors.MINECRAFT_RED, true, desc = "Color of the impact marker.")
    private val entityColor by ColorSetting("Entity Hit Color", Colors.MINECRAFT_YELLOW, true, desc = "Color of the entity hit highlight.")
    private val lineWidth by NumberSetting("Line Width", 3f, 1, 5, 0.5, desc = "Thickness of the trajectory line.")
    private val throughWalls by BooleanSetting("Through Walls", false, desc = "Renders the preview through blocks.")

    init {
        on<RenderEvent.Extract> {
            val player = mc.player ?: return@on
            val stack = player.mainHandItem
            if (!typeEnabled(stack)) return@on
            val partialTicks = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
            val launch = ProjectileSim.launchFor(stack, player, partialTicks) ?: return@on
            val result = ProjectileSim.simulate(player, launch)
            if (result.points.size < 2) return@on

            drawLine(result.points, lineColor, depth = !throughWalls, thickness = lineWidth)
            result.blockHit?.let {
                drawFilledBox(AABB.ofSize(it.location, 0.25, 0.25, 0.25), impactColor, depth = !throughWalls)
            }
            result.entityHit?.let {
                drawWireFrameBox(it.boundingBox, entityColor, depth = !throughWalls)
            }
        }
    }

    private fun typeEnabled(stack: ItemStack): Boolean = when (stack.item) {
        is BowItem -> arrows
        is EnderpearlItem -> pearls
        is SnowballItem, is EggItem -> snowballs
        is FishingRodItem -> fishingRod
        is ThrowablePotionItem -> potions
        else -> false
    }
}
```

Notes for the implementer:
- `mc` comes from the `Module` base class (`Module.kt:50`) — no import needed.
- `drawLine`/`drawFilledBox`/`drawWireFrameBox` are extension functions on the `RenderEvent.Extract` receiver — they only resolve inside the `on<RenderEvent.Extract>` block.
- `EnderpearlItem` is checked before `SnowballItem` deliberately — keep that order in both `typeEnabled` and `launchFor` in case of subclass surprises.
- Category ctor arg omitted on purpose: inferred as RENDER from the package (`Module.kt:128`).

- [ ] **Step 2: Register the module**

In `src/main/kotlin/com/odtheking/odin/features/ModuleManager.kt`, render group (~line 66-67), append `TrajectoryPreview`:

```kotlin
            // render
            ClickGUIModule, Camera, Etherwarp, PlayerSize, PerformanceHUD, RenderOptimizer,
            PlayerDisplay, Waypoints, HidePlayers, Highlight, GyroWand, TrajectoryPreview,
```

No import change needed — `import com.odtheking.odin.features.impl.render.*` wildcard already covers the package.

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/odtheking/odin/features/impl/render/TrajectoryPreview.kt src/main/kotlin/com/odtheking/odin/features/ModuleManager.kt
git commit -m "Added Trajectory Preview module"
```

---

### Task 3: In-game verification

**Files:** none (manual verification; fix-up commits only if bugs found).

**Interfaces:**
- Consumes: the complete feature from Tasks 1-2.
- Produces: verified working module.

- [ ] **Step 1: Launch the dev client**

Run: `./gradlew runClient`
Expected: Minecraft dev client opens. Create/open a singleplayer world (vanilla physics there matches the simulation exactly).

- [ ] **Step 2: Walk the checklist**

1. Open Odin's ClickGUI, RENDER category → "Trajectory Preview" module exists with all 10 settings; enable it.
2. Hold an ender pearl → white line renders from eye toward look direction, red box at wall/floor hit. Throw the pearl → it lands at the marked spot (small deviation from random spread is expected).
3. Hold a bow → full-power arc shows. Draw the bow → arc starts short and extends as charge grows.
4. Aim the pearl line at a mob → line stops at the mob, yellow wireframe box around it.
5. Toggle "Ender Pearls" setting off → line disappears while holding a pearl; "Arrows" still works.
6. Toggle "Through Walls" → line visible through terrain when on.
7. Hold a fishing rod (setting on) → short arc; cast → bobber lands near line end. Line disappears while the bobber is out.
8. Throw a splash potion at a wall → impact near the marker.
9. Look straight down and throw a pearl → no crash, line points down (degenerate single-segment case).

- [ ] **Step 3: Fix anything that failed, commit fixes**

Any fix commits use the same style, e.g.:

```bash
git commit -m "Fixed trajectory preview <specific issue>"
```
