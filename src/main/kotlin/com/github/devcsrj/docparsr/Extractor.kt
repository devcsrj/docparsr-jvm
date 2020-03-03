package com.github.devcsrj.docparsr

data class Extractor(
    val pdfExtractor: PdfExtractor = PdfMiner,
    val ocrExtractor: OcrExtractor = Tesseract,
    val languages: Set<String> = setOf("eng")
)

sealed class PdfExtractor(name: String)
object PdfMiner : PdfExtractor("pdfminer")
object PdfJs : PdfExtractor("pdfjs")
data class UnknownPdfExtractor(val name: String) : PdfExtractor(name)

sealed class OcrExtractor(name: String)
object Tesseract : OcrExtractor("tesseract")
data class UnknownOcrExtractor(val name: String) : OcrExtractor(name)
