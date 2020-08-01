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

@SuppressWarnings("TooManyFunctions")
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

    @Suppress("ReturnCount", "ComplexMethod")
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
                "drawing-detection" -> DrawingDetection
                "header-footer-detection" -> deserializeHeaderFooterDetection(opts)
                "hierarchy-detection" -> HierarchyDetection
                "image-detection" -> ImageDetection(opts["ocrImages"].asBoolean())
                "key-value-detection" -> deserializeKeyValueDetection(opts)
                "lines-to-paragraph" -> LinesToParagraph(tolerance = opts["tolerance"].asDouble())
                "link-detection" -> LinkDetection
                "list-detection" -> ListDetection
                "ml-heading-detection" -> MlHeadingDetection
                "number-correction" -> deserializeNumberCorrection(opts)
                "out-of-page-removal" -> OutOfPageRemoval
                "page-number-detection" -> PageNumberDetection
                "reading-order-detection" -> deserializeReadingOrderDetection(opts)
                "redundancy-detection" -> RedundancyDetection(opts["minOverlap"].asDouble())
                "regex-matcher" -> deserializeRegexMatcher(opts)
                "separate-words" -> SeparateWords
                "table-detection" -> deserializeTableDetection(opts)
                "table-of-contents-detection" -> deserializeTableOfContentsDetection(opts)
                "whitespace-removal" -> WhitespaceRemoval(opts["minWidth"].asInt())
                "words-to-line" -> deserializeWordsToLine(opts)
                else -> UnknownCleaner(name)
            }
        }
        return UnknownCleaner(node.asText())
    }

    private fun deserializeTableOfContentsDetection(opts: JsonNode): TableOfContentsDetection {
        return TableOfContentsDetection(
            pageKeywords = opts["pageKeywords"].map { it.asText() }.toSet()
        )
    }

    private fun deserializeWordsToLine(opts: JsonNode): WordsToLine {
        return WordsToLine(
            lineHeightUncertainty = opts["lineHeightUncertainty"].asDouble(),
            topUncertainty = opts["topUncertainty"].asDouble(),
            maximumSpaceBetweenWords = opts["maximumSpaceBetweenWords"].asInt(),
            mergeTableElements = opts["mergeTableElements"].asBoolean()
        )
    }

    private fun deserializeReadingOrderDetection(opts: JsonNode): ReadingOrderDetection {
        return ReadingOrderDetection(
            minVerticalGapWidth = opts["minVerticalGapWidth"].asInt(),
            minColumnWidthInPagePercent = opts["minColumnWidthInPagePercent"].asDouble()
        )
    }

    private fun deserializeNumberCorrection(opts: JsonNode): Cleaner {
        return NumberCorrection(
            fixSplitNumbers = opts["fixSplitNumbers"].asBoolean(),
            maxConsecutiveSplits = opts["maxConsecutiveSplits"].asInt(),
            numberRegExp = opts["numberRegExp"].asText(),
            whitelist = opts["whitelist"].map { it.asText() }.toSet()
        )
    }

    private fun deserializeKeyValueDetection(opts: JsonNode): Cleaner {
        return UnknownCleaner("key-value-detection")
    }

    private fun deserializeHeaderFooterDetection(opts: JsonNode): HeaderFooterDetection {
        return HeaderFooterDetection(
            ignorePages = opts["ignorePages"].map { it.asInt() }.toSet(),
            maxMarginPercentage = opts["maxMarginPercentage"].asInt()
        )
    }

    private fun deserializeRegexMatcher(opts: JsonNode): RegexMatcher {
        return RegexMatcher(
            isCaseSensitive = opts["isCaseSensitive"].asBoolean(),
            isGlobal = opts["isGlobal"].asBoolean(),
            queries = opts["queries"].map {
                RegexMatcher.Query(
                    label = it["label"].asText(),
                    regex = it["regex"].asText()
                )
            }.toSet()
        )
    }

    private fun deserializeTableDetection(opts: JsonNode): TableDetection {
        return TableDetection(
            opts["checkDrawings"].booleanValue(),
            opts["runConfig"].map { opt ->
                TableDetection.Option(
                    pages = opt["pages"].map { it.asInt() }.toSet(),
                    flavor = TableDetection.Flavor.fromString(opt["flavor"].asText())
                )
            }.toList()
        )
    }

    private fun deserializeOutput(node: JsonNode): Output {
        val granularity = when (val value = node.get("granularity").asText()) {
            "word" -> Output.Granularity.WORD
            "character" -> Output.Granularity.CHARACTER
            else -> throw UnsupportedOperationException("granularity: $value")
        }
        val includeMarginals = node["includeMarginals"].asBoolean()
        val includeDrawings = node["includeDrawings"].asBoolean()

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
        if (formatNode["simpleJson"].asBoolean()) {
            formats.add(SimpleJson)
        }

        return Output(
            granularity = granularity,
            includeMarginals = includeMarginals,
            includeDrawings = includeDrawings,
            formats = formats
        )
    }
}
