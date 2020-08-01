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

object GoldenConfiguration {

    val INSTANCE = Configuration(
        version = "0.9",
        extractor = Extractor(
            pdfExtractor = PdfMiner,
            ocrExtractor = Tesseract,
            languages = setOf("eng", "fra")
        ),
        cleaners = setOf(
            DrawingDetection,
            ImageDetection(false),
            OutOfPageRemoval,
            WhitespaceRemoval(0),
            RedundancyDetection(0.5),
            TableDetection(
                checkDrawings = true,
                runConfig = listOf(
                    TableDetection.Option(
                        pages = emptySet(),
                        flavor = TableDetection.Flavor.LATTICE
                    )
                )
            ),
            HeaderFooterDetection(
                ignorePages = emptySet(),
                maxMarginPercentage = 8
            ),
            LinkDetection,
            WordsToLineNew,
            ReadingOrderDetection(
                minColumnWidthInPagePercent = 15.0,
                minVerticalGapWidth = 5
            ),
            LinesToParagraph(0.25),
            TableOfContentsDetection(
                pageKeywords = setOf("pagina", "page", "pag")
            ),
            MlHeadingDetection,
            ListDetection,
            PageNumberDetection,
            HierarchyDetection,
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
            includeDrawings = false,
            includeMarginals = false,
            formats = setOf(
                Json,
                Text,
                Csv,
                SimpleJson,
                Markdown
            )
        )
    )
}