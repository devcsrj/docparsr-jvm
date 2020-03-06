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
            properties = mutableMapOf(
                "order" to 1
            ),
            content = "Hello",
            fontId = FontId.valueOf(1)
        )
        val word2 = Word(
            id = ElementId.valueOf(2),
            box = Box(1.0, 1.0, 1.0, 1.0),
            properties = mutableMapOf(
                "order" to 2
            ),
            content = "world",
            fontId = FontId.valueOf(1)
        )
        val line = Line(
            id = ElementId.valueOf(3),
            box = Box(1.0, 1.0, 2.0, 2.0),
            properties = mutableMapOf(),
            content = arrayListOf(word1, word2)
        )
        val heading = Heading(
            id = ElementId.valueOf(4),
            box = Box(1.0, 1.0, 2.0, 2.0),
            properties = mutableMapOf(),
            content = arrayListOf(line),
            level = 1
        )
        val paragraph = Paragraph(
            id = ElementId.valueOf(5),
            box = Box(1.0, 1.0, 2.0, 2.0),
            properties = mutableMapOf(),
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

                override fun visitAny(element: AnyElement) {
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
