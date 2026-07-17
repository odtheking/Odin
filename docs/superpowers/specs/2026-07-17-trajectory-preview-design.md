# Trajectory Preview Module — Design

**Date:** 2026-07-17
**Status:** Approved by user

## Purpose

New Odin module that renders a predicted trajectory line for the projectile the player is holding (arrow, ender pearl, snowball/egg, fishing rod, splash potion), plus an impact marker on the block it would hit and a highlight on any entity in the path. Client-side only, purely visual. Inspired by [ProjectilesTrajectoryPreview](https://github.com/maDU59/ProjectilesTrajectoryPreview) (Fabric, MIT-style reference for physics constants and simulation structure).

## Architecture

Two units (approach B — shared physics util + thin module):

### 1. `src/main/kotlin/com/odtheking/odin/utils/ProjectileSim.kt`

Pure physics. No settings, no rendering. Reusable by other modules (e.g. `PearlWaypoints` later).

- `enum class ProjectileType(val speed: Double, val gravity: Double, val drag: Double, val waterDrag: Double, val order: StepOrder, val pitchOffset: Float = 0f)`
  | Type | speed | gravity | drag | waterDrag | order | notes |
  |---|---|---|---|---|---|---|
  | `ARROW` | 3.0 × charge | 0.05 | 0.99 | 0.6 | PDG | charge from `BowItem.getPowerForTime`; 1.0 when not drawing |
  | `THROWN` (pearl/snowball/egg) | 1.5 | 0.03 | 0.99 | 0.8 | GDP | |
  | `FISHING_ROD` | vanilla bobber launch vector (yaw/pitch trig from `FishingBobberEntity` spawn code, ~1.5 magnitude) | 0.03 | 0.92 | 0.92 | GPD | water collision enabled |
  | `POTION` | 0.5 | 0.05 | 0.99 | 0.8 | GDP | −20° pitch offset (thrown upward) |
- `enum class StepOrder { PDG, GDP, GPD }` — per-tick operation order (P=pos+=vel, D=vel*=drag, G=vel.y−=gravity).
- `data class SimResult(val points: List<Vec3>, val blockHit: BlockHitResult?, val entityHit: Entity?)`
- `fun simulate(player: Player, type: ProjectileType, speedMultiplier: Double = 1.0): SimResult`
  - Start: `player.getEyePosition(partialTicks) - (0, 0.1, 0)`; potion applies pitch offset to direction.
  - Initial velocity: `viewVector × (type.speed × speedMultiplier) + player.deltaMovement`.
  - Loop, max 200 ticks: apply steps in `type.order`; per segment run block raytrace (`level.clip`, `ClipContext.Block.COLLIDER`) and entity clip (`getEntitiesOfClass` in inflated segment AABB, exclude spectators/dead/self/projectiles, closest wins); if segment passes through water, use `waterDrag` next tick.
  - Stop on block hit, entity hit, or y below world floor − 120.
- `fun heldProjectile(stack: ItemStack, player: Player): Pair<ProjectileType, Double>?` — maps held item → type + speed multiplier. Bow: if `player.isUsingItem` use real charge, else assume full charge (Skyblock shortbows always fire full power, so holding-unpulled = full-charge preview is correct for them).

### 2. `src/main/kotlin/com/odtheking/odin/features/impl/render/TrajectoryPreview.kt`

`object TrajectoryPreview : Module(name = "Trajectory Preview", description = ...)` — category inferred RENDER from package. Registered in `ModuleManager.kt` render group (~line 62-82).

`init { on<RenderEvent.Extract> { ... } }` (rev 2 — meteor-style, per user feedback after in-game testing):
1. Held main-hand item → `launchFor` → null? skip.
2. Per-type toggle off? skip.
3. `ProjectileSim.simulate(...)` (500-tick cap, meteor's default).
4. Skip the first 3 sim points (meteor's `ignore-rendering-first-ticks`) so the line doesn't visually start at the player's body, then `drawLine(points, pathColor, depth = !throughWalls, thickness = lineWidth)` — where `pathColor` = Entity Hit Color when the predicted hit is an entity, else Line Color.
5. Block hit → thin 0.5×0.5 filled quad lying on the hit face (not a cube — a centered cube half-sinks into the floor).
6. Entity hit → no box; the line-color swap in step 4 is the indicator.

## Settings

| Setting | Type | Default |
|---|---|---|
| Arrows | BooleanSetting | on |
| Ender Pearls | BooleanSetting | on |
| Snowballs/Eggs | BooleanSetting | off |
| Fishing Rod | BooleanSetting | off |
| Potions | BooleanSetting | off |
| Line Color | ColorSetting | `Colors.WHITE`, alpha allowed |
| Impact Color | ColorSetting | `Colors.MINECRAFT_RED`, alpha allowed |
| Entity Hit Color | ColorSetting | `Colors.MINECRAFT_YELLOW`, alpha allowed — line color when predicted hit is an entity |
| Line Width | NumberSetting | 3 (1–5) |
| Through Walls | BooleanSetting | off |

## Edge cases / constraints

- Null player/level → return early each frame.
- Vanilla adds per-shot random spread; the preview is the center line. Noted in module description.
- Hypixel may deviate slightly from vanilla physics server-side; accepted, visual aid only.
- Crossbows out of scope v1 (rare in Skyblock); adding later = one enum row + item mapping.
- Offhand ignored v1.
- Performance: ≤500 iterations/frame with cheap clips (meteor's default budget); no caching needed. Sim stops at world `minY` (no plunge past floating-island bottoms).
- Launch math follows meteor-client's `ProjectileEntitySimulator`: yaw/pitch trig direction with per-item pitch offset (potions −20°), no player-motion inheritance, `isPickable` entity filter, unpulled/barely-pulled bow forced to full charge.

## Testing

No unit test infra in repo. Verification:
1. `./gradlew build` compiles.
2. In-game: pearl thrown at wall lands at line endpoint; bow drawn shows arc growing with charge; unpulled bow shows full-power arc; entity in path gets highlighted; toggles/colors respond.
