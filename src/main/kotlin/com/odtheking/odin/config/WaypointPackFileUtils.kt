package com.odtheking.odin.config

import com.google.gson.reflect.TypeToken
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import com.odtheking.odin.utils.modMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.minecraft.util.FileUtil
import java.io.File

private typealias PackMap = MutableMap<String, MutableList<DungeonWaypoint>>

object WaypointPackFileUtils {

    private val legacyConfigFile = File(mc.gameDirectory, "config/odin/dungeon-waypoint-config.json")
    val packsFolder = File(mc.gameDirectory, "config/odin/dungeon-waypoints").apply { mkdirs() }
    private val packType = object : TypeToken<PackMap>() {}.type

    init {
        if (listPackNames().isEmpty()) runCatching {
            val content = legacyConfigFile.takeIf(File::exists)?.readText() ?: DungeonWaypointConfig.gson.toJson(emptyPack())
            packFile("default").writeText(content)
        }.onFailure { it.printStackTrace() }
    }

    private fun packFile(name: String) = File(packsFolder, "$name.json")
    private fun emptyPack(): PackMap = mutableMapOf()

    fun listPackNames(): List<String> =
        packsFolder.listFiles { f -> f.extension == "json" }?.map { it.nameWithoutExtension }?.sorted() ?: emptyList()

    suspend fun loadPack(packName: String): PackMap = withContext(Dispatchers.IO) {
        runCatching {
            packFile(packName).takeIf(File::exists)?.readText()
                ?.let { DungeonWaypointConfig.gson.fromJson<PackMap>(it, packType) } ?: emptyPack()
        }.getOrElse { it.printStackTrace(); emptyPack() }
    }

    suspend fun savePack(packName: String, waypoints: PackMap) = withContext(Dispatchers.IO) {
        runCatching { packFile(packName).writeText(DungeonWaypointConfig.gson.toJson(waypoints)) }.onFailure { it.printStackTrace() }
    }

    suspend fun createPack(packName: String): Boolean = withContext(Dispatchers.IO) {
        val file = packFile(packName)
        when {
            !isValidPackName(packName) -> fail("Invalid pack name!")
            file.exists() -> fail("Pack '$packName' already exists!")
            else -> attempt("Created new pack: $packName", "Failed to create pack!") {
                file.writeText(DungeonWaypointConfig.gson.toJson(emptyPack()))
            }
        }
    }

    suspend fun deletePack(packName: String): Boolean = withContext(Dispatchers.IO) {
        val file = packFile(packName)
        when {
            !file.exists() -> fail("Pack '$packName' does not exist!")
            else -> attempt("Deleted pack: $packName", "Failed to delete pack!") {
                check(file.delete()) { "Failed to delete ${file.path}" }
            }
        }
    }

    suspend fun renamePack(oldName: String, newName: String): Boolean = withContext(Dispatchers.IO) {
        val oldFile = packFile(oldName)
        val newFile = packFile(newName)
        when {
            !isValidPackName(newName) -> fail("Invalid pack name!")
            !oldFile.exists() -> fail("Pack '$oldName' does not exist!")
            newFile.exists() -> fail("Pack '$newName' already exists!")
            else -> attempt("Renamed pack from '$oldName' to '$newName'", "Failed to rename pack!") {
                check(oldFile.renameTo(newFile)) { "Failed to rename ${oldFile.path}" }
            }
        }
    }

    private fun fail(message: String): Boolean {
        modMessage("§c$message")
        return false
    }

    private fun attempt(successMessage: String, failureMessage: String, action: () -> Unit): Boolean =
        runCatching(action).fold(
            onSuccess = { modMessage("§a$successMessage"); true },
            onFailure = { it.printStackTrace(); fail(failureMessage) }
        )

    private fun isValidPackName(name: String) =
        name.isNotBlank() && FileUtil.sanitizeName(name) == name && FileUtil.isPathPartPortable(name)
}
