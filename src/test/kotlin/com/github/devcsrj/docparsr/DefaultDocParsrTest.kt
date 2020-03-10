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

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*
import java.util.concurrent.Semaphore

internal class DefaultDocParsrTest {

    private val pollingInterval = Duration.ofSeconds(0)
    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun beforeEach() {
        mockWebServer = MockWebServer()
    }

    @AfterEach
    fun afterEach(): Unit = mockWebServer.shutdown()

    @Test
    fun `can fetch default configuration`() {
        val body = javaClass.getResourceAsStream("/config.json").use {
            it.bufferedReader().readText()
        }
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(body)
        )

        val parsr = DefaultDocParsr(mockWebServer.url("/"), pollingInterval)
        val actual = parsr.getDefaultConfig()

        assertThat(actual).isEqualTo(GoldenConfiguration.INSTANCE)

        val rr = mockWebServer.takeRequest()
        assertThat(rr.path).isEqualTo("/api/default-config")
    }

    @Test
    fun `can finish parsing job`(@TempDir tempDir: File) {
        val pollCount = 5
        val expectedJobId = "c9999a0a0595f5003ec9573a2c42f2"
        mockWebServer.apply {
            enqueue(
                MockResponse()
                    .setResponseCode(202)
                    .setBody(expectedJobId)
            )
            for (i in 1 until pollCount) {
                enqueue(
                    MockResponse()
                        .setResponseCode(200)
                        .setBody(
                            """{
                "estimated-remaining-time":null,
                "progress-percentage":0,
                "start-date":"2020-03-04T02:21:36.038Z",
                "status":"executing command: python3 blah blah"
                }""".trimIndent()
                        )
                )
            }
            enqueue(
                MockResponse()
                    .setResponseCode(201)
                    .setBody(
                        """
                {"id":"$expectedJobId",
                "json":"/api/v1/json/$expectedJobId",
                "csv":"/api/v1/csv/$expectedJobId",
                "text":"/api/v1/text/$expectedJobId",
                "markdown":"/api/v1/markdown/$expectedJobId"}
            """.trimIndent()
                    )
            )
        }


        val pdfBody = javaClass.getResourceAsStream("/dummy.pdf").use { src ->
            val file = tempDir.resolve("dummy.pdf")
            file.outputStream().use { sink ->
                src.copyTo(sink)
            }
            file
        }
        val parsr = DefaultDocParsr(mockWebServer.url("/"), pollingInterval)
        val job = parsr.newParsingJob(pdfBody, Configuration())

        val semaphore = Semaphore(pollCount)
        semaphore.acquire(pollCount)

        val updates: Queue<ParsingJob.Progress> = LinkedList()
        var actual: ParsingResult? = null
        job.enqueue(object : ParsingJob.Callback {
            override fun onFailure(jobId: String?, e: Exception) {}

            override fun onProgress(jobId: String, progress: ParsingJob.Progress) {
                updates.add(progress)
                semaphore.release()
            }

            override fun onSuccess(jobId: String, result: ParsingResult) {
                actual = result
                semaphore.release()
            }
        })

        semaphore.acquire(pollCount)
        assertThat(actual).isNotNull
        assertThat(updates).hasSize(4)
        for (update in updates) {
            assertThat(update.message).isEqualTo("executing command: python3 blah blah")
        }

        val rr = mockWebServer.takeRequest()
        assertThat(rr.path).isEqualTo("/api/document")
        assertThat(rr.method).isEqualTo("POST")
        assertThat(rr.getHeader("Content-Type")).startsWith("multipart/form-data")
        val body = rr.body.buffer.readString(StandardCharsets.UTF_8)
        assertThat(body).contains("Content-Disposition: form-data; name=\"file\"; filename=\"dummy.pdf\"")
        assertThat(body).contains("Content-Type: application/pdf")
        assertThat(body).contains("Content-Length: 13264")
        assertThat(body).contains("Content-Disposition: form-data; name=\"config\"; filename=\"parsr-conf")
        assertThat(body).contains("Content-Type: application/json")
        assertThat(body).contains("Content-Length: 976")
    }
}