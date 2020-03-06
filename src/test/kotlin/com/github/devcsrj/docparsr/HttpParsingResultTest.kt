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

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class HttpParsingResultTest : Spek({

    describe("retrievable parsing result") {

        val httpClient = OkHttpClient()
        lateinit var mockWebServer: MockWebServer

        beforeEachTest {
            mockWebServer = MockWebServer()
        }

        afterEachTest { mockWebServer.shutdown() }

        val jobId = "fake-job-id"

        Format::class.sealedSubclasses.forEach {
            val format = it.objectInstance!!

            it("can retrieve result for ${format.name}") {
                mockWebServer.enqueue(MockResponse()
                    .setResponseCode(200))

                val result = HttpParsingResult(jobId, mockWebServer.url("/"), httpClient)
                assertThat(result.id()).isEqualTo(jobId)
                val actual = result.source(format)
                assertThat(actual).hasContent("")

                val rr = mockWebServer.takeRequest()
                assertThat(rr.method).isEqualTo("GET")
                assertThat(rr.path).isEqualTo("/api/${format.name}/$jobId")
            }
        }
    }
})