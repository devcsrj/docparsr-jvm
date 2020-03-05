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
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import okio.buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import org.skyscreamer.jsonassert.JSONAssert
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

class DefaultDocParsrFnTest {

    companion object {
        @JvmStatic
        private val server = ParsrServerContainer()

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            server.start()
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            server.stop()
        }
    }


    private lateinit var parser: DocParsr

    @BeforeEach
    fun beforeEach() {
        parser = DocParsr.create(server.getUrl())
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
            .build()
        val field = parser::class.declaredMemberProperties.find {
            it.returnType.javaType == OkHttpClient::class.java
        } ?: fail("Expecting an OkHttpClient field, but got none")
        field.javaField.apply {
            this!!.trySetAccessible()
            this.set(parser, httpClient)
        }
    }

    @Test
    fun `can get default config`() {
        val config = parser.getDefaultConfig()
        assertThat(config).isNotNull
    }

    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    @Test
    fun `can parse pdf`(@TempDir tempDir: File) {
        val pdf = tempDir.resolve("dummy.pdf")
        javaClass.getResourceAsStream("/dummy.pdf").use { source ->
            pdf.outputStream().use { sink ->
                source.copyTo(sink)
            }
        }

        val config = Configuration()
        val job = parser.newParsingJob(pdf, config)
        val result = job.execute()

        assertThat(result).isNotNull

        assertThat(result.source(Csv).buffer().readUtf8())
            .isEqualTo(readResource("/dummy.csv"))
        JSONAssert.assertEquals(
            readResource("/dummy.json"),
            result.source(Json).buffer().readUtf8(), false
        )
        assertThat(result.source(Markdown).buffer().readUtf8())
            .isEqualTo(readResource("/dummy.md"))
        assertThat(result.source(Text).buffer().readUtf8())
            .isEqualTo(readResource("/dummy.txt"))
    }

    private fun readResource(path: String): String {
        return javaClass.getResourceAsStream(path).use { source ->
            val buffer = Buffer()
            buffer.readFrom(source).readUtf8()
        }
    }

}