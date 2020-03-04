package com.github.devcsrj.docparsr

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * A [DocParsr] implementation that communicates with the Parsr API server
 *
 * See [API guide](https://github.com/axa-group/Parsr/blob/master/docs/api-guide.md)
 */
internal class DefaultDocParsr(val baseUri: HttpUrl) : DocParsr {

    private val httpClient = OkHttpClient()
    private val objectMapper = ObjectMapper()

    init {
        objectMapper.registerModule(KotlinModule())
        objectMapper.registerModule(DocParsrModule)
    }

    override fun getDefaultConfig(): Configuration {
        val request = Request.Builder()
            .get().url(baseUri.resolve("/api/default-config")!!)
            .build()
        return httpClient.newCall(request).execute().use { response ->
            if (response.code != 200) {
                throw IllegalStateException("The server returned ${response.code}")
            }
            val body = response.body ?: throw IllegalStateException("The server did not return any content")
            objectMapper.readValue(body.byteStream())
        }
    }
}