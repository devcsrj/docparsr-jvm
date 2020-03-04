package com.github.devcsrj.docparsr

data class Extractor(
    val pdfExtractor: PdfExtractor = PdfMiner,
    val ocrExtractor: OcrExtractor = Tesseract,
    val languages: Set<String> = setOf("eng")
)

sealed class PdfExtractor(val name: String)
object PdfMiner : PdfExtractor("pdfminer")
object PdfJs : PdfExtractor("pdfjs")
class UnknownPdfExtractor(name: String) : PdfExtractor(name)

sealed class OcrExtractor(val name: String)
object Tesseract : OcrExtractor("tesseract")
class UnknownOcrExtractor(name: String) : OcrExtractor(name)
