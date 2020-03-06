package com.github.devcsrj.docparsr

interface PageVisitor {

    fun visitHeading(heading: Heading)
    fun visitLine(line: Line)
    fun visitParagraph(paragraph: Paragraph)
    fun visitWord(word: Word)
    fun visitAny(element: AnyElement)
}