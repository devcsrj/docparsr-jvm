package com.github.devcsrj.docparsr

sealed class Cleaner(name: String)

object OutOfPageRemoval : Cleaner("out-of-page-removal")

data class WhitespaceRemoval(val minWidth: Int = 0) : Cleaner("whitespace-removal")

data class RedundancyDetection(val minOverlap: Double = 0.5) : Cleaner("redundancy-detection")

data class TableDetection(val options: List<Option> = emptyList()) : Cleaner("table-detection") {

    data class Option(
        val pages: Set<Int> = emptySet(),
        val flavor: Flavor = Flavor.LATTICE
    )

    enum class Flavor {
        LATTICE,
        STREAM;

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
    val caseSensitive: Boolean = true,
    val global: Boolean = true,
    val queries: Set<Query> = emptySet()
) : Cleaner("regex-matcher") {

    data class Query(
        val label: String,
        val regex: String
    )
}

data class UnknownCleaner(val name: String) : Cleaner(name)