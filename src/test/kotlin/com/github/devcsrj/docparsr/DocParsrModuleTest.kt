package com.github.devcsrj.docparsr

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.skyscreamer.jsonassert.JSONAssert
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

internal object DocParsrModuleTest : Spek({

    val objectMapper = ObjectMapper()
    objectMapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
    objectMapper.registerModule(DocParsrModule)
    objectMapper.registerModule(KotlinModule())

    describe("Jackson module") {
        val goldenConfig = GoldenConfiguration.INSTANCE

        it("can deserialize golden file") {
            val actual = javaClass.getResourceAsStream("/config.json").use {
                objectMapper.readValue(it, Configuration::class.java)
            }
            assertThat(actual).isEqualTo(goldenConfig)
        }

        it("can serialize golden file") {
            val expected = javaClass.getResourceAsStream("/config.json").use {
                it.bufferedReader().readText()
            }
            val writer = objectMapper.writerWithDefaultPrettyPrinter()
            val actual = writer.writeValueAsString(goldenConfig)
            JSONAssert.assertEquals(expected, actual, false)
        }
    }
})