package com.github.devcsrj.docparsr

data class Output(
    val granularity: Granularity = Granularity.WORD,
    val includeMarginals: Boolean = false,
    val formats: Set<Format> = setOf(Json)
) {

    enum class Granularity {
        WORD,
        CHARACTER
    }
}

sealed class Format(val name: String)
object Json : Format("json")
object Text : Format("text")
object Csv : Format("csv")
object Markdown : Format("markdown")
object Pdf : Format("pdf")
