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
import org.assertj.core.api.Assertions.assertThat
import org.skyscreamer.jsonassert.JSONAssert
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

internal object DocParsrModuleTest : Spek({

    val objectMapper = ObjectMapper()
    objectMapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
    objectMapper.registerModule(DocParsrModule)

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


        describe("elements") {

            it("can deserialize properties") {
                val actual = javaClass.getResourceAsStream("/element/properties.json").use {
                    objectMapper.readValue(it, Properties::class.java)
                }
                assertThat(actual.order).isEqualTo(7)
                assertThat(actual.isRedundant).isTrue()
                assertThat(actual.isHeader).isTrue()
                assertThat(actual.isFooter).isTrue()
                assertThat(actual.isPageNumber).isTrue()
                assertThat(actual.bulletList).isTrue()
                assertThat(actual.others).containsEntry(
                    "titleScores", mapOf(
                        "size" to 1.16,
                        "weight" to 0,
                        "color" to 0,
                        "name" to 0,
                        "italic" to 0,
                        "underline" to 0
                    )
                )
            }

            it("can deserialize word") {
                val actual = javaClass.getResourceAsStream("/element/word.json").use {
                    objectMapper.readValue(it, Word::class.java)
                }
                assertThat(actual.id()).isEqualTo(ElementId.valueOf(25))
                assertThat(actual.type()).isEqualTo(Element.Type.WORD)
                assertThat(actual.properties().order).isEqualTo(0)
                assertThat(actual.box()).isEqualTo(
                    Box(
                        left = 158.15,
                        top = 204.92,
                        width = 88.89,
                        height = 22.9
                    )
                )
                assertThat(actual.content()).isEqualTo("REPUBLIC")
                assertThat(actual.fontId()).isEqualTo(FontId.valueOf(1))
            }

            it("can deserialize line") {
                val actual = javaClass.getResourceAsStream("/element/line.json").use {
                    objectMapper.readValue(it, Line::class.java)
                }
                assertThat(actual.id()).isEqualTo(ElementId.valueOf(81180))
                assertThat(actual.type()).isEqualTo(Element.Type.LINE)
                assertThat(actual.properties().order).isEqualTo(0)
                assertThat(actual.box()).isEqualTo(
                    Box(
                        left = 158.15,
                        top = 204.92,
                        width = 288.63,
                        height = 22.9
                    )
                )
                assertThat(actual.content()).isEmpty()
            }

            it("can deserialize paragraph") {
                val actual = javaClass.getResourceAsStream("/element/paragraph.json").use {
                    objectMapper.readValue(it, Paragraph::class.java)
                }
                assertThat(actual.id()).isEqualTo(ElementId.valueOf(84348))
                assertThat(actual.type()).isEqualTo(Element.Type.PARAGRAPH)
                assertThat(actual.properties().order).isEqualTo(45)
                assertThat(actual.box()).isEqualTo(
                    Box(
                        left = 326.14,
                        top = 114.12,
                        width = 132.03,
                        height = 14.14
                    )
                )
                assertThat(actual.content()).isEmpty()
            }

            it("can deserialize heading") {
                val actual = javaClass.getResourceAsStream("/element/heading.json").use {
                    objectMapper.readValue(it, Heading::class.java)
                }
                assertThat(actual.id()).isEqualTo(ElementId.valueOf(84084))
                assertThat(actual.type()).isEqualTo(Element.Type.HEADING)
                assertThat(actual.properties().order).isEqualTo(0)
                assertThat(actual.box()).isEqualTo(
                    Box(
                        left = 158.15,
                        top = 204.92,
                        width = 288.63,
                        height = 22.9
                    )
                )
                assertThat(actual.content()).isEmpty()
                assertThat(actual.level()).isEqualTo(5)
            }

            it("can deserialize generic element") {
                val actual = javaClass.getResourceAsStream("/element/image.json").use {
                    objectMapper.readValue(it, AnyElement::class.java)
                }
                assertThat(actual.id()).isEqualTo(ElementId.valueOf(132))
                assertThat(actual.type()).isEqualTo(Element.Type.IMAGE)
                assertThat(actual.properties().others).containsEntry("src", "")
                assertThat(actual.properties().others).containsEntry("refId", "Bg")
                assertThat(actual.properties().others).containsEntry("xObjId", "103")
                assertThat(actual.properties().others).containsEntry("xObjExt", "jpg")
                assertThat(actual.box()).isEqualTo(
                    Box(
                        left = 0.0,
                        top = 0.0,
                        width = 595.7,
                        height = 840.95
                    )
                )
            }
        }

        describe("pages") {

            it("can deserialize fonts") {
                val actual = javaClass.getResourceAsStream("/page/font.json").use {
                    objectMapper.readValue(it, Font::class.java)
                }
                assertThat(actual.id).isEqualTo(FontId.valueOf(22))
                assertThat(actual.name).isEqualTo("CISFEO+TimesNewRomanPSMT")
                assertThat(actual.size).isEqualTo(13.47)
                assertThat(actual.weight).isEqualTo(FontWeight.valueOf("medium"))
                assertThat(actual.isItalic).isFalse()
                assertThat(actual.isUnderline).isFalse()
                assertThat(actual.color).isEqualTo(FontColor.fromHex("#000000"))
                assertThat(actual.sizeUnit).isEqualTo("px")
            }

            it("can deserialize page") {
                val actual = javaClass.getResourceAsStream("/page/page.json").use {
                    objectMapper.readValue(it, Page::class.java)
                }
                assertThat(actual.box).isEqualTo(
                    Box(
                        left = 0.0,
                        top = 0.0,
                        width = 595.7,
                        height = 840.95
                    )
                )
                assertThat(actual.rotation).isEqualTo(
                    Rotation(
                        degrees = 0.0,
                        origin = Point(0, 0),
                        translation = Point(0, 0)
                    )
                )
                assertThat(actual.number).isEqualTo(1)
                assertThat(actual.elements).hasSize(8)
            }
        }

        describe("metadata") {

            it("can deserialize regex content") {
                val actual = javaClass.getResourceAsStream("/metadata/regex.json").use {
                    objectMapper.readValue(it, Metadata::class.java)
                }
                assertThat(actual.id).isEqualTo(MetadataId.valueOf(1))
                assertThat(actual.elementIds).containsExactly(ElementId.valueOf(37381))
                assertThat(actual.type).isEqualTo(Metadata.Type.REGEX)

                val content = actual.content as Metadata.RegexContent
                assertThat(content.name).isEqualTo("Percent")
                assertThat(content.regex).isEqualTo("([\\-]?(\\d)+[\\.\\,]*(\\d)*)[ ]*(%|per|percent)")
                assertThat(content.fullMatch).isEqualTo("34%")
                assertThat(content.groups).containsExactly(
                    "34", "4", null, "%"
                )
            }
        }
    }
})
