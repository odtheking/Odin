package me.odinmain.utils.network

import me.odinmain.OdinMain.logger
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object SslUtils {
    private val TrustManager: TrustManagerFactory by lazy {
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                load(this::class.java.getResourceAsStream("/odincacerts.jks"), "changeit".toCharArray())
            })
        }
    }

    fun createSslContext(): SSLContext = SSLContext.getInstance("TLS").apply {
        try {
            init(null, TrustManager.trustManagers, null)
            logger.info("Created SSLContext successfully.")
        } catch (e: Exception) {
            logger.error("Failed to create SSLContext: ${e.message}")
            throw e
        }
    }

    fun getTrustManager(): X509TrustManager = try {
        val manager = TrustManager.trustManagers
        if (manager.isNotEmpty()) manager[0] as X509TrustManager
        else throw IllegalStateException("No TrustManager found")
    } catch(e: Exception) {
        logger.error("Failed to get TrustManager: ${e.message}")
        throw e
    }
}