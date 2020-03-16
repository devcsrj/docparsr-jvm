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

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*

object PageTest : Spek({

    describe("page") {

        val word1 = Word(
            id = ElementId.valueOf(1),
            box = Box(1.0, 1.0, 1.0, 1.0),
            properties = Properties(
                order = 1
            ),
            metadata = emptySet(),
            content = "Hello",
            fontId = FontId.valueOf(1)
        )
        val word2 = Word(
            id = ElementId.valueOf(2),
            box = Box(1.0, 1.0, 1.0, 1.0),
            properties = Properties(
                order = 2
            ),
            metadata = emptySet(),
            content = "world",
            fontId = FontId.valueOf(1)
        )
        val line = Line(
            id = ElementId.valueOf(3),
            box = Box(1.0, 1.0, 2.0, 2.0),
            properties = Properties(),
            metadata = emptySet(),
            content = arrayListOf(word1, word2)
        )
        val heading = Heading(
            id = ElementId.valueOf(4),
            box = Box(1.0, 1.0, 2.0, 2.0),
            properties = Properties(),
            metadata = emptySet(),
            content = arrayListOf(line),
            level = 1
        )
        val paragraph = Paragraph(
            id = ElementId.valueOf(5),
            box = Box(1.0, 1.0, 2.0, 2.0),
            properties = Properties(),
            metadata = emptySet(),
            content = arrayListOf(line)
        )
        val page = Page(
            number = 1,
            box = Box(1.0, 1.0, 20.0, 20.0),
            rotation = Rotation(0.0, Point(0, 0), Point(0, 0)),
            elements = arrayListOf(
                heading,
                paragraph
            )
        )

        it("can visit tree") {

            val queue = LinkedList<Element<*>>()

            class QueuingVisitor : PageVisitor {
                override fun visitHeading(heading: Heading) {
                    queue.add(heading);
                }

                override fun visitLine(line: Line) {
                    queue.add(line);
                }

                override fun visitParagraph(paragraph: Paragraph) {
                    queue.add(paragraph);
                }

                override fun visitWord(word: Word) {
                    queue.add(word);
                }

                override fun visitAnyElement(element: AnyElement) {
                    queue.add(element);
                }

            }
            page.accept(QueuingVisitor())

            assertThat(queue).containsExactly(
                heading,
                line,
                word1,
                word2,
                paragraph,
                line,
                word1,
                word2
            )
        }
    }
})
