package com.odtheking.odin.config

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import com.odtheking.odin.utils.modMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.minecraft.world.phys.AABB
import java.io.File

object WaypointPackFileUtils {

    data class WaypointPack(
        val name: String,
        val file: File,
        val waypoints: MutableMap<String, MutableList<DungeonWaypoint>>
    )

    private val gson = GsonBuilder()
        .registerTypeAdapter(AABB::class.java, DungeonWaypointConfig.AABBSerializer())
        .registerTypeAdapter(DungeonWaypoint::class.java, DungeonWaypointConfig.DungeonWaypointDeserializer())
        .setPrettyPrinting()
        .create()

    private val packType =
        object : TypeToken<MutableMap<String, MutableList<DungeonWaypoint>>>() {}.type

    private val configFile = File(mc.gameDirectory, "config/odin/dungeon-waypoint-config.json")
    val packsFolder = File(mc.gameDirectory, "config/odin/dungeon-waypoints").apply { mkdirs() }

    init {
        copyLegacyConfigIfExists()
    }

    private fun copyLegacyConfigIfExists() {
        if (!configFile.exists()) return
        val target = File(packsFolder, configFile.name)
        if (target.exists()) return
        runCatching { configFile.copyTo(target) }.onFailure { it.printStackTrace() }
    }

    private fun packFile(name: String) = File(packsFolder, "$name.json")
    private fun emptyPack() = mutableMapOf<String, MutableList<DungeonWaypoint>>()

    suspend fun loadPack(packName: String) = withContext(Dispatchers.IO) {
        runCatching {
            packFile(packName)
                .takeIf(File::exists)
                ?.readText()
                ?.let { gson.fromJson(it, packType) }
                ?: emptyPack()
        }.getOrElse {
            it.printStackTrace()
            emptyPack()
        }
    }

    suspend fun savePack(packName: String, waypoints: MutableMap<String, MutableList<DungeonWaypoint>>) =
        withContext(Dispatchers.IO) {
            runCatching {
                packFile(packName).writeText(gson.toJson(waypoints))
            }.onFailure { it.printStackTrace() }
        }

    suspend fun createPack(packName: String) = withContext(Dispatchers.IO) {
        when {
            !isValidPackName(packName) -> {
                modMessage("§cInvalid pack name! Use only letters, numbers, hyphens, and underscores.")
                false
            }
            packFile(packName).exists() -> {
                modMessage("§cPack '$packName' already exists!")
                false
            }
            else -> runCatching {
                packFile(packName).writeText(gson.toJson(emptyPack()))
                modMessage("§aCreated new pack: $packName")
                true
            }.getOrElse {
                it.printStackTrace()
                modMessage("§cFailed to create pack!")
                false
            }
        }
    }

    suspend fun deletePack(packName: String) = withContext(Dispatchers.IO) {
        when {
            !packFile(packName).exists() -> {
                modMessage("§cPack '$packName' does not exist!")
                false
            }
            else -> runCatching {
                packFile(packName).delete()
                updateActivePacks { it.remove(packName) }

                val remainingPacks = getAllPacks()
                if (DungeonWaypoints.activeEditPack == packName)
                    DungeonWaypoints.activeEditPack = remainingPacks.firstOrNull()?.name ?: ""

                modMessage("§aDeleted pack: $packName")
                true
            }.getOrElse {
                it.printStackTrace()
                modMessage("§cFailed to delete pack!")
                false
            }
        }
    }

    suspend fun renamePack(oldName: String, newName: String) = withContext(Dispatchers.IO) {
        when {
            !isValidPackName(newName) -> {
                modMessage("§cInvalid pack name! Use only letters, numbers, hyphens, and underscores.")
                false
            }
            !packFile(oldName).exists() -> {
                modMessage("§cPack '$oldName' does not exist!")
                false
            }
            packFile(newName).exists() -> {
                modMessage("§cPack '$newName' already exists!")
                false
            }
            else -> runCatching {
                packFile(oldName).renameTo(packFile(newName))

                updateActivePacks {
                    val idx = it.indexOf(oldName)
                    if (idx != -1) it[idx] = newName
                }

                if (DungeonWaypoints.activeEditPack == oldName) DungeonWaypoints.activeEditPack = newName
                modMessage("§aRenamed pack from '$oldName' to '$newName'")
                true
            }.getOrElse {
                it.printStackTrace()
                modMessage("§cFailed to rename pack!")
                false
            }
        }
    }

    suspend fun getAllPacks() = withContext(Dispatchers.IO) {
        packsFolder.listFiles { f -> f.extension == "json" }
            ?.mapNotNull { file ->
                runCatching {
                    WaypointPack(file.nameWithoutExtension, file, gson.fromJson(file.readText(), packType))
                }.getOrElse {
                    println("Failed to load waypoint pack: ${file.name}")
                    it.printStackTrace()
                    null
                }
            } ?: emptyList()
    }

    suspend fun mergeActivePacks(packNames: List<String>) = withContext(Dispatchers.IO) {
        val merged = mutableMapOf<String, MutableList<DungeonWaypoint>>()
        val packs = getAllPacks().associateBy { it.name }

        for (name in packNames) {
            packs[name]?.waypoints?.forEach { (room, points) ->
                merged.getOrPut(room) { mutableListOf() }.addAll(points)
            }
        }
        merged
    }

    private fun updateActivePacks(block: (MutableList<String>) -> Unit) {
        val packs = DungeonWaypoints.activePacks.split(",").filter { it.isNotBlank() }.toMutableList()
        block(packs)
        DungeonWaypoints.activePacks = packs.joinToString(",")
    }

    private fun isValidPackName(name: String) =
        name.isNotEmpty() && name.all { it.isLetterOrDigit() || it == '-' || it == '_' }
}
