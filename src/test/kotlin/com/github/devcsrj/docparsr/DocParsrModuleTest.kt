package com.github.devcsrj.docparsr

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.skyscreamer.jsonassert.JSONAssert
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

internal object DocParsrModuleTest : Spek({

    val objectMapper = ObjectMapper()
    objectMapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
    objectMapper.registerModule(DocParsrModule)
    objectMapper.registerModule(KotlinModule())
    objectMapper.registerModule(JavaTimeModule())

    describe("Jackson module") {
        val goldenConfig = GoldenConfiguration.INSTANCE

        it("can deserialize config") {
            val actual = javaClass.getResourceAsStream("/config.json").use {
                objectMapper.readValue(it, Configuration::class.java)
            }
            assertThat(actual).isEqualTo(goldenConfig)
        }

        it("can serialize config") {
            val expected = javaClass.getResourceAsStream("/config.json").use {
                it.bufferedReader().readText()
            }
            val writer = objectMapper.writerWithDefaultPrettyPrinter()
            val actual = writer.writeValueAsString(goldenConfig)
            JSONAssert.assertEquals(expected, actual, false)
        }

        it("can deserialize job progress") {
            val actual = javaClass.getResourceAsStream("/job-progress.json").use {
                objectMapper.readValue(it, ParsingJob.Progress::class.java)
            }
            assertThat(actual.timestamp).isNotNull()
            assertThat(actual.message).isEqualTo("Detecting reading order...")
        }
    }
})