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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Supplier

class Document(private val source: Supplier<InputStream>) {

    companion object {
        fun from(result: ParsingResult) = Document(Supplier { result.source(Json) })
        fun from(file: Path) = Document(Supplier { Files.newInputStream(file) })
    }

    fun accept(visitor: DocumentVisitor) {
        Jackson.MAPPER.factory.createParser(source.get()).use {
            require(it.nextToken() == JsonToken.START_OBJECT) { "expecting '{' (START_OBJECT) from source" }
            while (it.nextToken() != JsonToken.END_OBJECT) {
                when (it.currentName) {
                    "metadata" -> acceptMetadata(it, visitor)
                    "pages" -> acceptPages(it, visitor)
                    "fonts" -> acceptFonts(it, visitor)
                }
            }
        }
    }

    private fun acceptMetadata(parser: JsonParser, visitor: DocumentVisitor) {
        require(parser.nextToken() == JsonToken.START_ARRAY) { "expecting '[' (START_ARRAY) for field 'metadata'" }
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            val meta = parser.readValueAs(Metadata::class.java)
            visitor.visitMeta(meta)
        }
    }

    private fun acceptPages(parser: JsonParser, visitor: DocumentVisitor) {
        require(parser.nextToken() == JsonToken.START_ARRAY) { "expecting '[' (START_ARRAY) for field 'pages'" }
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            val page = parser.readValueAs(Page::class.java)
            visitor.visitPage(page)
            page.accept(object : PageVisitor {
                override fun visitHeading(heading: Heading) = visitor.visitHeading(heading)
                override fun visitLine(line: Line) = visitor.visitLine(line)
                override fun visitParagraph(paragraph: Paragraph) = visitor.visitParagraph(paragraph)
                override fun visitWord(word: Word) = visitor.visitWord(word)
                override fun visitAnyElement(element: AnyElement) = visitor.visitAnyElement(element)
            })
        }

    }

    private fun acceptFonts(parser: JsonParser, visitor: DocumentVisitor) {
        require(parser.nextToken() == JsonToken.START_ARRAY) { "expecting '[' (START_ARRAY) for field 'fonts'" }
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            val font = parser.readValueAs(Font::class.java)
            visitor.visitFont(font)
        }
    }
}
