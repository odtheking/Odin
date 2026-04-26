package com.odtheking.odin.utils.network.hypixelapi

import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.features.impl.render.ClickGUIModule.hypixelApiUrl
import com.odtheking.odin.utils.network.WebUtils.fetchJson

object RequestUtils {

    private const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutes

    private data class CacheEntry<T>(
        val value: T,
        val timestamp: Long
    )

    private val uuidCache = HashMap<String, CacheEntry<UuidData>>()
    private val playerCache = HashMap<String, CacheEntry<HypixelData.PlayerInfo>>()

    private fun getServer(endpoint: EndPoint, uuid: String): String =
        hypixelApiUrl + endpoint.name.lowercase() + "/" + uuid

    private fun <T> MutableMap<String, CacheEntry<T>>.evictExpired() {
        val now = System.currentTimeMillis()
        entries.removeIf { now - it.value.timestamp >= CACHE_TTL_MS }
    }

    private fun getPlayerFromCache(name: String): HypixelData.PlayerInfo? {
        val key = name.lowercase()
        playerCache.evictExpired()
        return playerCache[key]?.value
    }

    private fun putPlayerInCache(info: HypixelData.PlayerInfo) {
        if (info.profileData.profiles.isEmpty()) {
            logger.info("Refusing to cache empty profile!")
            return
        }

        playerCache[info.name.lowercase()] = CacheEntry(info, System.currentTimeMillis())

        logger.info("Cached ${info.name}. Cache size: ${playerCache.size}")
    }

    suspend fun getProfile(name: String): Result<HypixelData.PlayerInfo> {
        getPlayerFromCache(name)?.let { return Result.success(it) }
        val uuidData = getUuid(name).getOrElse { return Result.failure(Exception(it.cause)) }

        return fetchJson<HypixelData.ProfilesData>(getServer(EndPoint.GET, uuidData.id)).map { data ->
            data.failed?.let { return Result.failure(Exception("Failed to get hypixel data: $it")) }

            HypixelData.PlayerInfo(data, uuidData.id, uuidData.name).also(::putPlayerInCache)
        }
    }

    suspend fun getUuid(name: String): Result<UuidData> {
        val key = name.lowercase()

        uuidCache.evictExpired()

        uuidCache[key]?.let { return Result.success(it.value) }

        return fetchJson<UuidData>(
            "https://api.minecraftservices.com/minecraft/profile/lookup/name/$name"
        ).onSuccess { uuidCache[key] = CacheEntry(it, System.currentTimeMillis()) }
    }

    suspend fun pullSecrets(name: String): Result<Long> {
        val uuidData = getUuid(name).getOrElse { return Result.failure(Exception(it.cause)) }
        return fetchJson<Long>(getServer(EndPoint.SECRETS, uuidData.id))
    }
    enum class EndPoint { SECRETS, GET }
    data class UuidData(val name: String, val id: String)
}
