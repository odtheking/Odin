package com.odtheking.odin.config

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.OdinMod.mc
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

    private val legacyConfigFile = File(mc.gameDirectory, "config/odin/dungeon-waypoint-config.json")
    val packsFolder = File(mc.gameDirectory, "config/odin/dungeon-waypoints").apply { mkdirs() }

    init {
        migrateLegacyConfigIfNeeded()
    }

    private fun migrateLegacyConfigIfNeeded() {
        if (!legacyConfigFile.exists() || packsFolder.listFiles()?.isNotEmpty() == true) return
        runCatching {
            val target = File(packsFolder, "default.json")
            target.writeText(legacyConfigFile.readText())
        }.onFailure { it.printStackTrace() }
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
                modMessage("§cInvalid pack name! Use only letters, numbers, spaces, hyphens, and underscores.")
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
                if (!packFile(packName).delete()) error("Failed to delete ${packFile(packName).path}")
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
                modMessage("§cInvalid pack name! Use only letters, numbers, spaces, hyphens, and underscores.")
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
                if (!packFile(oldName).renameTo(packFile(newName))) error("Failed to rename ${packFile(oldName).path}")
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


    private fun isValidPackName(name: String) =
        name.isNotBlank() && name.all { it.isLetterOrDigit() || it == '-' || it == '_' || it == ' ' }
}
