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

object OutOfPageRemoval : Cleaner("out-of-page-removal")

data class WhitespaceRemoval(val minWidth: Int = 0) : Cleaner("whitespace-removal")

data class RedundancyDetection(val minOverlap: Double = 0.5) : Cleaner("redundancy-detection")

data class TableDetection(val runConfig: List<Option> = emptyList()) : Cleaner("table-detection") {

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

data class HeaderFooterDetection(
    val ignorePages: Set<Int> = emptySet(),
    val maxMarginPercentage: Int = 15
) : Cleaner("header-footer-detection")

data class ReadingOrderDetection(
    val minVerticalGapWidth: Int = 5,
    val minColumnWidthInPagePercent: Double = 15.0
) : Cleaner("reading-order-detection")

object LinkDetection : Cleaner("link-detection")
object ImageDetection : Cleaner("image-detection")
data class WordsToLine(
    val lineHeightUncertainty: Double = 0.2,
    val topUncertainty: Double = 0.4,
    val maximumSpaceBetweenWords: Int = 100,
    val mergeTableElements: Boolean = false
) : Cleaner("words-to-line")

data class LinesToParagraph(val tolerance: Double = 0.25) : Cleaner("lines-to-paragraph")

object HeadingDetection : Cleaner("heading-detection")

object HeadingDetectionDt : Cleaner("heading-detection-dt")

object ListDetection : Cleaner("list-detection")

object PageNumberDetection : Cleaner("page-number-detection")

object HierarchyDetection : Cleaner("hierarchy-detection")

data class TableOfContentsDetection(
    val keywords: Set<String> = emptySet(),
    val pageKeywords: Set<String> = emptySet()
) : Cleaner("table-of-contents-detection")

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

class UnknownCleaner(name: String) : Cleaner(name)