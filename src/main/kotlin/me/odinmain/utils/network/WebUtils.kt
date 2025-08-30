package me.odinmain.utils.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.withTimeoutOrNull
import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.okClient
import me.odinmain.utils.network.SslUtils.createSslContext
import me.odinmain.utils.network.SslUtils.getTrustManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.apache.http.impl.EnglishReasonPhraseCatalog
import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object WebUtils {
    val gson = GsonBuilder().setPrettyPrinting().create()

    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"

    suspend inline fun <reified T> fetchJson(url: String, json: Gson = gson): Result<T> = runCatching {
        return getInputStream(url).map {
            json.fromJson<T>(it.bufferedReader().use { reader -> reader.readText() }, T::class.java)
        }
    }

    suspend inline fun fetchString(url: String): Result<String> = runCatching {
        return getInputStream(url).map { it.bufferedReader().use { reader -> reader.readText() } }
    }

    suspend fun getInputStream(url: String): Result<InputStream> =
        clientCall(Request.Builder().url(url).build())
            .onFailure { e -> logger.warn("Failed to get input stream. Error: ${e.message}") }

    suspend fun postData(url: String, body: String): Result<String> =
        clientCall(Request.Builder().url(url).post(body.toRequestBody()).build())
            .map { it.bufferedReader().use { reader -> reader.readText() } }
            .onFailure { e -> logger.warn("Failed to post data. Error: ${e.message}") }

    private suspend fun clientCall(request: Request): Result<InputStream> = suspendCoroutine { cont ->
        logger.info("Making request to ${request.url}")

        val callback = object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                cont.resume(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) response.body?.let { cont.resume(Result.success(it.byteStream())) }
                else cont.resume(Result.failure(InputStreamException(response.code, request.url.toString())))
            }
        }

        okClient.newCall(request).enqueue(callback)
    }

    fun createClient(): OkHttpClient = OkHttpClient.Builder().apply {
        sslSocketFactory(createSslContext().socketFactory, getTrustManager())

        dispatcher(Dispatcher().apply {
            maxRequests = 1
            maxRequestsPerHost = 1
        })

        readTimeout(10, TimeUnit.SECONDS)
        connectTimeout(5, TimeUnit.SECONDS)
        writeTimeout(10, TimeUnit.SECONDS)

        addInterceptor { chain ->
            chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .build()
                .let { chain.proceed(it) }
        }
    }.build()

    suspend fun hasBonusPaulScore(): Boolean = withTimeoutOrNull(5000) {
        val response: String = fetchString("https://api.hypixel.net/resources/skyblock/election").getOrElse { return@withTimeoutOrNull false }
        val jsonObject = JsonParser().parse(response).asJsonObject
        val mayor = jsonObject.getAsJsonObject("mayor") ?: return@withTimeoutOrNull false
        val name = mayor.get("name")?.asString ?: return@withTimeoutOrNull false
        return@withTimeoutOrNull if (name == "Paul") {
            mayor.getAsJsonArray("perks")?.any { it.asJsonObject.get("name")?.asString == "EZPZ" } == true
        } else false
    } == true

    class InputStreamException(code: Int, url: String) : Exception("Failed to get input stream from $url: ${EnglishReasonPhraseCatalog.INSTANCE.getReason(code, null)}")
}