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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import kotlin.reflect.KClass

internal class ConfigurationDeserializer : StdDeserializer<Configuration>(Configuration::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Configuration {
        val root = p.codec.readTree<JsonNode>(p)

        val version = root.get("version").asText()
        val extractor = deserializeExtractor(root.get("extractor"))
        val cleaner = deserializeCleaners(root.get("cleaner"))
        val output = deserializeOutput(root.get("output"))

        return Configuration(
            version = version,
            extractor = extractor,
            cleaners = cleaner,
            output = output
        )
    }

    private fun deserializeExtractor(node: JsonNode): Extractor {
        val pdf = when (val pdfKey = node.get("pdf").asText()) {
            "pdfminer" -> PdfMiner
            "pdfjs" -> PdfJs
            else -> UnknownPdfExtractor(pdfKey)
        }
        val ocr = when (val ocrKey = node.get("ocr").asText()) {
            "tesseract" -> Tesseract
            else -> UnknownOcrExtractor(ocrKey)
        }
        val languages = node.get("language").map { it.asText() }.toSet()
        return Extractor(
            pdfExtractor = pdf,
            ocrExtractor = ocr,
            languages = languages
        )
    }

    private fun deserializeCleaners(node: JsonNode): Set<Cleaner> {
        val cleaners = mutableSetOf<Cleaner>()
        for (item in node) {
            val cleaner = deserializeCleaner(item)
            cleaners.add(cleaner)
        }
        return cleaners
    }

    @Suppress("ReturnCount")
    private fun deserializeCleaner(node: JsonNode): Cleaner {
        if (node.isTextual) {
            val klass: KClass<out Cleaner>? = Cleaner::class.sealedSubclasses.find {
                it.objectInstance?.name == node.asText()
            }
            if (klass != null) {
                return klass.objectInstance!!
            }
        }
        if (node.isArray) {
            val name = node[0].asText()
            val opts = node[1]
            return when (name) {
                "whitespace-removal" -> WhitespaceRemoval(opts["minWidth"].asInt())
                "redundancy-detection" -> RedundancyDetection(opts["minOverlap"].asDouble())
                "table-detection" -> TableDetection(opts["runConfig"].map { opt ->
                    TableDetection.Option(
                        pages = opt["pages"].map { it.asInt() }.toSet(),
                        flavor = TableDetection.Flavor.fromString(opt["flavor"].asText())
                    )
                }.toList())
                "header-footer-detection" -> HeaderFooterDetection(
                    ignorePages = opts["ignorePages"].map { it.asInt() }.toSet(),
                    maxMarginPercentage = opts["maxMarginPercentage"].asInt()

                )
                "reading-order-detection" -> ReadingOrderDetection(
                    minVerticalGapWidth = opts["minVerticalGapWidth"].asInt(),
                    minColumnWidthInPagePercent = opts["minColumnWidthInPagePercent"].asDouble()
                )
                "words-to-line" -> WordsToLine(
                    lineHeightUncertainty = opts["lineHeightUncertainty"].asDouble(),
                    topUncertainty = opts["topUncertainty"].asDouble(),
                    maximumSpaceBetweenWords = opts["maximumSpaceBetweenWords"].asInt(),
                    mergeTableElements = opts["mergeTableElements"].asBoolean()
                )
                "lines-to-paragraph" -> LinesToParagraph(
                    tolerance = opts["tolerance"].asDouble()

                )
                "table-of-contents-detection" -> TableOfContentsDetection(
                    keywords = opts["keywords"].map { it.asText() }.toSet(),
                    pageKeywords = opts["pageKeywords"].map { it.asText() }.toSet()
                )
                "regex-matcher" -> RegexMatcher(
                    isCaseSensitive = opts["isCaseSensitive"].asBoolean(),
                    isGlobal = opts["isGlobal"].asBoolean(),
                    queries = opts["queries"].map {
                        RegexMatcher.Query(
                            label = it["label"].asText(),
                            regex = it["regex"].asText()
                        )
                    }.toSet()
                )
                else -> UnknownCleaner(name)
            }
        }
        return UnknownCleaner(node.asText())
    }

    private fun deserializeOutput(node: JsonNode): Output {
        val granularity = when (val value = node.get("granularity").asText()) {
            "word" -> Output.Granularity.WORD
            "character" -> Output.Granularity.CHARACTER
            else -> throw UnsupportedOperationException("granularity: $value")
        }
        val includeMarginals = node["includeMarginals"].asBoolean()

        val formatNode = node["formats"]
        val formats = mutableSetOf<Format>()
        if (formatNode["json"].asBoolean()) {
            formats.add(Json)
        }
        if (formatNode["text"].asBoolean()) {
            formats.add(Text)
        }
        if (formatNode["csv"].asBoolean()) {
            formats.add(Csv)
        }
        if (formatNode["markdown"].asBoolean()) {
            formats.add(Markdown)
        }
        if (formatNode["pdf"].asBoolean()) {
            formats.add(Pdf)
        }

        return Output(
            granularity = granularity,
            includeMarginals = includeMarginals,
            formats = formats
        )
    }
}
