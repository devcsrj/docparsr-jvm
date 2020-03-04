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
        val goldenConfig = Configuration(
            version = "0.9",
            extractor = Extractor(
                pdfExtractor = PdfMiner,
                ocrExtractor = Tesseract,
                languages = setOf("eng", "fra")
            ),
            cleaners = setOf(
                OutOfPageRemoval,
                WhitespaceRemoval(0),
                RedundancyDetection(0.5),
                TableDetection(
                    listOf(
                        TableDetection.Option(
                            pages = emptySet(),
                            flavor = TableDetection.Flavor.LATTICE
                        )
                    )
                ),
                HeaderFooterDetection(
                    ignorePages = emptySet(),
                    maxMarginPercentage = 15
                ),
                ReadingOrderDetection(
                    minVerticalGapWidth = 5,
                    minColumnWidthInPagePercent = 15.0
                ),
                LinkDetection,
                ImageDetection,
                WordsToLine(
                    lineHeightUncertainty = 0.2,
                    topUncertainty = 0.4,
                    maximumSpaceBetweenWords = 100,
                    mergeTableElements = false
                ),
                LinesToParagraph(
                    tolerance = 0.25
                ),
                HeadingDetection,
                HeadingDetectionDt,
                ListDetection,
                PageNumberDetection,
                HierarchyDetection,
                TableOfContentsDetection(
                    keywords = setOf(
                        "contents",
                        "index",
                        "table of contents",
                        "contenidos",
                        "indice",
                        "índice",
                        "tabla de contenidos"
                    ),
                    pageKeywords = setOf(
                        "pagina",
                        "page",
                        "pag"
                    )
                ),
                RegexMatcher(
                    isCaseSensitive = true,
                    isGlobal = true,
                    queries = setOf(
                        RegexMatcher.Query("Car", "([A-Z]{2}\\-[\\d]{3}\\-[A-Z]{2})"),
                        RegexMatcher.Query("Age", "(\\d+)[ -]*(ans|jarige)"),
                        RegexMatcher.Query(
                            "Percent",
                            "([\\-]?(\\d)+[\\.\\,]*(\\d)*)[ ]*(%|per|percent|pourcent|procent)"
                        )
                    )
                )
            ),
            output = Output(
                granularity = Output.Granularity.WORD,
                includeMarginals = false,
                formats = setOf(
                    Json,
                    Text,
                    Csv,
                    Markdown
                )
            )
        )

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