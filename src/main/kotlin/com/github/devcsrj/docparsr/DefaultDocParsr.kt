package com.github.devcsrj.docparsr

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okio.Source
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.time.Duration

/**
 * A [DocParsr] implementation that communicates with the Parsr API server
 *
 * See [API guide](https://github.com/axa-group/Parsr/blob/master/docs/api-guide.md)
 */
internal class DefaultDocParsr(
    val baseUri: HttpUrl,
    val pollingInterval: Duration
) : DocParsr {

    private val httpClient = OkHttpClient()
    private val objectMapper = ObjectMapper()

    init {
        objectMapper.registerModule(KotlinModule())
        objectMapper.registerModule(JavaTimeModule())
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

    override fun newParsingJob(file: File, config: Configuration): ParsingJob {
        return ParsingJobImpl(config, file)
    }

    private inner class ParsingJobImpl(
        private val config: Configuration,
        private val file: File
    ) : ParsingJob {

        // Guarded by this.
        private var executed = false

        override fun configuration() = config
        override fun enqueue(callback: ParsingJob.Callback) {
            synchronized(this) {
                check(!executed) { "Already Executed" }
                executed = true
            }

            val configFile: File
            try {
                configFile = Files.createTempFile("parsr-conf", "json").toFile()
                objectMapper.writeValue(configFile, configFile)
            } catch (e: IOException) {
                throw ParsingJobException("Could not write configuration to a temporary file", e)
            }

            val form = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody(null))
                .addFormDataPart(
                    "config",
                    configFile.name,
                    file.asRequestBody("application/json".toMediaTypeOrNull())
                )
                .build()
            val request = Request.Builder()
                .post(form)
                .url(baseUri.resolve("/api/document")!!)
                .build()
            httpClient.newCall(request).enqueue(submissionCallback(callback))
        }

        private fun submissionCallback(callback: ParsingJob.Callback): Callback {
            val job = this
            return object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailure(job, e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code % 100 != 2) {
                        callback.onFailure(job, ParsingJobException("The server rejected the file"))
                        return
                    }
                    val jobId = response.body?.string()
                    if (jobId == null) {
                        callback.onFailure(job, ParsingJobException("Expecting a job id from the server, but got none"))
                        return
                    }
                    runBlocking {
                        startPolling(jobId, callback)
                    }
                }
            }
        }

        private suspend fun startPolling(jobId: String, callback: ParsingJob.Callback) {
            val job = this
            val request = Request.Builder()
                .get()
                .url(baseUri.resolve("/api/queue/$jobId")!!)
                .build()
            while (true) {
                delay(pollingInterval.toMillis())
                var stopPolling = false
                httpClient.newCall(request).execute().use {
                    when (it.code) {
                        201 -> {
                            stopPolling = true
                            callback.onSuccess(job, HttpResult(jobId, config))
                        }
                        200 -> {
                            val src = it.body!!.source().inputStream()
                            val progress = objectMapper.readValue<ParsingJob.Progress>(src)
                            callback.onProgress(job, progress)
                        }
                        else -> {
                            stopPolling = true
                            callback.onFailure(
                                job,
                                ParsingJobException("The server returned '${it.code}' while polling for '$jobId'")
                            )
                        }
                    }
                }
                if (stopPolling) {
                    break
                }
            }
        }
    }

    private inner class HttpResult(
        private val id: String,
        private val config: Configuration
    ) : ParsingJob.Result {

        override fun id() = id
        override fun source(format: Format): Source {
            if (!config.output.formats.contains(format))
                throw IllegalArgumentException("The format '$format' is not enabled in the job configuration")

            val request = Request.Builder()
                .get()
                .url(baseUri.resolve("/${format.name}/$id")!!)
                .build()
            val response = httpClient.newCall(request).execute()
            if (response.code != 200) {
                throw IllegalStateException(
                    "Could not fetch result of '$id' for '$format' - the server " +
                            "responded with '${response.code}'"
                )
            }
            val body = response.body ?: throw IllegalStateException("Could not fetch result for '$format'")
            return body.source()
        }
    }
}