package com.github.devcsrj.docparsr

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.Duration
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

        val countDownLatch = CountDownLatch(1)
        val config = Configuration(cleaners = setOf())
        val job = parser.newParsingJob(pdf, config)

        val callback = CollectingCallback(countDownLatch)
        job.enqueue(callback)
        countDownLatch.await()

        assertThat(callback.exception).isNull()
        assertThat(callback.result).isNotNull
    }

    private class CollectingCallback(private val latch: CountDownLatch) : ParsingJob.Callback {

        var exception: Exception? = null
        val updates: Queue<ParsingJob.Progress> = LinkedList()
        var result: ParsingJob.Result? = null

        override fun onFailure(job: ParsingJob, e: Exception) {
            exception = e
            latch.countDown()
        }

        override fun onProgress(job: ParsingJob, progress: ParsingJob.Progress) {
            updates.add(progress)
        }

        override fun onSuccess(job: ParsingJob, result: ParsingJob.Result) {
            this.result = result
            latch.countDown()
        }

    }
}