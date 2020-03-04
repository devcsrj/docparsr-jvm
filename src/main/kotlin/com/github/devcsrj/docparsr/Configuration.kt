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

data class Configuration(
    val version: String = "0.9",
    val extractor: Extractor = Extractor(),
    val cleaners: Set<Cleaner> = setOf(
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
