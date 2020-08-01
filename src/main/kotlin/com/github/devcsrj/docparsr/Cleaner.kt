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

import com.fasterxml.jackson.annotation.JsonValue

sealed class Cleaner(val name: String)

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/DrawingDetectionModule)
 */
object DrawingDetection : Cleaner("drawing-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/HeaderFooterDetectionModule)
 */
data class HeaderFooterDetection(
    val ignorePages: Set<Int> = emptySet(),
    val maxMarginPercentage: Int = 15
) : Cleaner("header-footer-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/HierarchyDetectionModule)
 */
object HierarchyDetection : Cleaner("hierarchy-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/ImageDetectionModule)
 */
data class ImageDetection(val ocrImages: Boolean = false) : Cleaner("image-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/KeyValueDetectionModule)
 */
data class KeyValueDetection(
    val threshold: Double = 0.2,
    val keyValueDividedChars: Set<String>,
    val keyPatterns: Map<String, Set<String>>
) : Cleaner("key-value-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/LinesToParagraphModule)
 */
data class LinesToParagraph(val tolerance: Double = 0.25) : Cleaner("lines-to-paragraph")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/LinkDetectionModule)
 */
object LinkDetection : Cleaner("link-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/ListDetectionModule)
 */
object ListDetection : Cleaner("list-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/MlHeadingDetectionModule)
 */
object MlHeadingDetection : Cleaner("ml-heading-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/NumberCorrectionModule)
 */
data class NumberCorrection(
    val fixSplitNumbers: Boolean = true,
    val maxConsecutiveSplits: Int = 3,
    val numberRegExp: String,
    val whitelist: Set<String> = emptySet()

) : Cleaner("number-correction")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/OutOfPageRemovalModule)
 */
object OutOfPageRemoval : Cleaner("out-of-page-removal")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/PageNumberDetectionModule)
 */
object PageNumberDetection : Cleaner("page-number-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/ReadingOrderDetectionModule)
 */
data class ReadingOrderDetection(
    val minVerticalGapWidth: Int = 5,
    val minColumnWidthInPagePercent: Double = 15.0
) : Cleaner("reading-order-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/RedundancyDetectionModule)
 */
data class RedundancyDetection(val minOverlap: Double = 0.5) : Cleaner("redundancy-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/RegexMatcherModule)
 */
data class RegexMatcher(
    val isCaseSensitive: Boolean = true,
    val isGlobal: Boolean = true,
    val queries: Set<Query> = emptySet()
) : Cleaner("regex-matcher") {
    data class Query(
        val label: String,
        val regex: String
    )
}

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/SeparateWordsModule)
 */
object SeparateWords : Cleaner("separate-words")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/TableDetectionModule)
 */
data class TableDetection(
    val checkDrawings: Boolean = true,
    val runConfig: List<Option> = emptyList()) : Cleaner("table-detection") {
    data class Option(
        val pages: Set<Int> = emptySet(),
        val flavor: Flavor = Flavor.LATTICE
    )

    enum class Flavor {

        LATTICE,

        STREAM;

        @JsonValue // :(
        fun toValue() = this.name.toLowerCase()

        companion object {

            fun fromString(str: String) = valueOf(str.toUpperCase())


        }

    }
}

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/TableOfContentsDetectionModule)
 */
data class TableOfContentsDetection(
    val pageKeywords: Set<String> = emptySet()
) : Cleaner("table-of-contents-detection")

/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/WhitespaceRemovalModule)
 */
data class WhitespaceRemoval(val minWidth: Int = 0) : Cleaner("whitespace-removal")
/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/WordsToLineModule)
 */
data class WordsToLine(
    val lineHeightUncertainty: Double = 0.2,
    val topUncertainty: Double = 0.4,
    val maximumSpaceBetweenWords: Int = 100,
    val mergeTableElements: Boolean = false
) : Cleaner("words-to-line")
/**
 * See [module](https://github.com/axa-group/Parsr/tree/master/server/src/processing/WordsToLineNewModule)
 */
object WordsToLineNew : Cleaner("words-to-line-new")

class UnknownCleaner(name: String) : Cleaner(name)
