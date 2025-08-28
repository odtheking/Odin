package me.odinmain.utils.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.okClient
import me.odinmain.utils.network.SslUtils.createSslContext
import me.odinmain.utils.network.SslUtils.getTrustManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.apache.http.impl.EnglishReasonPhraseCatalog
import org.luaj.vm2.ast.Str
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object WebUtils {
    val gson = GsonBuilder().setPrettyPrinting().create()

//    object UuidCache {
//        private val cache = HashMap<String, String>()
//
//        fun addToCache(name: String, uuid: String) {
//            val lowercase = name.lowercase()
//            if (lowercase in cache) return
//            cache[name.lowercase()] = uuid.replace("-", "")
//        }
//
//        fun getFromCache(name: String): String? = cache[name.lowercase()]
//    }

    private const val USER_AGENT = "OdinAPI" // this can be replaced with a browser like one but it might be good to send this to our api to potentially stop unrelated scraping

    suspend inline fun <reified T> streamAndRead(url: String, json: Gson = gson): Result<T> = runCatching {
        return getInputStream(url).map {
            json.fromJson<T>(it.bufferedReader().use { reader -> reader.readText() }, T::class.java)
        }
    }

    suspend fun getInputStream(url: String): Result<InputStream> =
        clientCall(
            Request.Builder().url(url).build()
        ).onFailure { e -> logger.warn("Failed to get input stream. Error: ${e.message}") }


//    suspend fun getUUIDbyName(name: String): Result<MojangData> {
//        UuidCache.getFromCache(name)?.let { return Result.success(MojangData(name, it)) }
//        return streamAndRead<MojangData>("https://api.minecraftservices.com/minecraft/profile/lookup/name/$name")
//            .onSuccess { UuidCache.addToCache(name, it.uuid) }
//    }

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

//    @Serializable
//    data class MojangData(
//        val name: String,
//        @SerialName("id")
//        val uuid: String
//    )

    class InputStreamException(code: Int, url: String) : Exception("Failed to get input stream from $url: ${EnglishReasonPhraseCatalog.INSTANCE.getReason(code, null)}")
}