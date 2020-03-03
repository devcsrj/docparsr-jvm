package com.github.devcsrj.docparsr

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

internal class ConfigurationDeserializer : StdDeserializer<Configuration>(Configuration::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Configuration {
        val root = p.codec.readTree<JsonNode>(p)

        val version = root.get("version").asText()
        val extractor = deserializeExtractor(root.get("extractor"))
        val cleaner = deserializeCleaner(root.get("cleaner"))
        val output = deserializeOutput(root.get("output"))

        return Configuration(
            version = version,
            extractor = extractor,
            cleaner = cleaner,
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

    private fun deserializeCleaner(node: JsonNode): Set<Cleaner> {
        val cleaners = mutableSetOf<Cleaner>()
        for (item in node) {
            if (item.isTextual) {
                val cleaner =
                    when (val key = item.asText()) {
                        "out-of-page-removal" -> OutOfPageRemoval
                        "link-detection" -> LinkDetection
                        "image-detection" -> ImageDetection
                        "heading-detection" -> HeadingDetection
                        "heading-detection-dt" -> HeadingDetectionDt
                        "list-detection" -> ListDetection
                        "page-number-detection" -> PageNumberDetection
                        "hierarchy-detection" -> HierarchyDetection
                        else -> UnknownCleaner(key)
                    }
                cleaners.add(cleaner)
            }
            if (item.isArray) {
                val name = item[0].asText()
                val opts = item[1]
                val cleaner = when (name) {
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
                        caseSensitive = opts["isCaseSensitive"].asBoolean(),
                        global = opts["isGlobal"].asBoolean(),
                        queries = opts["queries"].map {
                            RegexMatcher.Query(
                                label = it["label"].asText(),
                                regex = it["regex"].asText()
                            )
                        }.toSet()
                    )
                    else -> UnknownCleaner(name)
                }
                cleaners.add(cleaner)
            }
        }
        return cleaners
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
