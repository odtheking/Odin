package com.odtheking.odin.utils.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.logger
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.InputStream
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.coroutines.resume

object WebUtils {
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    val gson: Gson = GsonBuilder().create()

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    suspend inline fun <reified T> fetchJson(url: String, json: Gson = gson): Result<T> = runCatching {
        json.fromJson<T>(fetchString(url).getOrElse { return Result.failure(it) }, T::class.java)
    }

    suspend fun fetchString(url: String): Result<String> =
        executeRequest(createGetRequest(url))
            .mapCatching { response -> response.body() }
            .onFailure { logger.warn("Failed to fetch from $url: ${it.message} ${it.cause}") }

    suspend fun getInputStream(url: String): Result<InputStream> =
        executeRequest(createGetRequest(url))
            .map { response -> response.body().byteInputStream() }
            .onFailure { logger.warn("Failed to get input stream from $url: ${it.message} ${it.cause}") }

    suspend fun postData(url: String, body: String): Result<String> =
        executeRequest(
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(10))
                .build()
        )
            .mapCatching { response -> response.body() }
            .onFailure { logger.warn("Failed to post data to $url: ${it.message}") }

    private fun createGetRequest(url: String): HttpRequest =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .header("User-Agent", USER_AGENT)
            .header("X-Mod-Version", OdinMod.version.friendlyString)
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build()

    private suspend fun executeRequest(request: HttpRequest): Result<HttpResponse<String>> = suspendCancellableCoroutine { cont ->
        cont.resume(Result.failure( IOException("BLOCKED")))
    }

    suspend fun hasBonusPaulScore(): Boolean {
        val response = fetchJson<JsonObject>("https://api.hypixel.net/resources/skyblock/election").getOrNull() ?: return false
        val mayor = response.getAsJsonObject("mayor") ?: return false
        return mayor.get("name")?.asString == "Paul" && mayor.getAsJsonArray("perks")?.any { it.asJsonObject.get("name")?.asString == "EZPZ" } == true
    }

    class InputStreamException(body: String, link: String) : Exception("$body : $link")
}
