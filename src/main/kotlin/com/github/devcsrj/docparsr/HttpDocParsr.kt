/**
 * Copyright (c) 2020, Reijhanniel Jearl Campos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.devcsrj.docparsr

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.apache.tika.Tika
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.time.Duration
import java.util.concurrent.CountDownLatch

/**
 * A [DocParsr] implementation that communicates with the Parsr API server
 *
 * See [API guide](https://github.com/axa-group/Parsr/blob/master/docs/api-guide.md)
 */
@Suppress("MagicNumber")
internal class HttpDocParsr(
    val baseUri: HttpUrl,
    val pollingInterval: Duration
) : DocParsr {

    private val httpClient: OkHttpClient = OkHttpClient()
    private val objectMapper = Jackson.MAPPER
    private val tika = Tika()

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

    override fun loadParsingResult(jobId: String): ParsingResult {
        return HttpParsingResult(jobId, baseUri, httpClient)
    }

    private inner class ParsingJobImpl(
        private val config: Configuration,
        private val file: File
    ) : ParsingJob {

        // Guarded by this.
        private var executed = false
        private val inputFileMediaType = tika.detect(file).toMediaTypeOrNull()

        override fun configuration() = config
        override fun enqueue(callback: ParsingJob.Callback) {
            synchronized(this) {
                check(!executed) { "Already Executed" }
                executed = true
            }

            val configFile: File
            try {
                configFile = Files.createTempFile("parsr-conf", ".json").toFile()
                objectMapper.writeValue(configFile, config)
            } catch (e: IOException) {
                throw ParsingJobException("Could not write configuration to a temporary file", e)
            }

            val form = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody(inputFileMediaType))
                .addFormDataPart(
                    "config",
                    configFile.name,
                    configFile.asRequestBody("application/json".toMediaTypeOrNull())
                )
                .build()
            val request = Request.Builder()
                .post(form)
                .url(baseUri.resolve("/api/document")!!)
                .build()
            httpClient.newCall(request).enqueue(submissionCallback(callback))
        }

        private fun submissionCallback(callback: ParsingJob.Callback): Callback {
            return object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailure(null, e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val jobId = response.body?.string()
                    if (response.code % 100 != 2) {
                        callback.onFailure(null, ParsingJobException("The server rejected the file ($jobId)"))
                        return
                    }
                    if (jobId == null) {
                        callback.onFailure(
                            null,
                            ParsingJobException("Expecting a job id from the server, but got none")
                        )
                        return
                    }
                    runBlocking {
                        startPolling(jobId, callback)
                    }
                }
            }
        }

        private suspend fun startPolling(jobId: String, callback: ParsingJob.Callback) {
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
                            callback.onSuccess(
                                jobId,
                                HttpParsingResult(jobId, baseUri, httpClient)
                            )
                        }
                        200 -> {
                            val src = it.body!!.source().inputStream()
                            val progress = objectMapper.readValue<ParsingJob.Progress>(src)
                            callback.onProgress(jobId, progress)
                        }
                        else -> {
                            stopPolling = true
                            callback.onFailure(
                                jobId,
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

        override fun execute(): ParsingResult {
            val latch = CountDownLatch(1)
            var output: Any? = null
            val logger = LoggerFactory.getLogger(HttpDocParsr::class.java)
            enqueue(object : ParsingJob.Callback {
                override fun onFailure(jobId: String?, e: Exception) {
                    output = e
                    latch.countDown()
                }

                override fun onProgress(jobId: String, progress: ParsingJob.Progress) {
                    logger.info("[JOB-$jobId] ${progress.message}")
                }

                override fun onSuccess(jobId: String, result: ParsingResult) {
                    output = result
                    latch.countDown()
                }
            })
            latch.await()
            when (output) {
                is Throwable -> throw output as Throwable
                is ParsingResult -> return output as ParsingResult
                else -> throw AssertionError("Should never happen")
            }
        }
    }
}
