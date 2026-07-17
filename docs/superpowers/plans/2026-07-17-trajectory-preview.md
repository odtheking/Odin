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

---

### Task 4: Meteor-style rework (user feedback round 1)

User feedback from in-game testing: line origin renders too low, line/marker visually punches through the floor, entity wireframe unwanted. Rework to match meteor-client's Trajectories approach (reference clone: `/tmp/claude-1001/-home-girish-Odin/d78e02dd-b5ae-4d13-94cd-86eb14395a6a/scratchpad/meteor`, files `utils/entity/simulator/ProjectileEntitySimulator.java`, `systems/modules/render/Trajectories.java` — already Mojang mappings).

**Files:**
- Modify: `src/main/kotlin/com/odtheking/odin/utils/ProjectileSim.kt`
- Modify: `src/main/kotlin/com/odtheking/odin/features/impl/render/TrajectoryPreview.kt`

**Interfaces:**
- Consumes: existing `ProjectileSim` public API (signatures unchanged except `simulate` maxTicks default 200→500).
- Produces: same API. `SimResult` unchanged (blockHit carries the hit face via `blockHit.direction`).

- [ ] **Step 1: Rework `ProjectileSim.kt`**

Apply these exact changes:

1. Replace the `ProjectileType` enum with (adds `power` and `roll`; constants from meteor's MotionData table):

```kotlin
    enum class ProjectileType(
        val power: Double,
        val roll: Float, // pitch offset in degrees — potions are thrown 20° upward
        val gravity: Double,
        val drag: Double,
        val waterDrag: Double,
        val order: StepOrder,
        val hitsWater: Boolean = false
    ) {
        ARROW(0.0, 0f, 0.05, 0.99, 0.6, StepOrder.PDG), // power dynamic: 3.0 × bow charge
        THROWN(1.5, 0f, 0.03, 0.99, 0.8, StepOrder.GDP),
        POTION(0.5, -20f, 0.05, 0.99, 0.8, StepOrder.GDP),
        FISHING_ROD(0.0, 0f, 0.03, 0.92, 0.92, StepOrder.GPD, hitsWater = true) // velocity from vanilla bobber formula
    }
```

2. Replace `launchFor` with (changes: direction computed meteor-style for every type via a shared helper; unpulled-or-barely-pulled bow forces full charge like meteor does for the local player; fishing rod unchanged logic):

```kotlin
    /**
     * Maps the held item to launch parameters, or null if the item is not a supported projectile.
     * Bows use the real draw charge while drawing; below meteor's 0.1 threshold (or when not
     * drawing at all) full charge is assumed — Skyblock shortbows always fire at full power.
     */
    fun launchFor(stack: ItemStack, player: Player, partialTicks: Float): Launch? {
        val eyePos = player.getEyePosition(partialTicks).subtract(0.0, 0.1, 0.0)
        return when (stack.item) {
            is BowItem -> {
                var charge = if (player.isUsingItem && player.useItem === stack)
                    BowItem.getPowerForTime(player.ticksUsingItem)
                else 1f
                if (charge < 0.1f) charge = 1f
                Launch(ProjectileType.ARROW, eyePos, direction(player, 0f).scale(3.0 * charge))
            }
            is EnderpearlItem, is SnowballItem, is EggItem ->
                Launch(ProjectileType.THROWN, eyePos, direction(player, 0f).scale(ProjectileType.THROWN.power))
            is ThrowablePotionItem ->
                Launch(ProjectileType.POTION, eyePos, direction(player, ProjectileType.POTION.roll).scale(ProjectileType.POTION.power))
            is FishingRodItem -> {
                if (player.fishing != null) return null
                val h = Mth.cos((-player.yRot * DEG_TO_RAD - Mth.PI).toDouble())
                val i = Mth.sin((-player.yRot * DEG_TO_RAD - Mth.PI).toDouble())
                val j = -Mth.cos((-player.xRot * DEG_TO_RAD).toDouble())
                val k = Mth.sin((-player.xRot * DEG_TO_RAD).toDouble())
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

    // Meteor's direction formula: yaw/pitch trig with a per-item pitch offset (roll), normalized.
    private fun direction(player: Player, roll: Float): Vec3 {
        val x = -Mth.sin((player.yRot * DEG_TO_RAD).toDouble()) * Mth.cos((player.xRot * DEG_TO_RAD).toDouble())
        val y = -Mth.sin(((player.xRot + roll) * DEG_TO_RAD).toDouble())
        val z = Mth.cos((player.yRot * DEG_TO_RAD).toDouble()) * Mth.cos((player.xRot * DEG_TO_RAD).toDouble())
        return Vec3(x.toDouble(), y.toDouble(), z.toDouble()).normalize()
    }
```

Note: if `Mth.sin`/`Mth.cos` return Float in this codebase's mappings (they do — Task 1 established `float sin(double)`), the products `x`/`z` are Float×Float — keep the `.toDouble()` conversions on the results as shown when building the `Vec3`. Adjust conversions only as the compiler requires; the formula itself is fixed.

3. Delete the now-unused `angleFromRot` private function.

4. In `simulate`:
   - Change signature default: `maxTicks: Int = 500`.
   - Change `var vel = launch.velocity.add(player.deltaMovement)` to `var vel = launch.velocity` (meteor only adds player motion behind an "accurate" setting that defaults off; vanilla adds it server-side but the visual difference misleads more than helps).
   - Replace the entity predicate with meteor's: `{ e -> !e.isSpectator && e.isAlive && e.isPickable && e !== player }` (drop the manual `Projectile`/`ItemEntity`/`ExperienceOrb` exclusions — `isPickable` covers them). Remove the now-unused imports (`Projectile`, `ItemEntity`, `ExperienceOrb`).
   - Change the void cutoff from `pos.y < level.minY - 120` to `pos.y < level.minY` (meteor stops at world floor — the line no longer plunges 120 blocks past the island bottom).

- [ ] **Step 2: Rework `TrajectoryPreview.kt` rendering**

Replace the `init` block body and `typeEnabled` as follows (changes: skip the first 3 sim points like meteor's `ignore-rendering-first-ticks` so the line doesn't visually start at your body; entity hit now recolors the line instead of drawing a wireframe box; block-impact marker becomes a thin quad aligned to the hit face instead of a half-buried cube):

```kotlin
    private const val IGNORE_FIRST_POINTS = 3 // meteor's ignore-rendering-first-ticks default

    init {
        on<RenderEvent.Extract> {
            val player = mc.player ?: return@on
            val stack = player.mainHandItem
            if (!typeEnabled(stack)) return@on
            val partialTicks = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
            val launch = ProjectileSim.launchFor(stack, player, partialTicks) ?: return@on
            val result = ProjectileSim.simulate(player, launch)

            val visiblePoints = if (result.points.size > IGNORE_FIRST_POINTS) result.points.drop(IGNORE_FIRST_POINTS) else emptyList()
            if (visiblePoints.size >= 2) {
                val pathColor = if (result.entityHit != null) entityColor else lineColor
                drawLine(visiblePoints, pathColor, depth = !throughWalls, thickness = lineWidth)
            }

            result.blockHit?.let { hit ->
                drawFilledBox(impactQuad(hit), impactColor, depth = !throughWalls)
            }
        }
    }

    // Thin 0.5×0.5 quad lying on the hit face (meteor draws a flat hit quad, not a cube).
    private fun impactQuad(hit: BlockHitResult): AABB {
        val c = hit.location
        return when (hit.direction.axis) {
            Direction.Axis.Y -> AABB(c.x - 0.25, c.y - 0.01, c.z - 0.25, c.x + 0.25, c.y + 0.01, c.z + 0.25)
            Direction.Axis.X -> AABB(c.x - 0.01, c.y - 0.25, c.z - 0.25, c.x + 0.01, c.y + 0.25, c.z + 0.25)
            Direction.Axis.Z -> AABB(c.x - 0.25, c.y - 0.25, c.z - 0.01, c.x + 0.25, c.y + 0.25, c.z + 0.01)
        }
    }
```

Supporting changes in the same file:
- Add imports: `net.minecraft.core.Direction`, `net.minecraft.world.phys.BlockHitResult`.
- Remove import `com.odtheking.odin.utils.render.drawWireFrameBox` (no longer used).
- Change the `entityColor` setting description to: `"Line color when the projectile would hit an entity."` (name stays "Entity Hit Color" so existing configs keep working).
- `typeEnabled` unchanged.
- Kotlin note: `private const val` must live in a `companion object` or top level — since `TrajectoryPreview` is an `object`, a plain `private const val IGNORE_FIRST_POINTS = 3` declared directly in the object body is legal. Place it above the settings.

- [ ] **Step 3: Verify it compiles**

Run: `JAVA_HOME=/home/girish/.jdks/jdk-25.0.3+9 ./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/odtheking/odin/utils/ProjectileSim.kt src/main/kotlin/com/odtheking/odin/features/impl/render/TrajectoryPreview.kt
git commit -m "Reworked trajectory preview to meteor-style rendering and simulation"
```

---

### Task 5: Vanilla entity collision + visibility fixes (user feedback round 2)

User feedback: trajectory line passes through players (visible going behind them, then depth-hidden = "disappears"), and the line reads too thin. Fix by porting meteor's entity-collision exactly — vanilla `ProjectileUtil.getEntityHitResult` with meteor's growing tolerance margin — and improving hit visibility.

**Files:**
- Modify: `src/main/kotlin/com/odtheking/odin/utils/ProjectileSim.kt`
- Modify: `src/main/kotlin/com/odtheking/odin/features/impl/render/TrajectoryPreview.kt`

**Interfaces:**
- Public API unchanged: `launchFor`, `simulate`, `Launch`, `SimResult(points, blockHit, entityHit)`.

- [ ] **Step 1: Replace hand-rolled entity clip in `ProjectileSim.simulate` with `ProjectileUtil`**

Verified against the mapped 26.1.2 jar: `net.minecraft.world.entity.projectile.ProjectileUtil` has
`public static EntityHitResult getEntityHitResult(Level, Entity, Vec3, Vec3, AABB, Predicate<Entity>, float)`.

In `ProjectileSim.kt`:

1. Add import `net.minecraft.world.entity.projectile.ProjectileUtil`. Remove the now-unused `Entity` import ONLY if nothing else references it (`SimResult.entityHit` still does — keep it).
2. Change the loop from `repeat(maxTicks) {` to `for (tick in 0 until maxTicks) {` (the tolerance margin needs the tick index; closing brace count unchanged).
3. Replace the whole entity-search block (the `var closestEntity ... level.getEntitiesOfClass(...).forEach { ... }` section) with:

```kotlin
            // Meteor-style entity collision: vanilla ProjectileUtil with a tolerance margin
            // that grows over flight time (meteor's getToleranceMargin).
            val margin = Mth.clamp((tick - 2) / 20f, 0f, 0.3f)
            val entityHit = ProjectileUtil.getEntityHitResult(
                level, player, prevPos, pos,
                AABB(prevPos, pos).inflate(1.0),
                { e -> !e.isSpectator && e.isAlive && e.isPickable && e !== player },
                margin
            )
            val entityDistSq = entityHit?.let { prevPos.distanceToSqr(it.location) } ?: Double.MAX_VALUE
```

4. Update the two hit checks that followed to use the new names (block hit wins only when strictly closer than the entity hit, as before):

```kotlin
            if (blockHit.type != HitResult.Type.MISS && prevPos.distanceToSqr(blockHit.location) < entityDistSq) {
                points.add(blockHit.location)
                return SimResult(points, blockHit, null)
            }
            entityHit?.let { hit ->
                points.add(hit.location)
                return SimResult(points, null, hit.entity)
            }
```

5. Delete the now-unused imports for `net.minecraft.world.entity.projectile.Projectile` — wait: that import was already removed in Task 4. After this change nothing but `SimResult`/the predicate uses `Entity`; keep that import. No other import changes should be needed; let the compiler confirm.

- [ ] **Step 2: Visibility fixes in `TrajectoryPreview.kt`**

1. Widen the line-width setting (name unchanged so existing configs keep working; new installs default thicker):

```kotlin
    private val lineWidth by NumberSetting("Line Width", 5f, 1, 10, 0.5, desc = "Thickness of the trajectory line.")
```

2. After the `result.blockHit?.let { ... }` block, add an entity-hit marker so a predicted entity hit stays visible even where the player model occludes the line end (the sim appends the exact hit location as the last path point):

```kotlin
            if (result.entityHit != null && result.points.isNotEmpty()) {
                drawFilledBox(AABB.ofSize(result.points.last(), 0.25, 0.25, 0.25), entityColor, depth = !throughWalls)
            }
```

- [ ] **Step 3: Verify it compiles**

Run: `JAVA_HOME=/home/girish/.jdks/jdk-25.0.3+9 ./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/odtheking/odin/utils/ProjectileSim.kt src/main/kotlin/com/odtheking/odin/features/impl/render/TrajectoryPreview.kt
git commit -m "Fixed trajectory entity collision using vanilla ProjectileUtil and improved hit visibility"
```

---

### Task 6: Line render stability (user feedback round 3)

User feedback: the trajectory line "twists" whenever the camera rotates. Root cause: `PrimitiveRenderer.renderVector` (`RenderUtils.kt`) passes the **unnormalized** segment direction as the line-shader normal. Minecraft's `rendertype_lines` shader computes the screen-space line quad from `Position + Normal` with perspective division — vanilla always submits unit normals; magnitude-carrying normals skew that math and each segment's screen-space quad rotates independently as the view changes. Fix the normal, and add a camera-independent "Dots" style as a guaranteed-stable option (meteor's render-position-boxes).

**Files:**
- Modify: `src/main/kotlin/com/odtheking/odin/utils/render/RenderUtils.kt` (renderVector — repo-wide line fix)
- Modify: `src/main/kotlin/com/odtheking/odin/features/impl/render/TrajectoryPreview.kt` (render style selector)

**Interfaces:**
- `ProjectileSim` untouched. `drawLine` signature unchanged — only the normal math inside `renderVector` changes.

- [ ] **Step 1: Normalize line normals in `renderVector`**

In `RenderUtils.kt`, `renderVector` currently computes:

```kotlin
        val nx = direction.x.toFloat()
        val ny = direction.y.toFloat()
        val nz = direction.z.toFloat()
```

Replace with (normalize, guarding the degenerate zero-length segment):

```kotlin
        val len = direction.length()
        val inv = if (len > 1.0E-6) (1.0 / len).toFloat() else 0f
        val nx = direction.x.toFloat() * inv
        val ny = direction.y.toFloat() * inv
        val nz = direction.z.toFloat() * inv
```

Both `setNormal` calls below already use `nx/ny/nz` — no other change. This matches the unit-normal contract vanilla's own line rendering follows and affects every `drawLine`/`drawTracer` user in the mod (an improvement, not a regression — direction is unchanged, only magnitude).

- [ ] **Step 2: Add "Render Style" selector to `TrajectoryPreview.kt`**

1. Add import `com.odtheking.odin.clickgui.settings.impl.SelectorSetting`.
2. Add the setting directly above the `lineWidth` declaration:

```kotlin
    private val renderStyle by SelectorSetting("Render Style", "Line", listOf("Line", "Dots", "Both"), desc = "Trajectory drawn as a line, a dotted trail, or both.")
```

3. In the `on<RenderEvent.Extract>` block, replace the line-drawing section:

```kotlin
            val visiblePoints = if (result.points.size > IGNORE_FIRST_POINTS) result.points.drop(IGNORE_FIRST_POINTS) else emptyList()
            if (visiblePoints.size >= 2) {
                val pathColor = if (result.entityHit != null) entityColor else lineColor
                drawLine(visiblePoints, pathColor, depth = !throughWalls, thickness = lineWidth)
            }
```

with:

```kotlin
            val visiblePoints = if (result.points.size > IGNORE_FIRST_POINTS) result.points.drop(IGNORE_FIRST_POINTS) else emptyList()
            if (visiblePoints.size >= 2) {
                val pathColor = if (result.entityHit != null) entityColor else lineColor
                if (renderStyle != 1) drawLine(visiblePoints, pathColor, depth = !throughWalls, thickness = lineWidth)
                if (renderStyle != 0) {
                    val dotSize = (lineWidth * 0.02).toDouble()
                    visiblePoints.forEach { point ->
                        drawFilledBox(AABB.ofSize(point, dotSize, dotSize, dotSize), pathColor, depth = !throughWalls)
                    }
                }
            }
```

Note: `SelectorSetting`'s delegated value is the selected **index** (`Int`): 0 = Line, 1 = Dots, 2 = Both (established by `RenderTest.kt`'s `boxStyle` usage feeding `drawStyledBox(style: Int)`).

- [ ] **Step 3: Verify it compiles**

Run: `JAVA_HOME=/home/girish/.jdks/jdk-25.0.3+9 ./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/odtheking/odin/utils/render/RenderUtils.kt src/main/kotlin/com/odtheking/odin/features/impl/render/TrajectoryPreview.kt
git commit -m "Fixed line render twisting by normalizing line-shader normals and added trajectory render styles"
```

---

### Task 7: Sphere dots + color defaults (user feedback round 4)

User feedback: dots should be spheres, not cubes; default colors change to line = RGB(128, 0, 255), impact = white, entity hit = red. Requires a filled-sphere primitive in the shared render layer (none exists), modeled exactly on the existing filled-box path (`BoxData` → `renderQueuedFilledBoxes` → `PrimitiveRenderer.addChainedFilledBoxVertices`, chained TRIANGLE_STRIP with duplicated lead/tail vertices for degenerate stitching).

**Files:**
- Modify: `src/main/kotlin/com/odtheking/odin/utils/render/RenderUtils.kt`
- Modify: `src/main/kotlin/com/odtheking/odin/features/impl/render/TrajectoryPreview.kt`

**Interfaces:**
- Produces: `RenderEvent.Extract.drawSphere(center: Vec3, radius: Float, color: Color, depth: Boolean = false)` — reusable by any module.
- `ProjectileSim` untouched.

- [ ] **Step 1: Add sphere primitive to `RenderUtils.kt`**

1. Next to `internal data class BoxData(...)`, add:

```kotlin
internal data class SphereData(val center: Vec3, val radius: Float, val r: Float, val g: Float, val b: Float, val a: Float, val depth: Boolean)
```

2. In `RenderConsumer`: add `internal val spheres = ObjectArrayList<SphereData>()` beside `filledBoxes`, and `spheres.clear()` inside `clear()`.

3. In `RenderBatchManager`'s `init` block, directly after the `renderQueuedFilledBoxes(...)` line, add:

```kotlin
            poseStack.renderQueuedSpheres(renderConsumer.spheres, bufferSource)
```

4. Beside `BoxData.filledRenderType()`, add:

```kotlin
private fun SphereData.filledRenderType() = if (depth) RenderTypes.debugFilledBox() else CustomRenderType.QUADS_ESP
```

5. Beside `renderQueuedFilledBoxes`, add:

```kotlin
private fun PoseStack.renderQueuedSpheres(consumer: List<SphereData>, bufferSource: MultiBufferSource.BufferSource) {
    if (consumer.isEmpty()) return
    val last = this.last()

    for (sphere in consumer) {
        val buffer = bufferSource.getBuffer(sphere.filledRenderType())
        PrimitiveRenderer.renderSphere(
            last, buffer,
            sphere.center.x.toFloat(), sphere.center.y.toFloat(), sphere.center.z.toFloat(),
            sphere.radius, sphere.r, sphere.g, sphere.b, sphere.a
        )
    }
}
```

6. Public extension, next to `drawFilledBox`:

```kotlin
fun RenderEvent.Extract.drawSphere(center: Vec3, radius: Float, color: Color, depth: Boolean = false) {
    consumer.spheres.add(
        SphereData(center, radius, color.redFloat, color.greenFloat, color.blueFloat, color.alphaFloat, depth)
    )
}
```

7. In the `PrimitiveRenderer` object (same object that holds `renderVector`), add a UV-sphere emitted as one chained triangle strip — duplicated first and last vertices plus degenerate bridges between latitude bands, matching the chained-strip convention `addChainedFilledBoxVertices` uses:

```kotlin
    fun renderSphere(
        pose: PoseStack.Pose,
        buffer: VertexConsumer,
        cx: Float, cy: Float, cz: Float,
        radius: Float,
        r: Float, g: Float, b: Float, a: Float,
        stacks: Int = 6,
        sectors: Int = 10
    ) {
        var firstVertex = true
        var lastX = 0f
        var lastY = 0f
        var lastZ = 0f

        fun vertex(x: Float, y: Float, z: Float) {
            buffer.addVertex(pose, x, y, z).setColor(r, g, b, a)
            lastX = x
            lastY = y
            lastZ = z
        }

        for (stack in 0 until stacks) {
            val t1 = (Math.PI * stack / stacks).toFloat()
            val t2 = (Math.PI * (stack + 1) / stacks).toFloat()
            val sin1 = sin(t1)
            val cos1 = cos(t1)
            val sin2 = sin(t2)
            val cos2 = cos(t2)

            for (sector in 0..sectors) {
                val phi = (2.0 * Math.PI * sector / sectors).toFloat()
                val sp = sin(phi)
                val cp = cos(phi)
                val xa = cx + radius * sin2 * cp
                val ya = cy + radius * cos2
                val za = cz + radius * sin2 * sp
                val xb = cx + radius * sin1 * cp
                val yb = cy + radius * cos1
                val zb = cz + radius * sin1 * sp

                if (sector == 0) {
                    if (firstVertex) {
                        vertex(xa, ya, za) // chained-strip leading duplicate
                        firstVertex = false
                    } else {
                        vertex(lastX, lastY, lastZ) // degenerate bridge out of previous band
                        vertex(xa, ya, za)          // degenerate bridge into this band
                    }
                }
                vertex(xa, ya, za)
                vertex(xb, yb, zb)
            }
        }
        vertex(lastX, lastY, lastZ) // chained-strip trailing duplicate
    }
```

If `sin`/`cos` are unresolved in `PrimitiveRenderer`'s scope, add `import kotlin.math.cos` / `import kotlin.math.sin` at the top of the file (the file very likely has them already — `drawCylinder` uses both).

- [ ] **Step 2: `TrajectoryPreview.kt` — sphere dots + new color defaults**

1. Change the three color setting defaults (names unchanged — NOTE: existing saved configs override defaults; only fresh configs see these):

```kotlin
    private val lineColor by ColorSetting("Line Color", Color(128, 0, 255), true, desc = "Color of the trajectory line.")
    private val impactColor by ColorSetting("Impact Color", Colors.WHITE, true, desc = "Color of the impact marker.")
    private val entityColor by ColorSetting("Entity Hit Color", Colors.MINECRAFT_RED, true, desc = "Line color when the projectile would hit an entity.")
```

Add import `com.odtheking.odin.utils.Color` (keep the `Colors` import).

2. Replace the dots block inside the render handler:

```kotlin
                if (renderStyle != 0) {
                    val dotSize = (lineWidth * 0.02).toDouble()
                    visiblePoints.forEach { point ->
                        drawFilledBox(AABB.ofSize(point, dotSize, dotSize, dotSize), pathColor, depth = !throughWalls)
                    }
                }
```

with:

```kotlin
                if (renderStyle != 0) {
                    val dotRadius = lineWidth * 0.015f
                    visiblePoints.forEach { point ->
                        drawSphere(point, dotRadius, pathColor, depth = !throughWalls)
                    }
                }
```

(This also removes the redundant `.toDouble()` the compiler warned about in Task 6.)

3. Add import `com.odtheking.odin.utils.render.drawSphere`. Keep `drawFilledBox` and `AABB` imports — still used by the impact quad and entity-hit marker.

- [ ] **Step 3: Verify it compiles**

Run: `JAVA_HOME=/home/girish/.jdks/jdk-25.0.3+9 ./gradlew build --console=plain`
Expected: `BUILD SUCCESSFUL`, and the Task 6 "Redundant call of conversion method" warning is gone.

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/odtheking/odin/utils/render/RenderUtils.kt src/main/kotlin/com/odtheking/odin/features/impl/render/TrajectoryPreview.kt
git commit -m "Added sphere rendering for trajectory dots and updated default colors"
```
