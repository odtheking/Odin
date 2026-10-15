package com.odtheking.odin.utils.network.hypixelapi

import com.odtheking.odin.features.impl.render.ClickGUIModule.hypixelApiUrl
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import java.util.concurrent.ConcurrentHashMap

object RequestUtils {

    private class TtlCache<T : Any>(private val ttlMs: Long = 10 * 60 * 1000L) {
        private class Entry<T>(val value: T, val timestamp: Long = System.currentTimeMillis())

        private val entries = ConcurrentHashMap<String, Entry<T>>()

        operator fun get(name: String): T? {
            val now = System.currentTimeMillis()
            entries.values.removeIf { now - it.timestamp >= ttlMs }
            return entries[name.lowercase()]?.value
        }

        operator fun set(name: String, value: T) {
            entries[name.lowercase()] = Entry(value)
        }
    }

    private val uuidCache = TtlCache<UuidData>()
    private val playerCache = TtlCache<HypixelData.PlayerInfo>()

    private fun getServer(endpoint: EndPoint, uuid: String): String =
        hypixelApiUrl + endpoint.name.lowercase() + "/" + uuid

    suspend fun getProfile(name: String): Result<HypixelData.PlayerInfo> {
        playerCache[name]?.let { return Result.success(it) }
        val uuidData = getUuid(name).getOrElse { return Result.failure(it) }

        return fetchJson<HypixelData.ProfilesData>(getServer(EndPoint.GET, uuidData.id)).mapCatching { data ->
            data.cause?.let { throw Exception("Failed to get hypixel data: $it") }
            HypixelData.PlayerInfo(data, uuidData.id, uuidData.name).also {
                if (data.profiles.isNotEmpty()) playerCache[name] = it
            }
        }
    }

    suspend fun getUuid(name: String): Result<UuidData> {
        uuidCache[name]?.let { return Result.success(it) }

        return fetchJson<UuidData>("https://api.minecraftservices.com/minecraft/profile/lookup/name/$name")
            .onSuccess { uuidCache[name] = it }
    }

    suspend fun pullSecrets(name: String): Result<Long> {
        val uuidData = getUuid(name).getOrElse { return Result.failure(it) }
        return fetchJson<Long>(getServer(EndPoint.SECRETS, uuidData.id))
    }

    private enum class EndPoint { SECRETS, GET }
    data class UuidData(val name: String, val id: String)
}
