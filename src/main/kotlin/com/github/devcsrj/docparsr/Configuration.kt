package com.github.devcsrj.docparsr

data class Configuration(
    val version: String = "0.9",
    val extractor: Extractor = Extractor(),
    val cleaner: Set<Cleaner> = setOf(
        OutOfPageRemoval,
        WhitespaceRemoval(),
        RedundancyDetection(),
        TableDetection(),
        HeaderFooterDetection(),
        ReadingOrderDetection(),
        LinkDetection,
        ImageDetection,
        WordsToLine(),
        LinesToParagraph(),
        HeadingDetection,
        HeadingDetectionDt,
        ListDetection,
        PageNumberDetection,
        HierarchyDetection,
        TableOfContentsDetection(),
        RegexMatcher()
    ),
    val output: Output = Output()
)
