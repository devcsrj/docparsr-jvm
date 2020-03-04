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

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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